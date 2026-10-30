# Guia de validação: recomendações diárias

Este guia descreve a implementação existente e seus comandos de validação local. Os endpoints, a migração V4 e as suítes de recomendações estão implementados; resultados efetivamente obtidos e limites ficam em [validation-results.md](validation-results.md). Contratos: [API](contracts/recommendations.openapi.yaml), [Painel](contracts/dashboard.md), [job](contracts/daily-generation.md). Modelo: [data-model.md](data-model.md).

## Preparação conferida antes da implementação

Evidências e critérios de avanço: [implementation-readiness.md](implementation-readiness.md). O comando documental pode ser executado agora, a partir da raiz, com node_modules existente ou após npm ci em alexandria-frontend:

```powershell
node specs/002-recomendacoes-painel/scripts/validate-preimplementation.cjs
./specs/002-recomendacoes-painel/scripts/check-mysql-toolchain.ps1
```

O probe usa container MySQL novo e descartável, sem volumes/banco da aplicação, e aplica a cadeia V0/V001/V002/V3/V4. A concorrência e o upgrade com dados são verificados adicionalmente em RecommendationPersistenceIntegrationTest. No ambiente local validado (Docker 29.6.2 e Testcontainers 1.19.0), a autodetecção padrão falhou e a opção de JVM api.version=1.44 resolveu a conexão. O probe já a passa à JVM. Não desabilitar Ryuk/Testcontainers nem alterar o daemon para mascarar falhas. A opção não muda configuração global.

No PowerShell, usar mvnw.cmd para os comandos Maven deste guia. Quando executar os futuros testes MySQL neste ambiente, usar a propriedade como argumento único entre aspas, preservando JaCoCo:

```powershell
cd alexandria-backend
./mvnw.cmd '-Dapi.version=1.44' test
./mvnw.cmd '-Dapi.version=1.44' '-Dtest=RecommendationPersistenceIntegrationTest' test
```

Essa classe está implementada em T012; o probe também pode ser usado isoladamente. A opção vale também para a suíte completa quando incluir Testcontainers. Em shell Unix, o equivalente é ./mvnw -Dapi.version=1.44 test. A propagação da propriedade ao Surefire foi conferida em um teste existente. Não substituir argLine para passar a opção, pois isso pode perder a instrumentação de cobertura.

## Pré-requisitos e execução

- Java 17, Docker com MySQL de desenvolvimento/teste, Node compatível com Next 16 (>=20.9), npm e variáveis locais já descritas no README. Não usar dados ou credenciais de produção.
- Executar comandos abaixo a partir da raiz do Alexandria. Configurar JWT_SECRET e banco pelos mecanismos existentes, sem gravar segredos no Git.

```bash
cd alexandria-backend
java -version
./mvnw -version
docker compose up -d mysql
./mvnw test
./mvnw spring-boot:run
```

Em outro terminal, a partir da raiz. Configure NEXT_PUBLIC_API_URL=http://localhost:8080 em alexandria-frontend/.env.local (arquivo local ignorado), antes de iniciar o frontend. O README referencia .env.local.example, mas esse arquivo não existe no checkout; não depender dessa cópia. Não gravar segredos nem .env.local no Git.

Comandos:

```bash
cd alexandria-frontend
node --version
npm ci
npm run dev
```

Antes de escrever o código frontend, ler o guia relevante de `node_modules/next/dist/docs/`, conforme AGENTS do módulo. Não alterar o lockfile sem necessidade.

## Testes e verificações

As classes de recomendação estão nos diretórios de teste do módulo. No PowerShell usar mvnw.cmd e, neste ambiente Docker, acrescentar -Dapi.version=1.44 conforme os exemplos acima:

```bash
cd alexandria-backend
./mvnw test
./mvnw -Dtest=RecommendationPersistenceIntegrationTest test
```

O segundo comando executa Testcontainers/MySQL com Flyway habilitado, incluindo V4, restrições novas e upgrade V3→V4 com dados semeados. H2 não prova comportamento de leases, plano de consulta, publicação concorrente ou custo no MySQL. A suíte cria bancos descartáveis; não aponta para bancos compartilhados ou de produção.

Frontend:

```bash
cd alexandria-frontend
npm run lint
npm test
npm run build
npx playwright install chromium
npm run test:e2e
```

