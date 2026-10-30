# Prontidão para implementação — recomendações no Painel

Data: 2026-10-30. Branch: `feature/002-recomendacoes-painel-wip`. Base do checkout: `4e3eae3`, com refinamentos locais ainda sem commit. Esta avaliação cobre preparação, não aceite da funcionalidade.

Registro histórico da preparação. Durante a implementação posterior, o usuário dispensou SC-007/T051 como aceite humano obrigatório e preferiu iterar depois; referências abaixo a participantes descrevem o planejamento anterior e foram substituídas pela decisão registrada na [spec.md](spec.md). Progresso atual em [validation-results.md](validation-results.md).

## Decisão e próximo passo

O fluxo já passou por specify → clarify → plan → tasks → analyze, seguido das revisões registradas em [review-iterations.md](review-iterations.md). A etapa seguinte é `$speckit-implement`, começando por T001/T002 e pelo checkpoint MySQL da base. Não há necessidade de gerar novamente o plano ou as 52 tarefas.

O planejamento está pronto para implementação incremental: não foram encontrados requisitos sem cobertura nem esclarecimentos de produto bloqueantes. As validações de comportamento, capacidade e usabilidade permanecem obrigatórias nas tarefas correspondentes. Não há garantia de ausência de riscos antes de executar essas validações.

Não foi implementado código da funcionalidade nesta preparação. As 52 tarefas continuam desmarcadas; as verificações de ambiente abaixo são evidência parcial para T001, sem afirmar que todos os seus passos foram concluídos. Não houve commit, push, deploy ou migração em banco da aplicação.

## Análise e correções aplicadas

| ID | Categoria / gravidade | Local e problema de entrada | Fechamento documental |
| --- | --- | --- | --- |
| E1 | Dependência / alta | Testcontainers 1.19.0 falhou na conexão padrão com Docker 29.6.2, embora Docker CLI funcionasse | Probe reproduzível com api.version=1.44 executou MySQL/Flyway; opção propagada ao Surefire comprovada; quickstart/T001/T012 atualizados. Compatibilidade limitada ao ambiente ensaiado. |
| U1 | Subespecificação / alta | T011/T032 não separavam explicitamente heartbeat, descoberta com backpressure e scheduler | Plano/contrato/T011 definem scheduler de heartbeat dedicado e executor de controle; T030 cobre saturação e shutdown. Comportamento real ainda depende desses testes. |
| U2 | Subespecificação / média | Sessão ativa do catálogo podia ser interpretada como compartilhável entre datas | Sessão por processo/referenceDate, sem reutilizar ontem para hoje; handles antigos preservados; T021 cobre virada e pico de memória. |
| I1 | Inconsistência / média | T052 apontava para feature/002-recomendacoes-painel | Corrigido para feature/002-recomendacoes-painel-wip. |
| U3 | Clareza / baixa | research.md definia maior peso do motivo sem resolver empate entre características | Assunto por chave normalizada crescente, autor por ID crescente e evidência por bookId crescente; T015/T020 cobrem ordem de entrada diferente. |

Reforço de precisão de T008: todos os lotes do catálogo e autores na mesma transação read-only REPEATABLE_READ explícita, com cenário de alteração concorrente; não depender do isolamento default do servidor.

Resultado após correções: nenhum achado crítico/alto documental conhecido em aberto. Os cinco achados acima têm encaminhamento e cobertura; isso não prova o funcionamento do código futuro. Não existe constituição nem arquivo de hooks de extensão neste checkout. Não foram importadas regras de outro projeto.

## Cobertura dos artefatos

26 requisitos/critérios (17 FR e 9 SC), 52 IDs de tarefa únicos e nenhuma tarefa marcada. Cobertura documental: 26/26. Não foram detectados ciclos nas dependências explícitas ou nos pré-requisitos por fase/história. Tarefas de setup, fixtures e documentação sustentam as histórias e os critérios transversais; não constituem escopo de produto adicional. Nenhuma duplicação bloqueante encontrada.

| Requisitos/critérios | Tarefas principais |
| --- | --- |
| FR-001, FR-007, FR-008, FR-015 | T025–T029, T036–T037, T042–T045 |
| FR-002, FR-003, FR-004, FR-005, FR-012 | T008–T009, T013–T015, T018–T022 |
| FR-006, FR-010 | T005–T007, T020–T024, T030, T032–T037 |
| FR-009, FR-016 | T010, T031, T036–T045 |
| FR-011, FR-013 | T016–T017, T023–T029, T030–T037 |
| FR-014 | T010, T016–T017, T025, T027, T039, T044, T047 |
| FR-017 | T005–T007, T012, T022–T024, T030, T032–T035 |
| SC-001, SC-002, SC-003, SC-004 | T012–T016, T020–T025, T029–T031, T037 |
| SC-005, SC-008, SC-009 | T016–T017, T030–T045, T047–T048 |
| SC-006 | T049–T050 |
| SC-007 | T045, T051 |

Fonte operacional: [tasks.md](tasks.md). A tabela de cobertura e o grafo são conferidos pelo script documental; condições textuais adicionais continuam exigindo leitura das tarefas.

## Evidências executadas nesta preparação

