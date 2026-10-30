# Implementation Plan: Recomendações diárias no Painel

**Branch**: `feature/002-recomendacoes-painel-wip` | **Date**: 2026-10-30 | **Spec**: [spec.md](spec.md)

**Input**: `specs/002-recomendacoes-painel/spec.md`, com as decisões mais recentes do usuário: considerar todo o catálogo digital, selecionar até 30 livros por leitor uma vez por dia, repetir a seleção em ciclos e excluir imediatamente os livros adicionados à biblioteca.

## Summary

Gerar uma seleção diária persistida de até 30 livros por leitor. O processamento em segundo plano considera todo o catálogo; a leitura do Painel consulta apenas a seleção pronta e a biblioteca atual. O frontend recebe todos os até 30 cartões e repete a navegação localmente em grupos responsivos de capas verticais (2/3/4/5 conforme a largura). O título é “Você também pode gostar”; não exibir data, contagem ou motivo. Não há recálculo, paginação infinita nem requisição por volta do carrossel. Não há substituição de livros adicionados antes da geração seguinte.

A seleção usa afinidade determinística por assuntos e autores, idioma complementar e popularidade apenas como desempate. Não depende de IA externa, embeddings, outras bibliotecas privadas ou serviços adicionais.

## Technical Context

**Language/Version**: Java 17; TypeScript 5; Next.js 16.2.4; React 19.2.4 (versões do lockfile).

**Primary Dependencies**: Spring Boot 3.4.4, Spring Data JPA, Spring Security/JWT, Spring Scheduling já habilitado; Next/React, Tailwind 4, lucide-react. Nenhuma nova dependência de carrossel ou cache necessária.

**Storage**: MySQL 8. Duas novas tabelas de seleção e itens, criadas por Flyway V4. O estado pronto é durável após reinício. O catálogo compacto em memória é temporário, compartilhado entre os trabalhadores de uma execução da mesma referenceDate e descartado após concluir os trabalhos; não armazena EPUBs nem entidades JPA. Uma sessão de ontem não fornece catálogo para a seleção de hoje.

**Testing**: JUnit/Spring Boot/H2 para regras; MySQL Testcontainers para leases, unicidade, publicação e migração. Vitest 3.2.7/jsdom para frontend. Playwright 1.60.0 instalado como dependência, mas sem configuração/suíte atual: criar validação E2E na implementação.

**Target Platform**: Backend JVM/Linux e navegadores desktop/celular. Datas de referência calculadas explicitamente em `America/Sao_Paulo`; instantes de lease em UTC.

**Project Type**: Aplicação web com backend e frontend existentes.

**Performance Goals**: Com seleção pronta, p95 de exibição no Painel <=2s. Nenhuma reconstrução por avanço ou volta. Perfil de ensaio de engenharia: 100 mil livros, mil vínculos por biblioteca, mil leitores a processar por dia e 50 leitores simultâneos no Painel. Esses números são hipóteses de validação, não volume medido de produção nem limite de catálogo.

**Constraints**: Até 30 itens por seleção; no máximo uma publicação bem-sucedida por leitor/data; sem limite arbitrário nos candidatos avaliados. Estado `READY` é imutável. Exclusão atual da biblioteca em todo GET. Não fazer chamadas Gutendex ou leitura de EPUB durante ranking. Não manter transação ou lock de linha durante o cálculo.

**Scale/Scope**: Seleção diária às 00h por default configurável, uma primeira geração sob demanda quando não existe seleção do dia, processamento assíncrono, recuperação após reinício. Estimar custo da seleção pela base inteira, medir processamento diário e suportar menos de 30 itens quando necessário. Meta de engenharia inicial: concluir mil leitores em até 60 minutos no ambiente de ensaio; a janela e o consumo serão medidos, sem alegação de resultado já alcançado.

## Constitution Check

*GATE: verificação antes da pesquisa e novamente após o desenho.*

Não existe `.specify/memory/constitution.md` no Alexandria. Não importar princípios do SIPLAN. Gates locais vêm de `CONTEXT.md`, dos AGENTS dos módulos e dos guias de arquitetura:

| Gate | Antes da pesquisa | Após desenho |
| --- | --- | --- |
| Domínio sem framework; aplicação orquestra; adapters persistem | PASS | PASS: cálculo puro e portas dedicadas |
| Reusar autenticação, biblioteca e acesso à leitura | PASS | PASS: identidade por JWT; assinatura não é alterada |
| Toda a base considerada, até 30 selecionados, uma geração diária | PASS | PASS: varredura completa e heap top-30; unicidade por data |
| Sem novas integrações obrigatórias | PASS | PASS: Java/MySQL/frontend existentes |
| Seguir configuração real de schema | PASS | PASS: Flyway habilitado + ddl-auto=validate, V4 necessária |
| Código Next deve seguir documentação da versão instalada | PASS | PASS como obrigação para implementação após npm ci; nenhum código Next escrito nesta fase |
| Requisitos e contratos sem decisão bloqueante | PASS | PASS: horários, retenção e capacidade explicitados como defaults de engenharia |