Vitest inclui somente src/**/*.test.{ts,tsx}; Playwright usa testDir ./e2e. O globalSetup padrão compila o backend, cria MySQL descartável e inicia RecommendationE2EServer na porta 18080; o webServer inicia Next na porta 3100. O teardown encerra ambos e remove os arquivos temporários com JWTs. É necessário Docker iniciado e Chromium instalado. E2E_FIXTURE_DIR permite fornecer um harness já iniciado, que então não é encerrado pela suíte. Não há endpoint público de clock/seed. Os helpers Vitest usam React act/createRoot em src/test-utils, sem nova dependência de testing-library/react.

## Cenários de ponta a ponta

Criar fixtures isoladas por teste, usando repositórios/seed de teste, sem endpoint público de seed. Usar Clock de teste para avançar dia e disparo do job em teste, sem esperar o cron real e sem alterar relógio do host.

| Cenário | Resultado esperado |
| --- | --- |
| Catálogo >30 com candidatos fortes no início/meio/fim | resultado igual ao top-30 da ordenação integral de referência; nenhum corte por página |
| Biblioteca >uma página | todos os sinais considerados e todos os livros existentes excluídos |
| done nota5, reading, toread e done nota3 equivalentes | prioridade dos pesos 4,3,2,1; nota0 é válida e não gera sinal positivo |
| Idioma/popularidade coincidem, sem autor/assunto positivo | candidato não entra; sem fallback popular |
| Apenas 7 candidatos compatíveis | só 7 itens; fim volta ao primeiro por avanço, sem rede |
| Uma seleção com 30, navegar mil voltas | memória/DOM limitados a até30; sem GET por volta e sem novo ranking |
| Adicionar livro no Painel e receber GET antigo atrasado | todas as ocorrências somem; resposta antiga ignorada |
| Duas abas adicionam mesmo livro | um vínculo; 409 na duplicada e cartão retirado |
| Adicionar todos os selecionados | EMPTY até próxima seleção, sem reposição |
| Avaliar/mudar status depois de READY | mesmo conjunto/ordem naquele dia, salvo exclusões; próximo dia usa novo perfil |
| Remover da biblioteca livro que ainda está na seleção | pode reaparecer por filtro atual, sem nova geração |
| Atualização diária sem mudança no perfil/base | seleção pode ser igual; não prometer30 títulos diferentes |
| Trocar usuário/logout com GET pendente | nenhum cartão da conta anterior aparece |
| Falha do job com seleção anterior | última válida STALE, data e aviso visíveis, exclusões atuais |
| Primeiro acesso sem seleção atual | 202 enquanto gera; polling limitado; READY posterior sem recálculo na recarga |
| Sem READY de hoje e sem perfil positivo, inclusive com seleção antiga | EMPTY/NO_PROFILE com Explorar; não servir STALE nem varrer catálogo; novo sinal pode iniciar a primeira geração do dia |
| READY de hoje e perfil alterado intradiariamente | reutilizar a seleção com exclusões atuais; no dia seguinte, NO_PROFILE prevalece se os sinais continuarem ausentes |
| Snapshot READY de zero itens | EMPTY/NO_MATCHES; não regenerar antes do próximo dia |
| PENDING abandonado, dois workers, lease expirado, crash e reinício | uma READY por data, sem publicação parcial ou worker antigo sobrescrever |
| Hosts em fusos diferentes e virada de dia | mesma referenceDate America/Sao_Paulo e correto rollover |

## Verificação manual da API

Com backend iniciado, usar JWT de teste válido em variável de ambiente; não colar token em documentação nem logs:

```bash
curl -i -H "Authorization: Bearer ${ALEXANDRIA_TEST_TOKEN}" http://localhost:8080/recommendations
curl -i -X POST -H "Authorization: Bearer ${ALEXANDRIA_TEST_TOKEN}" http://localhost:8080/recommendations/generation
```

Conferir Cache-Control: no-store, até30 IDs distintos e nenhum da biblioteca atual. Repetir POST quando já READY não altera seleção nem generatedAt. Sem JWT ->401, sem assumir corpo uniforme do entry point existente. Retry antes do cooldown ->429/Retry-After, sem novo job. Cadastro e inclusão usam as rotas existentes, sem mudanças no gate de assinatura.

## Desempenho, banco e capacidade

Usar MySQL isolado com 100 mil livros, mil leitores, até mil vínculos por leitor e 50 leitores simultâneos consultando seleções prontas. São parâmetros de ensaio, não estimativas confirmadas de produção. Registrar hardware, heap, versão MySQL, tamanho de linhas/índices, total de vínculos, aquecimento e condições de acesso.