Ambiente local Windows/PowerShell, Java Temurin 17.0.19, Node 24.18.0, npm 11.16.0 e Docker cliente/servidor 29.6.2. MySQL ensaiado: 8.0.32 em container novo, com as dependências do pom existente. Não foram usadas instâncias/volumes da aplicação ou dados de produção.

| Verificação | Resultado observado | Limite |
| --- | --- | --- |
| check-prerequisites.ps1 -Json -RequireSpec -RequireTasks -IncludeTasks | Feature 002 resolvida; artefatos obrigatórios presentes | Estrutura, não aprovação semântica |
| mvnw.cmd test | 296 testes, zero falhas/erros/ignorados | Suíte existente; integração atual usa H2, não recomendações |
| npm test | 12 arquivos, 30 testes aprovados | Suíte existente, sem feature nova |
| npm run lint | Zero erros; sete avisos existentes | Avisos não corrigidos fora do escopo |
| npm run build | Build de produção concluído | Não mede SC-006 nem prova E2E |
| Probe Testcontainers sem opção de API | Falhou ao obter ambiente Docker válido | Falha real que motivou E1 |
| Probe com api.version=1.44 | MySQL iniciado; quatro migrações V0/V001/V002/V3 aplicadas e validadas; sete tabelas incluindo histórico Flyway | Banco vazio descartável; não valida V4 ainda inexistente, Hibernate completo ou upgrade de banco legado |
| mvnw.cmd -Dapi.version=1.44 -Dtest=UserBooksStatusTest test | Seis testes aprovados; propriedade api.version=1.44 presente no XML Surefire | Prova propagação da configuração, não teste MySQL da feature |
| Validador documental | 26/26 requisitos cobertos, 52 tarefas únicas, sem ciclos; links locais presentes; nove referências resolvidas e nove schemas compilados; 14 exemplos sintéticos aceitos e 18 inválidos rejeitados, além do exemplo NO_PROFILE do contrato aceito | Não é validação completa OpenAPI; combinações semânticas de estados e respostas runtime são T016/T047 |

## Reprodução

A partir da raiz do repositório, com dependências frontend instaladas:

```powershell
node specs/002-recomendacoes-painel/scripts/validate-preimplementation.cjs
./specs/002-recomendacoes-painel/scripts/check-mysql-toolchain.ps1
```

O primeiro comando usa js-yaml e AJV presentes no lockfile frontend. Confere required/properties, resolução de referências sem ciclos, compila os schemas com nullable adaptado do OpenAPI 3.0 e verifica exemplos positivos/negativos. Não verifica toda a especificação OpenAPI nem prova que estados incompatíveis sejam rejeitados pelo servidor. Não faz rede ou escrita nos artefatos.

O segundo usa o classpath Maven e Java source launcher, cria container sem volume persistente e o encerra em finally/close com a limpeza Testcontainers habilitada. Pode baixar dependências/imagens ausentes. Usa opção api.version apenas na JVM do probe; não altera Docker ou configuração global. Docker deve estar iniciado. O parâmetro -DockerApiVersion permite outro valor quando for necessário validar outro ambiente. Falha do probe é falha do gate, nunca teste ignorado.

## Critérios de avanço e riscos restantes

| Momento | Evidência obrigatória | Tratamento de falha |
| --- | --- | --- |
| T001/T002 | Dependências, configuração local sem segredos, documentação Next instalada lida antes de código frontend e descoberta Vitest/Playwright separada | Corrigir setup antes de integrar histórias; baseline não conclui esses passos automaticamente |
| Antes de integrar histórias, T012 | V4, constraints, claims/tokens, rollback/publicação e filtros em MySQL real; banco vazio e upgrade V3→V4 preservando usuários/biblioteca semeados | Não substituir por mocks/H2 nem avançar com testes ignorados |
| US1/US3, T015/T021/T030 | Ordenação de referência, motivos determinísticos, snapshot entre ondas/datas e heartbeat sob saturação | Corrigir o comportamento antes do checkpoint |
| US2, T038–T045 | Concorrência de inclusão e ausência de reintrodução por GET atrasado; acesso entre contas, teclado/celular e erros de livro indisponível | Manter história incompleta enquanto falhar |
| T047/T048 | Contratos runtime, CORS real, no-store, Retry-After, testes completos e E2E com backend isolado | Ausência de suíte/Docker não conta como aprovação |
| T049/T050 | Capacidade sobre catálogo integral e p95 no navegador, incluindo pico de sessões entre datas | Medir e otimizar; nunca truncar catálogo para aprovar |
| T051 | Pelo menos cinco participantes reais para SC-007 e avaliação qualitativa do top-5 | Disponibilidade não confirmada nesta preparação; deixar pendente até haver participantes, sem substituir por agentes |
| T052 | Todos os critérios necessários comprovados e pendências descritas | Não declarar feature completa com aceite obrigatório pendente |

Sem nova infraestrutura externa, fallback popular, reposição intradiária, alteração de assinatura ou cadastro local. Os defaults de algoritmo/UI continuam sujeitos à avaliação prevista com leitores. Preparação documental concluída; aprovação para produção depende das evidências acima.
