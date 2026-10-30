# Modelo de dados: seleções diárias

## Entidades existentes, sem alteração destrutiva

- `books`: identidade, título, autores via book_authors, subjects separados por ponto e vírgula, languages por vírgula, download_url, cover_url e download_count. Elegibilidade digital requer download_url não vazia; source legado não cria um fluxo de cadastro local.
- `user_books`: user_id, book_id, status, progress, rating (0–5), created_at. Unique(user_id, book_id). Usar todos os registros do leitor, não uma Page padrão. created_at não é data de conclusão.
- `users`: referência do dono da seleção. Autorização nunca usa userId enviado pelo cliente.

## recommendation_batches — nova persistência

| Campo | Tipo lógico / MySQL | Regra |
| --- | --- | --- |
| id | Long / BIGINT AUTO_INCREMENT | PK |
| user_id | Long / BIGINT | FK users; não nulo |
| reference_date | LocalDate / DATE | dia America/Sao_Paulo capturado na criação de PENDING e preservado no claim |
| status | enum / VARCHAR(16) | PENDING, BUILDING, READY, FAILED |
| algorithm_version | String / VARCHAR(32) | versão do normalizador e comparador |
| catalog_fingerprint | String / CHAR(64), nulo antes de calcular | SHA-256 de dados relevantes ordenados |
| profile_fingerprint | String / CHAR(64), nulo antes de calcular | checksum privado dos vínculos/sinais usados |
| selected_count | Integer / SMALLINT | quantidade original publicada, 0–30; não muda quando um livro é filtrado/excluído |
| generated_at | Instant / DATETIME(6), nullable | UTC; preenchido somente em READY |
| lease_token | UUID / CHAR(36), nullable | propriedade da tentativa |
| lease_until | Instant / DATETIME(6), nullable | UTC do banco; só worker titular renova |
| attempt_count | Integer / INT | claims totais para diagnóstico; orçamento automático usa automatic_attempt_count |
| automatic_attempt_count | Integer / INT | claims AUTO efetivos; máximo 3 por data; NO_PROFILE reverte consumo da tentativa |
| failed_at | Instant / DATETIME(6), nullable | UTC da falha; lease_until observado em LEASE_EXPIRED; base do cooldown manual |
| next_attempt_at | Instant / DATETIME(6), nullable | backoff de recuperação |
| created_at, updated_at | Instant / DATETIME(6) | UTC; created_at é registrado ao solicitar/constatar PENDING, antes de aguardar fila, e permanece como data de criação; idade operacional usa operational_pending_since |
| operational_pending_since | Instant / DATETIME(6), nullable | UTC; inicialmente created_at; null em dormência NO_PROFILE; reinicia em UTC do banco ao reativar |
| failure_code | String / VARCHAR(64), nullable | código sanitizado; sem stack/segredos |

Unicidade `(user_id, reference_date)`. Índice de retomada `(reference_date, status, next_attempt_at, lease_until, id)`. Índice de busca da última pronta `(user_id, status, reference_date)`. A consulta de retomada pode ser dividida entre falhas e leases expirados para aproveitar índices; verificar EXPLAIN no MySQL antes de otimizar com outro índice.

Status READY é imutável, mesmo quando tem zero itens. Não atualizar gerados hoje por mudança de perfil. Quando todos os selecionados entram na biblioteca, o header continua READY e a consulta retorna lista vazia.

## recommendation_items — nova persistência

| Campo | Tipo lógico / MySQL | Regra |
| --- | --- | --- |
| batch_id | Long / BIGINT | FK batch, ON DELETE CASCADE |
| position | Integer / SMALLINT | 1–30, sem lacunas na publicação inicial |
| book_id | Long / BIGINT | FK books, ON DELETE CASCADE para não impedir exclusão do livro |
| reason_type | enum / VARCHAR(16) | SUBJECT ou AUTHOR |
| reason_value | String / TEXT | assunto ou nome de autor observado |
| reason_text | String / TEXT | explicação em português baseada no dia da geração |
| source_book_id | Long / BIGINT, nullable | FK books, ON DELETE SET NULL |
| source_book_title | String / VARCHAR(500) | título de evidência no momento da geração |
| evidence_weight | Integer / SMALLINT | 1–4; nunca 0 |

PK `(batch_id, position)`; unique `(batch_id, book_id)`; índice de FK book_id e source_book_id. A entidade persistida pode usar chave composta ou ID técnico, mantendo ambas as restrições. Não expor source_book_id nem fingerprints no contrato público; o texto pode citar um livro do próprio leitor.

Regras position<=30, peso positivo, motivo válido e ausência de duplicidade são verificadas no domínio e no banco quando suportado. DDL deve ser validado na versão MySQL do ambiente; não confiar só em CHECK de versões antigas.

Não guardar score inteiro ou todos os candidatos: position é a ordem congelada. Razões são snapshots e a interface indica o dia da seleção; não afirmar que avaliações atuais são as mesmas do dia da geração.

## Modelos temporários de domínio

- `CatalogBookFeatures`: bookId, authorIds, canonicalSubjects, languageCodes, downloadCount; somente livros digitais disponíveis. Snapshot consistente completo da execução/referenceDate, sem entidades JPA/EPUBs e sem URLs enormes desnecessárias ao cálculo. O identificador de sessão inclui a data; consumidores de hoje não recebem a projeção de ontem, sem invalidar handles antigos ativos.
- `ReadingProfile`: excludedBookIds e mapas feature -> peso/evidência por usuário. Dados das projeções da biblioteca atual, inclusive metadados necessários à justificativa.
- `RecommendationCandidate`: bookId, tupla inteira de afinidade, motivo e evidência. Heap limitada a 30, mas todos os registros do catálogo são avaliados.
- `DailySelection`: batch + itens ordenados. Consulta remove os que hoje pertencem à biblioteca ou não estão mais disponíveis, sem alterar a seleção persistida e sem repor.