1. Medir separadamente construção da projeção completa, cálculo por leitor, publicação, limpeza e GET pronto; registrar quantidade exata de livros avaliados.
2. Medir a exibição no navegador com o protocolo detalhado abaixo: p95 <=2s com seleção pronta. Não confundir com tempo de GET nem de geração inicial.
3. Meta de engenharia do job: mil leitores em <=60 minutos no ambiente de referência. Registrar CPU, heap/RSS, pausas de GC, filas, I/O, locks/deadlocks e duração por leitor. Se falhar, otimizar projeções/algoritmo/capacidade antes de liberar; nunca reduzir silenciosamente o catálogo avaliado.
4. Conferir EXPLAIN do anti-join e da busca READY/claims. Verificar uso da unicidade user_books(user_id,book_id) e dos índices novos. Não manter lock durante CPU de ranking.
5. Depois da retenção, conferir <=30 itens por seleção e até duas READY por leitor. Para mil leitores, até60 mil itens históricos; pico transitório pode ser maior antes da limpeza.
6. Interromper job e reiniciar serviço: retomar leases e servir última seleção válida; confirmar que GETs não causam uma reconstrução da seleção pronta.

## Usabilidade e aceite

O usuário dispensou o teste com cinco participantes e a sessão qualitativa durante a implementação em 2026-10-30. Não são gates desta entrega e não devem ser relatados como testes aprovados. Validar 390px, desktop e teclado nas suítes automatizadas; ações visíveis sem hover, foco preservado após exclusão, justificativa histórica e aviso de seleção antiga continuam obrigatórios. Feedback com leitores pode orientar iterações futuras.

Os resultados, comandos, ambiente e limitações desta execução ficam em validation-results.md. Presença de código/teste não implica aprovação de uma meta; conferir a evidência correspondente.

## Protocolo de medição no navegador (SC-006)

Medir no navegador em build de produção, com seleção pronta, cinco acessos de aquecimento e pelo menos 100 navegações válidas ao Painel no mesmo perfil de execução. Início: navigation.startTime da navegação completa para /dashboard; fim: marca no navegador após os cartões ou o estado vazio final estarem visíveis e duas requestAnimationFrame consecutivas permitirem sua pintura. Não encerrar ao receber o GET, ao mostrar skeleton, GENERATING ou somente o título da seção. Registrar amostras individuais em milissegundos; calcular p95 pelo método nearest-rank (posição ceil(0.95*N) na lista ordenada), sem remover amostras lentas. Falhas de rede/401/5xx ou timeout não são amostras válidas, mas devem ser reportadas separadamente e impedem considerar o ensaio aprovado até resolução/reexecução; não as esconder do denominador operacional. Registrar navegador/versão, máquina, viewport, rede, cache, aquecimento e leitores simultâneos; não misturar perfis diferentes no mesmo p95. Tempo de geração diária é medido separadamente.

O ensaio Playwright opt-in está em alexandria-frontend/e2e/recommendations-performance.spec.ts. Usar npm run build e npm run start, com seleções READY previamente geradas, JWT de teste válido e carga de 50 leitores consultando o backend em paralelo conforme o ensaio de capacidade. Fixar Chromium e viewport 1280x800 para a primeira série; registrar a política de cache (cache normal do navegador, respostas personalizadas no-store) e a rede local sem throttling, ou as condições medidas se o ambiente for remoto. Aquecer com cinco navegações completas e coletar ao menos 100 medições depois; outros perfis são séries separadas.

Instalar a observação de renderização antes da navegação, usando page.addInitScript e marcas de Performance no navegador. Finalizar somente quando a seção tiver cartões visíveis ou EMPTY final, com skeleton/GENERATING ausentes, após duas requestAnimationFrame; não usar somente o tempo de page.goto ou waitForResponse. Coletar navigation.startTime e a marca final do mesmo documento. Guardar todas as durações, aquecimento separado, falhas/timeouts e o p95 em validation-results.md. Com 100 amostras válidas, p95 é o valor da 95ª amostra ordenada (índice94), não a média dos tempos.

## Aviso operacional de atraso

Verificar a cada 5 minutos se há gerações PENDING/BUILDING/FAILED sem READY cuja pendência dura mais que a janela configurável, default 60 minutos. A idade começa em operational_pending_since do header do dia, inicialmente igual a created_at e reiniciado ao reativar após dormência; não usar simplesmente a meia-noite para leitores recém-chegados. Emitir WARN agregado com referenceDate, quantidade de leitores pendentes e idade da pendência mais antiga, sem IDs pessoais, títulos, assuntos, avaliações ou tokens. Limitar a uma emissão por hora por instância/data de referência, sem suprimir a primeira ocorrência elegível; com a fila normalizada, não emitir aviso. Reinício pode reiniciar o limitador local. Notificações externas ficam fora do MVP.

