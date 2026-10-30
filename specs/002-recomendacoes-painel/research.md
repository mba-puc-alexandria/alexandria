# Pesquisa: recomendações diárias

Data: 2026-10-30. Escopo final: uma seleção por dia, até 30 livros, catálogo inteiro considerado e carrossel cíclico. As opções anteriores de recálculo por consulta, cursores e duas atualizações ao dia foram substituídas pelas decisões posteriores do usuário.

## 1. Base real do projeto

**Decisão**: usar o backend Java 17/Spring Boot 3.4.4 e o frontend Next 16.2.4/React 19.2.4 existentes.

**Evidência**: `alexandria-backend/pom.xml`, `alexandria-frontend/package-lock.json`, `CONTEXT.md` e guias de arquitetura. Backend segue ports/adapters. Scheduling e Actuator já existem. Não há cache externo ou biblioteca de carrossel configurados.

**Alternativas**: microserviço, Python, serviço externo de IA e novo framework de estado foram rejeitados por não serem necessários à seleção de 30 títulos.

## 2. Banco e catálogo digital

**Decisão**: duas tabelas derivadas em Flyway V4; nenhuma alteração destrutiva nas tabelas existentes.

**Razão**: `application.properties` e `application-rds.properties` habilitam Flyway e `ddl-auto=validate`. O AGENTS do backend afirma o contrário e está desatualizado nesse ponto. Última migração encontrada: V3. Os testes H2 desabilitam Flyway; precisam de validação complementar em MySQL para a nova migração.

**Evidência**: entidades `BookEntity`, `UserBooksEntity`, repositórios JPA e migrations existentes. `(user_id, book_id)` já é único na biblioteca. `BookSource.LOCAL` e campos antigos ainda existem no código, mas o usuário confirmou que o catálogo atual contém somente digitais; não reintroduzir cadastro local. Disponibilidade para recomendação exige URL digital não vazia, conforme o caminho de leitura existente. Esse filtro não depende de assinatura do leitor e não faz download/verificação remota por livro.

**Alternativas**: manter ranking apenas em memória perde a seleção no reinício; reusar tabelas da biblioteca mudaria dados de negócio. Guardar todos os candidatos em novas linhas por leitor seria desnecessário: persistir apenas até 30.

## 3. Definição inicial de afinidade

**Decisão**: filtragem baseada em conteúdo com pesos inteiros, normalização determinística e justificativa por evidência.

| Estado/nota | Peso por livro |
| --- | --- |
| done 4 ou 5 | 4 |
| reading | 3 |
| toread | 2 |
| done 3 | 1 |
| done 0, 1 ou 2 | 0 |

Para cada assunto, autor ou idioma do perfil, guardar o maior peso observado e sua evidência. Empates de evidência escolhem o menor bookId. Isso evita que muitos livros de baixo sinal multipliquem arbitrariamente a força de uma única característica.

Assuntos: separar por `;` (formato do mapper Gutendex), aplicar Unicode NFKC, minúsculas com Locale.ROOT, trim e compactar espaços; preservar a frase inteira, acentos e subdivisões `--`. Não dividir por vírgulas nem inventar traduções/sinônimos. Idiomas: separar por `,` e normalizar códigos. Autores: IDs existentes, não nomes. Deduplicar características por livro.

Para um candidato, calcular:

- Smax: maior peso de assunto coincidente; Smean: média dos pesos dos assuntos coincidentes, escalada para inteiro por `floor(1000 * soma / quantidade_de_assuntos_coincidentes)`; zero sem coincidência.
- Amax e Amean: equivalentes para autores coincidentes.
- Lmax: maior peso de idioma coincidente, zero quando ausente.
- P: downloadCount não negativo; nulo ou negativo = 0.

Elegível somente se não pertence à biblioteca, tem acesso digital disponível e `Smax > 0` ou `Amax > 0`. Ordenar por `(Smax DESC, Smean DESC, Amax DESC, Amean DESC, Lmax DESC, P DESC, bookId ASC)`. Não há precisão de ponto flutuante nem sorteio. O peso forte vence o intermediário em comparações com demais características iguais. Assunto precede autor; idioma/popularidade nunca criam elegibilidade. Sem penalização global por nota baixa.