Verde aqui significa conformidade do desenho, não testes ou desempenho comprovados.

## Project Structure

### Documentation (this feature)

```text
specs/002-recomendacoes-painel/
├── spec.md
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   ├── recommendations.openapi.yaml
│   ├── dashboard.md
│   └── daily-generation.md
└── checklists/requirements.md
```

A implementação inicial concluiu T001–T052. A revisão visual solicitada pelo usuário acrescenta T053–T055, sem alterar o motor ou os contratos de API.

O estado de entrada e as evidências de preparação estão em [implementation-readiness.md](implementation-readiness.md). Os scripts em `scripts/` verificam documentos/contratos e a ferramenta MySQL; não implementam a funcionalidade.

### Source Code (repository root)

Estrutura proposta, ainda não implementada:

```text
alexandria-backend/src/main/java/com/pucsp/alexandria/
├── domain/recommendation/             # perfil, pontuação, seleção, portas e estados
├── application/recommendation/        # Get/GenerateDailyRecommendationsUseCase + DTOs
├── adapter/in/rest/recommendation/    # controller + responses
├── adapter/in/job/                    # daily/recovery dispatcher
├── adapter/out/persistence/           # adapters de leitura completa e batches
│   ├── entity/                       # RecommendationBatchEntity, RecommendationItemEntity
│   └── jpa/                          # projeções, anti-join e claim condicional
└── config/                           # beans, properties, executor dedicado
alexandria-backend/src/main/resources/db/migration/
└── V4__create_recommendation_batches_and_items.sql
alexandria-backend/src/test/java/com/pucsp/alexandria/
├── domain/recommendation/
├── application/recommendation/
└── adapter/                          # REST + MySQL concorrente/migração
alexandria-frontend/src/
├── app/(main)/dashboard/page.tsx      # integra seção independente
├── components/recommendations/       # RecommendationCarousel, RecommendationCard
├── hooks/useRecommendations.ts
└── lib/                              # API + eventos leves de mudança da biblioteca
alexandria-frontend/e2e/               # suíte proposta + configuração Playwright
```

**Structure Decision**: adicionar um módulo funcional de recomendação nos pacotes existentes, sem microserviço ou novo módulo Maven. Não criar CRUD genérico de recomendações; o leitor só consulta e usa a inclusão já existente na biblioteca.

## Phase 0 — Research

Concluída em [research.md](research.md): fatos do checkout, algoritmo, frequência, armazenamento, consistência, riscos e alternativas. Pesquisa de backend e frontend realizada por agentes somente de leitura, conforme a skill.

## Phase 1 — Design

### Fluxo diário

1. Dispatcher diário e recuperação de reinício percorrem leitores por ID, sem carregar todos em memória. Leitores sem sinais positivos podem receber estado vazio sem criar trabalho de ranking.
2. Data capturada ao criar PENDING antes da fila e mantida imutável. Antes de processamento, claim curto de `(user_id, reference_date)` com lease e token aleatório. `READY` não é reconstruído no mesmo dia. Concorrência perde o claim sem duplicar publicação.
3. Uma varredura consistente do catálogo, em lotes por `id > lastId` de 500 registros, forma projeção compacta completa para a execução. Todos os lotes/autores usam a mesma transação read-only com REPEATABLE_READ explícito. Autores são lidos em lote; não paginar fetch join de coleção. Reusar uma única projeção entre trabalhadores do mesmo processo/data, preservando handles antigos na virada sem cedê-los à nova data.
4. Ler todos os vínculos do leitor, inclusive todas as páginas. Construir mapa de sinais e conjunto de exclusão; calcular a afinidade para cada livro digital disponível da projeção. Heap de 30 reduz memória da seleção, sem limitar o universo avaliado.
5. Publicar os até 30 itens em transação curta, verificando lease/token e tornando o cabeçalho `READY` atomicamente. Revalidar exclusões no momento da consulta; uma inclusão durante o cálculo não pode expor o livro na resposta.
6. Manter seleção atual e a última anterior pronta por leitor; eliminar histórico excedente em lotes somente após sucesso. Tentativas falhas e interrompidas têm retenção de 7 dias, sem apagar lease ativo ou a última seleção pronta.

### Fluxo do Painel