O cálculo detalhado está em research.md. Para evidência/score equivalentes, bookId crescente desempata. Não há limite por autor que descarte um item elegível dentro do top-30.

## Estados e transações

```text
inexistente -> PENDING -> BUILDING -> READY (imutável)
                          |           |
                          v           +-> leitura com exclusões atuais
                        FAILED
                          |
                          +-> BUILDING (novo token, backoff)
BUILDING com lease expirado -> FAILED (CAS de recuperação) -> BUILDING elegível com novo token
```

Claim: transação curta tenta criar/assumir o header, incluindo PENDING existente e FAILED elegível; normalizar BUILDING expirado para FAILED antes, mesmo com orçamento AUTO esgotado; unique e UPDATE condicional decidem o vencedor. Cálculo ocorre fora dessa transação. Heartbeat e transição para FAILED exigem BUILDING, token coincidente e lease_until > UTC_TIMESTAMP(6). Lease expirado não pode ser ressuscitado; o trabalhador deve parar e passar por um novo claim com novo token.

Publicação: transação curta bloqueia/verifica header/token/lease não expirado, insere <=30 itens e muda status para READY. Exceção faz rollback dos itens e do status; o último snapshot anterior permanece disponível. Trabalhador com token antigo não pode publicar, marcar FAILED ou limpar itens da nova tentativa.

Filtragem de GET: query de até 30 itens com `NOT EXISTS` da biblioteca do usuário autenticado e disponibilidade digital atual. Carregar autores/DTOs em lote. Uma inclusão concorrente confirmada no próprio Painel remove o livro também no frontend e invalida respostas anteriores. GET concorrente a um commit de outra aba é revalidado no foco/notificação; não prometer sincronismo instantâneo entre abas sem mensagem.

## Retenção, volume e segurança

Depois de publicar nova READY, reter atual e última pronta anterior; não excluir última válida em caso de falha. Limpar batches antigos de FAILED/PENDING e BUILDING com lease vencido após 7 dias, nunca lease ativo. Se o leitor não gera novos dias, manter a última pronta para fallback. Excluir usuário deve apagar suas seleções via cascade, conforme política existente de exclusão de conta.

No máximo 30 itens por READY; até duas seleções históricas prontas por leitor após limpeza. Tentativas não publicadas não devem acumular itens. Dados de recomendações e evidências pertencem ao leitor; consultar sempre por user_id autenticado e enviar Cache-Control: no-store.

Não há escrita em books/user_books no job. Adicionar à biblioteca continua pelo endpoint existente. A recomendação volta a ser elegível se o leitor remover posteriormente o livro da biblioteca e ele ainda fizer parte da seleção diária.

A consulta aplica a precedência READY de hoje -> perfil atual sem sinais (EMPTY/NO_PROFILE) -> tentativa diária com eventual STALE anterior. Não usar uma seleção antiga para contornar NO_PROFILE. PENDING é registrado uma vez por leitor/data sem adquirir lease; o worker adquire o lease ao iniciar. Isso permite medir idade da fila sem manter locks ou lease durante espera. O aviso agregado usa somente data, contagem e idade, sem persistir um histórico adicional de notificações.

## Identidade diária e ausência de perfil no worker

reference_date é imutável desde PENDING. Não renomear headers nem iniciar claims de datas anteriores à data atual America/Sao_Paulo. Worker iniciado antes da virada pode publicar sob data original, sem substituir READY de hoje; recuperação cria/processa só o dia atual. Retenção remove pendências antigas inativas após sete dias.

Revalidar perfil após claim, antes de catálogo/ranking. Sem sinais positivos: BUILDING→PENDING condicional com token/lease válidos, liberando lease, sem itens/READY/failure_code e revertendo automatic_attempt_count se origem AUTO. attempt_count mantém diagnóstico. PENDING sem perfil fica dormente: pedidos/recovery verificam sinais antes de novo claim. Novos sinais no mesmo dia reativam o header. Perfil positivo sem candidatos publica READY vazia.

Origem AUTO (GET/cron/startup/recovery) ou MANUAL (POST explícito) acompanha trabalho coalescido e claim. AUTO incrementa ambos contadores atomicamente só quando automatic_attempt_count<3; MANUAL incrementa só attempt_count e respeita cooldown de 60s após falha. Duplicatas, fila cheia e perda do claim não incrementam. Contexto da tentativa guarda origem protegida pelo token; liberação NO_PROFILE é atômica e idempotente por token.

PENDING dormente sem perfil e headers de datas anteriores não entram no WARN de atraso operacional do dia atual. O job de retenção também remove BUILDING antigo com lease expirado após sete dias; o predicado preserva qualquer lease ativo.

Ao observar ausência de sinais em PENDING sem lease ativo, limpar operational_pending_since; liberação NO_PROFILE pelo worker também o limpa. Ao reativar por sinais positivos, definir o campo uma única vez via atualização condicional se null; polls/retries não o renovam. O WARN usa esse campo, referência de hoje e perfil positivo; nunca atribuir à dormência histórica atraso novo. attempt_count/automatic_attempt_count não são reiniciados pela reativação.

Recuperação de lease expirado usa CAS específico por BUILDING/token/lease observados, com tempo UTC do banco, e invalida token sem consumir tentativa. Worker titular continua proibido de marcar falha após expiração. failed_at registra instante da falha (ou lease_until observado na recuperação), next_attempt_at agenda somente AUTO; MANUAL usa failed_at+60s. Ver contrato de geração para corridas e esgotamento de orçamento.