Justificativa: usar assunto com maior peso quando houver, caso contrário autor com maior peso; guardar a evidência positiva e os textos necessários à explicação no dia da geração. Não exibir identificadores de outros leitores. As médias reduzem a vantagem de muitos tags e a ordem é reproduzível; qualidade semântica depende da cobertura dos assuntos. Sinal de afinidade é uma aproximação testável de estilo, não garantia de satisfação.

**Alternativas**: similaridade por texto livre, LLM ou embeddings podem ser avaliados depois; exigem infraestrutura e avaliação adicionais. Filtragem colaborativa usa dados de outros usuários e não é necessária ao escopo.

**Fonte primária**: [Google — Content-based filtering](https://developers.google.com/machine-learning/recommendation/content-based/basics). A fonte sustenta o método geral; pesos e critérios acima são decisões deste projeto, não recomendações atribuídas à fonte.

## 4. Considerar toda a base sem custo por movimento do carrossel

**Decisão**: construir uma projeção compacta completa por execução, ler catálogo por chave ID em lotes de 500 e compartilhar o resultado entre trabalhadores. Fazer a leitura completa em transação read-only consistente do MySQL; buscar autores por IDs em lote, sem coleção paginada e sem N+1. Encerrar a transação antes dos cálculos por usuário.

Para cada leitor, consultar a biblioteca completa por projeção própria (book/status/rating/features). Não reutilizar `ListUserBooksUseCase`, que monta uma página e faz consultas por livro. Avaliar todos os registros elegíveis e manter somente os melhores 30 numa heap. Comparar o resultado com ordenação integral em testes.

**Razão**: evita um corte por popularidade/primeiras páginas e limita o custo das respostas. A complexidade de CPU diária é aproximadamente leitores x características do catálogo; guardar só 30 itens não elimina o custo de avaliar todos.

**Alternativas**: varrer o banco novamente para cada leitor aumenta I/O; carregar todos os candidatos no navegador expõe dados e aumenta tráfego; seleção por primeira página viola o requisito.

**Fonte primária**: [Spring Data — scrolling e ordenação estável](https://docs.spring.io/spring-data/jpa/reference/jpa/query-methods.html). A consulta concreta será ID-keyset explícita compatível com a versão instalada; não assumir APIs da documentação mais recente sem verificação.

## 5. Geração diária durável e recuperável

**Precedência consolidada**: A ordem de decisão é: (1) reutilizar READY de hoje, inclusive vazia, aplicando exclusões atuais; (2) sem READY de hoje, verificar o perfil atual antes de qualquer fallback; sem sinal positivo, responder EMPTY/NO_PROFILE, selection=null e items=[], sem servir seleção antiga ou iniciar ranking; (3) com sinal positivo, solicitar/retomar a geração diária e usar a última READY como STALE enquanto necessário, ou 202 sem anterior; falha sem anterior retorna 503. Mudanças intradiárias de perfil não invalidam uma READY de hoje: influenciam somente a próxima seleção. O caminho NO_PROFILE não publica READY e permite a primeira geração do dia se surgirem sinais positivos depois.

**Decisão**: default 00h America/Sao_Paulo, configurável. Data de referência usa Clock injetável. Unique por usuário/data, claim com lease/token e publicação atômica. Um READY vazio também é resultado final do dia.

Primeiro acesso sem seleção do dia pode iniciar a primeira geração desse dia, em segundo plano. Falhas podem ser repetidas até sucesso sem criar uma segunda publicação pronta. Recuperação após reinício e reconciliador de 5 minutos cobrem cron perdido. READY nunca é reconstruído por GET, alteração de perfil ou retry. Atualização de interesses fica para o próximo dia; inclusão na biblioteca afeta exclusão imediatamente.

Leases usam UTC do banco para expiração; UUID aleatório evita que um trabalhador antigo publique após outro assumir. Computação fora da transação. Executor próprio de dois trabalhadores; não reusar `gutendexSyncExecutor`, que tem configuração para sincronização externa.

**Alternativas**: cron isolado não resolve múltiplas réplicas ou downtime; scheduler distribuído externo e Kafka seriam excessivos. Não prometer exatamente uma tentativa de CPU: a garantia é no máximo uma seleção READY por usuário/data, com tentativas recuperáveis.

## 6. Consistência com a biblioteca

**Decisão**: GET sempre filtra os itens por pertencimento atual em `user_books` e disponibilidade atual do livro. Não gravar exclusão permanente no snapshot. Se um livro for removido posteriormente da biblioteca, pode reaparecer por já pertencer à seleção daquele dia. Isso não é nova geração.

O add existente não tem transação abrangente. Não tentar acoplar mutação de biblioteca e limpeza do snapshot; o anti-join e a remoção confirmada no frontend atendem à regra sem alterar o fluxo de gravação. Concorrência de duas inclusões deve resultar em um vínculo e 409 para a duplicada, inclusive no caminho de violação da restrição única.

Frontend cancela respostas iniciadas antes da inclusão, filtra IDs recém-adicionados e limpa o estado ao trocar de conta. Revalidar no retorno ao Painel e no foco da aba; eventos leves de mutação notificam a aba atual, e BroadcastChannel/storage pode transportar somente invalidação, sem títulos ou biblioteca. Backend segue como autoridade.

## 7. Carrossel curto e acessível

**Decisão**: retornar a seleção inteira (<=30); sem cursor, sem requisição por volta e sem autoplay. Índice modular, controles sempre acessíveis por toque/teclado; uma volta só reinicia após avanço do leitor. Até 30 elementos/dados mantidos, sem clones acumulados e com foco preservado ao remover um cartão.

`BookCard` atual tem ação de hover e estado local sem callback: criar cartão dedicado reaproveitando tokens visuais e `getAuthorDisplay`, sem impor a limitação de hover. `apiFetch` já propaga JWT e aceita AbortSignal. Links de detalhe: `/explorar/{bookId}`. Inclusão reusa `addUserBook`, que reconhece 409 como already_added.

**Fonte primária**: [W3C — Carousel pattern](https://www.w3.org/WAI/ARIA/apg/patterns/carousel/). Aplicar controles, foco e região rotulada; reinício circular não significa rotação automática por tempo.

## 8. Riscos, limites e como medir

- Banco: novos writes limitados a até 30 itens por seleção; claims/publicações curtos. Sem alteração de livros/perfil pelo cálculo. Migração de tabelas novas ainda exige validação de chaves e execução no pipeline real.
- Retenção: manter atual e última pronta anterior limita histórico a até 60 itens por leitor; não é 60 itens totais no sistema. Para mil leitores, até 60 mil itens históricos, mais até 30 mil em publicação/limpeza; monitorar tamanho real com razões e índices.
- CPU: todo catálogo por leitor pode atrasar o job; limitar concorrência e fila, medir duração, sem resolver atraso por truncamento oculto. Recém-gerado pode refletir perfil do começo do cálculo, como qualquer snapshot diário.
- Memória: projeção completa é compartilhada por execução; medir heap/RSS e pico na construção. Geração falha sem publicar parcialmente se não for possível ler a base completa; preservar última READY.
- Falhas: última pronta fica disponível como stale com aviso e filtro atual. Erro não faz overwrite de READY nem força leitura de Gutendex no request.
- Qualidade: uma seleção estável pode ter os mesmos 30 livros amanhã se catálogo/perfil não mudarem. Renovação aleatória de todos os títulos ou cooldown de recomendações vistas não faz parte do escopo.
- Meta de 2s não foi medida. Definir ensaio reproduzível de GETs prontos e job diário; testar MySQL real e cold start/recuperação separadamente.

Todas as decisões necessárias ao desenho estão resolvidas. Dimensionamento é uma hipótese explícita de ensaio, com gate de medição na implementação; não uma pendência de definição de produto.

## 9. Revisão após análise

Decisões de consistência aprovadas pelo usuário: precedência descrita acima, WARN operacional agregado para atraso de geração (default 60min e no máximo uma vez por hora por instância/data), e medição de SC-006 no navegador com cinco aquecimentos e pelo menos 100 amostras válidas por perfil. Notificação externa não é necessária ao MVP. Ver plan.md e quickstart.md para protocolo e limites.