Validar com tempo controlado em RecommendationMetricsTest: pendência nova abaixo do limiar não avisa; pendência acima do limiar emite WARN com os três campos agregados; nova checagem em cinco minutos não repete; após uma hora pode repetir; nenhuma READY anterior é descartada; normalização da fila interrompe avisos. Revisar logs para confirmar ausência de dados pessoais. Não alterar relógio do host nem esperar uma hora real.

## Casos acrescentados pela revisão crítica 1

- PENDING criado às 23h59 e claim após meia-noite: não iniciar ontem; solicitar hoje. Worker já iniciado pode terminar com data original.
- Cem GETs após três falhas AUTO não geram claim; POST explícito após cooldown pode tentar.
- Perfil perde sinais após PENDING: worker libera dormente sem READY/falha; novos sinais no mesmo dia reativam.
- Sobrepor cron/recovery/GET da mesma referenceDate: uma construção de snapshot por processo/data e nenhuma liberação enquanto existir consumidor. Datas diferentes não compartilham projeção.
- Browser em origem frontend distinta lê Retry-After; testar Access-Control-Expose-Headers na cadeia real de segurança e respeitar intervalo.
- STALE originalmente vazia ou filtrada até zero mantém pendência de hoje e ação GET para verificar; FAILED oferece POST apenas após cooldown.
- Um cartão visível em celular/desktop; ocultos fora da navegação Tab/leitura; Anterior revisita, um item se reapresenta, indicador usa itens atuais.
- NO_PROFILE com biblioteca vazia, notas 0–2 ou metadados insuficientes usa mensagem sem pressupor vazio.
- Justificativa permanece histórica após reavaliação/remoção da evidência; data vem de referenceDate, nunca data inventada da avaliação.
- A proposta original de T051 para relevância/interesse do top-5 e rejeições foi dispensada pelo usuário junto ao gate humano. Não inventar resultados de satisfação nem substituir leitores por agentes; registrar a dispensa e manter os testes automatizados dos fluxos.

## Casos finais da rodada 2

Validar OpenAPI: todo required deve estar declarado quando additionalProperties=false; 503 tem schema dedicado com generationStatus/retryAfterSeconds obrigatórios. Testar retenção de BUILDING de dia antigo com lease vencido após sete dias e preservação de lease ativo. WARN não conta datas antigas ignoradas nem PENDING sem sinais; cobrir esses cenários com tempo controlado.

Reativação após NO_PROFILE prolongado: operational_pending_since volta ao UTC atual do banco uma vez, não a created_at histórico; antes do novo limiar não há WARN, e polls não postergam esse início. Validar atomicidade do reset/reativação e contadores preservados.

## Validação da revisão independente adicional

- Após terceiro claim AUTO sofrer crash, expirar lease: GET deve retornar FAILED (503 ou STALE), sem quarto claim AUTO; POST após failed_at+60s recupera. Testar CAS contra heartbeat/publicação/novo claim.
- Com dois workers e seis leitores em ondas sincronizadas, proprietário mantém sessão enquanto tarefas aguardam; exigir uma construção integral e liberação somente ao fim, incluindo rejeição/cancelamento.
- Remover livro após exibição: BOOK_NOT_FOUND confirma retirada, GET revalida, foco e vazio permanecem coerentes, sem reposição. 404 sem código/5xx/rede não confirma indisponibilidade; GET autoritativo pode retirar.
- Compilar schemas com nullable adaptado e validar exemplos positivos/negativos de 503; verificar ciclos allOf, além de referências existentes.

## Casos da preparação final

- Alterar catálogo/autores entre lotes de leitura: transação única REPEATABLE_READ explícita preserva projeção consistente, inclusive se o default do banco for outro.
- Empatar pesos de assuntos/autores e variar ordem de Set/Map/consulta: o motivo mantém assunto de menor chave normalizada (String.compareTo) ou menor authorId; evidência mantém menor bookId.
- Atravessar meia-noite com handle antigo ativo e catálogo alterado: hoje usa sessão de hoje; ontem mantém seu handle até concluir. Medir pico de memória com sobreposição, sem invalidar consumidor em uso.
- Ocupar workers/fila e manter descoberta aguardando capacidade: heartbeat dedicado continua renovando; cron/recovery não bloqueiam o scheduler. Shutdown cancela heartbeat e encerra controle/workers, sem threads órfãs.

## Configuração implementada