`GET /recommendations` retorna até 30 itens prontos com anti-join da biblioteca e disponibilidade atual. A ordem de decisão é: (1) reutilizar READY de hoje, inclusive vazia, aplicando exclusões atuais; (2) sem READY de hoje, verificar o perfil atual antes de qualquer fallback; sem sinal positivo, responder EMPTY/NO_PROFILE, selection=null e items=[], sem servir seleção antiga ou iniciar ranking; (3) com sinal positivo, solicitar/retomar a geração diária e usar a última READY como STALE enquanto necessário, ou 202 sem anterior; falha sem anterior retorna 503. Mudanças intradiárias de perfil não invalidam uma READY de hoje: influenciam somente a próxima seleção. O caminho NO_PROFILE não publica READY e permite a primeira geração do dia se surgirem sinais positivos depois. Recarregar ou navegar não repõe a seleção.

Frontend guarda apenas esses 30 itens e um índice circular. Controles de avançar/voltar e gesto de rolagem; sem autoplay. Ao incluir com sucesso: retirar todas as ocorrências, abortar/desconsiderar GETs anteriores e revalidar. Falha não confirma inclusão; 409 indica livro já existente e também o retira. Recarregar não repõe a seleção. Exclusão é por pertencimento atual: se o leitor posteriormente remover o livro da biblioteca, ele pode reaparecer na mesma seleção, sem novo ranking.

### Consistência e operação

- Executor separado, default 2 trabalhadores e fila de 100 tarefas; dispatcher pagina e respeita capacidade, sem descartar trabalho quando a fila enche. Reconciliador busca faltantes e leases expirados a cada 5 minutos, com backoff de falhas e limite de 3 tentativas automáticas por data. Nova tentativa manual retoma a mesma seleção pendente/falha, nunca reconstrói `READY`.
- Lease inicial de 5 minutos, renovado a cada 60 segundos somente com BUILDING, token coincidente e lease ainda válido pelo UTC do banco; relógio de lease usa timestamp UTC do banco. Publicação exige token ainda válido. Reinício não deixa o dia sem geração.
- Heartbeat usa scheduler dedicado. Os gatilhos cron/startup/recovery apenas submetem e retornam; descoberta e espera por capacidade usam executor de controle separado do pool de ranking (um controlador, gatilhos coalescidos por data, sem fila ilimitada). Não aguardar capacidade dentro do scheduler nem de um worker do mesmo pool. Validar heartbeat positivo e menor que lease; cancelá-lo em finally e encerrar todos os executores no shutdown. T030 testa renovação com workers/fila ocupados e encerramento sem thread órfã.
- Snapshot de catálogo inicializa apenas quando há trabalho; falha de leitura não produz seleção parcial. Tamanho/tempo/heap são medidos. Evitar montar cópia completa por usuário.
- Sem dependência de cache Redis ou cursor assinado: seleção curta persistida elimina essa complexidade.
- Cada instância pode executar o dispatcher; unicidade/lease no MySQL coordenam o trabalho. Uma projeção por instância/executação é aceitável, mas precisa ser contabilizada ao escalar réplicas.
- Métricas: duração e quantidade de gerações, leitores pendentes/falhos, retries, idade da última seleção, livros analisados/selecionados, tempo de GET e tamanho da projeção. Usar Actuator/Micrometer existentes; sem userId como rótulo de métrica e sem biblioteca, avaliações, JWT ou lista de recomendações em logs.
- Aviso operacional do MVP: Verificar a cada 5 minutos se há gerações PENDING/BUILDING/FAILED sem READY cuja pendência dura mais que a janela configurável, default 60 minutos. A idade começa em operational_pending_since do header do dia, inicialmente igual a created_at e reiniciado ao reativar após dormência; não usar simplesmente a meia-noite para leitores recém-chegados. Emitir WARN agregado com referenceDate, quantidade de leitores pendentes e idade da pendência mais antiga, sem IDs pessoais, títulos, assuntos, avaliações ou tokens. Limitar a uma emissão por hora por instância/data de referência, sem suprimir a primeira ocorrência elegível; com a fila normalizada, não emitir aviso. Reinício pode reiniciar o limitador local. Notificações externas ficam fora do MVP.
- Protocolo de SC-006: Medir no navegador em build de produção, com seleção pronta, cinco acessos de aquecimento e pelo menos 100 navegações válidas ao Painel no mesmo perfil de execução. Início: navigation.startTime da navegação completa para /dashboard; fim: marca no navegador após os cartões ou o estado vazio final estarem visíveis e duas requestAnimationFrame consecutivas permitirem sua pintura. Não encerrar ao receber o GET, ao mostrar skeleton, GENERATING ou somente o título da seção. Registrar amostras individuais em milissegundos; calcular p95 pelo método nearest-rank (posição ceil(0.95*N) na lista ordenada), sem remover amostras lentas. Falhas de rede/401/5xx ou timeout não são amostras válidas, mas devem ser reportadas separadamente e impedem considerar o ensaio aprovado até resolução/reexecução; não as esconder do denominador operacional. Registrar navegador/versão, máquina, viewport, rede, cache, aquecimento e leitores simultâneos; não misturar perfis diferentes no mesmo p95. Tempo de geração diária é medido separadamente.

