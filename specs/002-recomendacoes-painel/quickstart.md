# Guia de validação: recomendações diárias

Este guia descreve como validar **após implementar** os componentes de plan.md. Não há endpoint, migração V4 ou suíte de recomendações implementados nesta etapa. Contratos: [API](contracts/recommendations.openapi.yaml), [Painel](contracts/dashboard.md), [job](contracts/daily-generation.md). Modelo: [data-model.md](data-model.md).

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

Na implementação, criar classes de recomendação sob os diretórios de teste existentes. Comandos previstos, válidos depois que essas classes/configurações existirem:

```bash
cd alexandria-backend
./mvnw test
./mvnw -Dtest=RecommendationPersistenceIntegrationTest test
```

O segundo comando deverá executar Testcontainers/MySQL com Flyway habilitado, incluindo V4 e as restrições novas. H2 não prova comportamento de leases, plano de consulta, publicação concorrente ou custo no MySQL. Confirmar a cadeia de migrations existente contra schema de desenvolvimento; não aplicar V4 em ambiente compartilhado nesta fase de planejamento.

Frontend:

```bash
cd alexandria-frontend
npm run lint
npm test
npm run build
npx playwright install chromium
npm run test:e2e
```

A configuração e os testes Playwright ainda precisam ser criados na implementação: testDir ./e2e. Ajustar Vitest para incluir somente src/**/*.test.{ts,tsx} ou excluir e2e preservando os defaults, evitando carregar Playwright no jsdom. Não interpretar script existente sem suíte como comprovação E2E. Reusar helpers Vitest de src/test-utils; não assumir @testing-library/react instalado.

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

Com backend implementado, usar JWT de teste válido em variável de ambiente; não colar token em documentação nem logs:

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

Com pelo menos cinco participantes, ao menos quatro devem navegar, abrir detalhe e adicionar sem orientação, e explicar o motivo da sugestão. Testar 390px, desktop, teclado e leitor de tela quando disponível; ações visíveis sem hover, foco preservado após exclusão e aviso de seleção antiga compreensível.

Registrar resultados, comandos, ambiente e limitações na implementação. Este planejamento não afirma que testes ou metas já passaram.

## Protocolo de medição no navegador (SC-006)

Medir no navegador em build de produção, com seleção pronta, cinco acessos de aquecimento e pelo menos 100 navegações válidas ao Painel no mesmo perfil de execução. Início: navigation.startTime da navegação completa para /dashboard; fim: marca no navegador após os cartões ou o estado vazio final estarem visíveis e duas requestAnimationFrame consecutivas permitirem sua pintura. Não encerrar ao receber o GET, ao mostrar skeleton, GENERATING ou somente o título da seção. Registrar amostras individuais em milissegundos; calcular p95 pelo método nearest-rank (posição ceil(0.95*N) na lista ordenada), sem remover amostras lentas. Falhas de rede/401/5xx ou timeout não são amostras válidas, mas devem ser reportadas separadamente e impedem considerar o ensaio aprovado até resolução/reexecução; não as esconder do denominador operacional. Registrar navegador/versão, máquina, viewport, rede, cache, aquecimento e leitores simultâneos; não misturar perfis diferentes no mesmo p95. Tempo de geração diária é medido separadamente.

Criar um ensaio Playwright opt-in em alexandria-frontend/e2e/recommendations-performance.spec.ts. Usar npm run build e npm run start, com seleções READY previamente geradas, JWT de teste válido e carga de 50 leitores consultando o backend em paralelo conforme o ensaio de capacidade. Fixar Chromium e viewport 1280x800 para a primeira série; registrar a política de cache (cache normal do navegador, respostas personalizadas no-store) e a rede local sem throttling, ou as condições medidas se o ambiente for remoto. Aquecer com cinco navegações completas e coletar ao menos 100 medições depois; outros perfis são séries separadas.

Instalar a observação de renderização antes da navegação, usando page.addInitScript e marcas de Performance no navegador. Finalizar somente quando a seção tiver cartões visíveis ou EMPTY final, com skeleton/GENERATING ausentes, após duas requestAnimationFrame; não usar somente o tempo de page.goto ou waitForResponse. Coletar navigation.startTime e a marca final do mesmo documento. Guardar todas as durações, aquecimento separado, falhas/timeouts e o p95 em validation-results.md. Com 100 amostras válidas, p95 é o valor da 95ª amostra ordenada (índice94), não a média dos tempos.

## Aviso operacional de atraso

Verificar a cada 5 minutos se há gerações PENDING/BUILDING/FAILED sem READY cuja pendência dura mais que a janela configurável, default 60 minutos. A idade começa em created_at do header do dia, registrado na primeira solicitação/constatação de trabalho; não usar simplesmente a meia-noite para leitores recém-chegados. Emitir WARN agregado com referenceDate, quantidade de leitores pendentes e idade da pendência mais antiga, sem IDs pessoais, títulos, assuntos, avaliações ou tokens. Limitar a uma emissão por hora por instância/data de referência, sem suprimir a primeira ocorrência elegível; com a fila normalizada, não emitir aviso. Reinício pode reiniciar o limitador local. Notificações externas ficam fora do MVP.

Validar com tempo controlado em RecommendationMetricsTest: pendência nova abaixo do limiar não avisa; pendência acima do limiar emite WARN com os três campos agregados; nova checagem em cinco minutos não repete; após uma hora pode repetir; nenhuma READY anterior é descartada; normalização da fila interrompe avisos. Revisar logs para confirmar ausência de dados pessoais. Não alterar relógio do host nem esperar uma hora real.