| Propriedade | Default | Uso |
| --- | --- | --- |
| recommendations.zone | America/Sao_Paulo | Identidade diária explícita |
| recommendations.cron | 0 0 0 * * * | Gatilho diário |
| recommendations.recovery-delay-ms | 300000 | Reconciliação do dia, incluindo startup perdido |
| recommendations.retention-delay-ms | 3600000 | Limpeza em transações curtas |
| recommendations.metrics-delay-ms | 300000 | Verificação agregada de atraso |
| recommendations.lease | 5m | Lease pelo UTC do banco |
| recommendations.heartbeat | 60s | Scheduler próprio; deve ser positivo, >=1ms e menor que lease |
| recommendations.workers | 2 | Pool de ranking independente |
| recommendations.queue-capacity | 100 | Fila limitada, sem claim durante espera |
| recommendations.page-size | 100 | Descoberta e chunks de manutenção |
| recommendations.overdue-threshold | 60m | Limiar do WARN agregado |
| recommendations.jobs-enabled | true | Desabilita gatilhos automáticos em testes isolados, preservando GET/POST e worker |

São quatro pools de execução: ranking, controle/descoberta, manutenção e heartbeat. Cron/recovery apenas submetem gatilhos coalescidos e retornam. Orçamento de três claims AUTO e backoffs de 60s/300s são regras do repositório; retry MANUAL usa failed_at+60s. Não há flag/endpoint de reconstrução de READY.

O Service Worker deixa /recommendations e suas subrotas network-only e remove respostas privadas legadas dos caches na ativação. AuthProvider, dashboard e hook vinculam dados ao dono da sessão; requests/respostas antigos não devem reaparecer após troca de conta ou logout.

## Ensaio de capacidade opt-in

Na raiz do backend, sem outros builds/ensaios de carga na mesma máquina:

```powershell
./mvnw.cmd '-Dapi.version=1.44' '-Drecommendations.capacity=true' '-Dtest=RecommendationCapacityTest' test
```

O fixture cria 100 mil livros, mil leitores e um milhão de vínculos em MySQL novo. O teste dispara o dispatcher real, exige mil READY e 100 milhões de livros considerados, compara três leitores determinísticos com ordenação integral independente e mede 50 leitores concorrentes por MockMvc. Registra EXPLAIN, CPU, heap/RSS, GC, I/O e locks em target/recommendation-capacity/backend-results.json, incluindo sobreposição de duas projeções/datas e o pool Hikari efetivo. Essa latência MockMvc não é SC-006 nem inclui transporte HTTP real. O default da suíte comum não executa esse ensaio pesado.

Para executar toda a suíte backend incluindo o opt-in e obter resultado sem esse teste ignorado:

```powershell
./mvnw.cmd '-Dapi.version=1.44' '-Drecommendations.capacity=true' test
```

Na raiz do frontend, para SC-006, produzir build apontado ao backend isolado e iniciar produção em outro terminal:

```powershell
$env:NEXT_PUBLIC_API_URL='http://localhost:18080'
npm run build
npm run start -- --port 3100
```

Com produção iniciada, em outro terminal frontend:

```powershell
$env:E2E_BASE_URL='http://localhost:3100'
$env:E2E_PRODUCTION='1'
$env:RUN_RECOMMENDATION_PERFORMANCE='1'
npm run test:e2e -- recommendations-performance.spec.ts
```

O globalSetup inicia o backend/MySQL isolado; a ação capacity do harness usa o fixture integral e o motor real para preparar mil seleções. O browser executa cinco aquecimentos e 100 navegações completas, enquanto 50 leitores distintos consultam o backend com pausa de 500ms entre leituras. Amostras, falhas, carga e ambiente são salvos em test-results; consolidar os JSONs aprovados e seus limites na evidência da feature. Não executar builds, seed de outro ensaio ou carga adicional durante a série.

## Reinício e cancelamento

Os E2E reiniciam o contexto Spring, executores e provider mantendo MySQL e relógio; verificam id/generatedAt persistidos e retomada automática de BUILDING expirado na inicialização, sem GET intermediário que dispare geração. Isso é reinício real do serviço Spring na mesma JVM, não um ensaio de kill do processo do sistema operacional sob carga. Falhas de claim, rollback e token são testadas separadamente no MySQL.

O teste de cancelamento do provider mantém a construção compartilhada bloqueada e interrompe outro consumidor, que deve liberar seu handle antes de concluir a construção. A corrida de heartbeat usa barreiras na interrupção da thread para provar que callback antigo não interrompe a próxima tentativa no pool. Não cancelar a construção compartilhada por cancelar apenas um consumidor.