### Sequência de implementação para decomposição posterior

Configurar Vitest para incluir somente src/**/*.test.{ts,tsx} (ou excluir e2e preservando defaults), e Playwright com testDir ./e2e, evitando executar testes de navegador no jsdom.

Modelo/pontuação e fixtures → Flyway e adapters/leases → geração diária e recuperação → GET filtrado/contrato → carrossel/inclusão/eventos → concorrência/E2E/desempenho. Atualizar `ARCHITECTURE.md` e `HOW_TO_ADD_NEW_DOMAIN.md` quando o código for implementado; não documentar componentes propostos como existentes.

### Rastreabilidade

| Requisito | Desenho/validação |
| --- | --- |
| FR-001, 007, 008, 013, 015 | contrato do Painel, cartões acessíveis, falhas independentes e E2E |
| FR-002 a 005, 012, 014 | projeções completas, pontuação pura, evidências e autenticação |
| FR-006, 010, 017 | geração diária top-30, publicação atômica, lease/retomada |
| FR-009, 016 | exclusão atual, remoção de respostas antigas, ciclos locais |
| FR-011 | estados vazios sem fallback popular |
| SC-001 a 005, 008, 009 | regras, contrato, concorrência e fluxo de biblioteca |
| SC-006 | ensaio de desempenho de quickstart.md |
| SC-007 | gate humano dispensado pelo usuário durante a implementação; registrar decisão em T051 sem alegar teste realizado |

## Complexity Tracking

Não há violação de constituição declarada. A persistência e o lease acrescentam complexidade necessária: uma seleção diária não deve ser recalculada por recarga/reinício nem publicada parcialmente. Uma lista somente na memória ou um cron sem coordenação não atende a esses requisitos. Os novos dados são derivados; não alteram tabelas nem regras de leitura/biblioteca existentes.

## Refinamento consolidado após rodada crítica 1

Contratos detalhados: [geração](contracts/daily-generation.md), [Painel](contracts/dashboard.md), [modelo](data-model.md).

- Data imutável no PENDING; não iniciar claims antigos após meia-noite. Worker iniciado pode publicar na data original; recuperação processa hoje.
- AUTO (GET/cron/recovery) respeita três claims por dia; MANUAL só por POST explícito após cooldown. automatic_attempt_count separa orçamento de attempt_count diagnóstico.
- Revalidar perfil no worker antes do catálogo. Sem sinais, liberar PENDING dormente sem READY/falha nem consumo AUTO; novos sinais reativam.
- Snapshot imutável por sessão compartilhada no processo e referenceDate com handles; single-flight e liberação só após término do proprietário, tarefas registradas e handles. O proprietário mantém sessão entre ondas; workers tardios aderem somente à sessão da mesma data. Nova data abre sessão própria sem invalidar os consumidores anteriores; medir pico de memória da sobreposição.
- API informa generationStatus=IDLE/PENDING/BUILDING/FAILED/READY/NO_PROFILE para orientar retry. STALE aceita zero itens e permite verificar conclusão por GET.
- Expor Retry-After no CORS efetivo da segurança e validar leitura entre origens no navegador; no-store em GET/POST e erros controlados.
- Um cartão visível, passo unitário, indicador X de N; ocultos sem foco. Anterior pode rever e item único pode se reapresentar.
- Razões históricas e data da seleção permanecem na API; a interface não as exibe após a revisão visual de 2026-10-30. NO_PROFILE não presume biblioteca vazia.

## Fechamento da rodada crítica 2

A API declara generationStatus em properties e schema dedicado RecommendationUnavailableResponse exige generationStatus/retryAfterSeconds no 503. Retenção inclui BUILDING antigo expirado; WARN conta somente pendências de hoje com perfil positivo, excluindo PENDING dormente e dias anteriores. T016/T034/T035 validam esses casos. A aprovação desta revisão é documental; implementação, desempenho e utilidade real ainda exigem as validações previstas.

## Revisão independente com agentes sem histórico

Lease expirado é normalizado atomicamente para FAILED mesmo após esgotar tentativas AUTO, tornando possível retry MANUAL após cooldown baseado em failed_at. Não responder ALREADY_RUNNING para lease vencido. A sessão de catálogo possui proprietário por execução que mantém referência durante fila/intervalos entre ondas; construção continua preguiçosa e liberação aguarda fim de todas as tarefas/handles. Indisponibilidade definitiva de livro durante inclusão retira cartão sem reposição, distinguida por erro BOOK_NOT_FOUND ou revalidação autoritativa. Contratos de geração/Painel e tarefas detalham os testes.
