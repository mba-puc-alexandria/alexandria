# Resultados da implementação — recomendações no Painel

Data: 2026-10-30. Branch: `feature/002-recomendacoes-painel-wip`. HEAD de base da validação: `4e3eae3`. Após esta validação, o usuário autorizou um único commit local do avanço; sem push ou deploy.

**Resultado: implementação concluída e critérios vigentes aprovados no ambiente local de referência.** As 52 tarefas estão concluídas; T051 registra a dispensa expressa do teste com participantes pelo usuário. Não houve teste humano nem se afirma satisfação comprovada.

## Evidências de aceite

| Verificação | Resultado |
| --- | --- |
| Backend completo, incluindo capacidade opt-in | **365 testes, zero falhas/erros/ignorados**, 7min24s |
| Frontend | **19 arquivos / 71 testes aprovados**, sem ignorados |
| TypeScript / lint / build | Aprovados; zero erros de lint e seis avisos de imagem preexistentes |
| Navegador funcional | **18/18 aprovados**, 2,4min, com autostart/teardown de MySQL, Spring e Next |
| SC-006: exibição no navegador | **p95 264,5ms**, meta <=2.000ms; 100 amostras válidas, cinco aquecimentos, 50 leitores concorrentes |
| Falhas no ensaio do navegador | Zero navegações com falha e zero falhas nas **2.235 leituras concorrentes** registradas |
| Meta de engenharia do job diário | Mil seleções em **197.918ms (3min17,918s)**, meta <=60min |
| Catálogo integral | 100.000 livros x 1.000 leitores = **100.000.000 livros considerados**, 1.000 READY |
| Contratos e rastreabilidade | 26 identificadores (25 ativos + SC-007 dispensado), 52 tarefas únicas, sem ciclos; nove schemas compilados |

Dados brutos: [capacidade backend](validation/backend-capacity.json) e [exibição no navegador](validation/browser-performance.json). O orquestrador recalculou o p95 ordenando as 100 amostras e selecionando a posição 95 (nearest-rank); conferiu o valor de 264,5ms, cinco aquecimentos, 50 leitores e ausência de falhas.

## Comportamento entregue

- Perfil calculado sobre a biblioteca completa, pesos de avaliação/status, afinidade determinística por assuntos/autores e justificativa histórica verificável.
- Seleção diária persistida de até 30 livros após considerar todo o catálogo, sem fallback popular ou reposição intradiária; carrossel local sem requisições por volta. A revisão visual posterior, descrita ao final, substitui o cartão único por grupos responsivos.
- Geração assíncrona com PENDING durável, fila limitada/coalescida, três claims AUTO por data, retry MANUAL, lease/heartbeat/CAS e publicação atômica. Recuperação, retenção e WARN agregado implementados.
- Exclusões atuais, inclusão sem duplicidade, tratamento de livro indisponível, cancelamento de respostas antigas e isolamento entre sessões/abas. Rotas privadas de recomendações ficam network-only no Service Worker.
- Teclado, 390px, desktop, foco após retirada, estados vazios/STALE/falha e demais seções do Painel preservados.

Referências: [spec](spec.md), [plano](plan.md), [tarefas](tasks.md), [contrato API](contracts/recommendations.openapi.yaml), [Painel](contracts/dashboard.md) e [geração](contracts/daily-generation.md).

## Cenários efetivamente verificados

MySQL real: migração completa V0/V001/V002/V3/V4; upgrade V3→V4 preservando usuários/biblioteca semeados; Hibernate validate; constraints; dois claims concorrentes; rollback com FK inválida no segundo item; orçamento AUTO/terceiro crash/retomada MANUAL; CAS contra heartbeat; dormência e reativação; leitura de catálogo/biblioteca com mais de 605 registros e alteração concorrente entre lotes; filtros READY/STALE; cascades e retenção. H2 não foi usado como prova dessas garantias.

Snapshot: construção única entre três ondas de dois workers/seis leitores, compartilhamento entre origens, sessões distintas por data, handles antigos preservados, erro de leitura e fingerprint estável. O teste de interrupção mantém o builder A bloqueado e exige que o consumidor B interrompido saia antes de liberar A, sem cancelar o future compartilhado ou deixar sessão pendente.

Execução: fila saturada não bloqueia heartbeat/observação; shutdown fecha registros enfileirados; perda de lease impede publicação; fila atravessando meia-noite não adquire claim antigo; worker iniciado antes da virada publica apenas sob a data original. Testes com barreiras cobrem callback tardio e a janela exata de Thread.interrupt, impedindo que heartbeat antigo afete uma thread já reutilizada.

Navegador: 17 casos da aplicação usam backend/MySQL reais, incluindo 30 selecionados entre 40 candidatos e inclusão dos 30 sem reposição, 409 real, BOOK_NOT_FOUND real do último cartão, GET antigo atrasado, remoção da biblioteca, CORS/Retry-After, limite de dez polls, rollover, isolamento de contas, teclado/390px e falha isolada da seção. Injeção de 5xx/404 genérico e atraso de entrega são fixtures de transporte identificadas, sem alegar prova do backend por respostas simuladas. O 18º caso testa atualização do Service Worker em HTTP efêmero com bytes legados diferentes e o script exato de produção: cache privado removido, asset público preservado e fetch privado offline rejeitado. O SW permanece ativo nos testes da aplicação.

O E2E também reinicia o contexto Spring mantendo MySQL/relógio: preserva id/generatedAt de READY e, no dia seguinte, retoma BUILDING expirado pelo startup, sem GET intermediário que inicie a geração.

Revisão visual inicial (antes da alteração de apresentação solicitada pelo usuário) dos recortes estáveis: [desktop](validation/desktop.png) e [390px](validation/mobile390.png). Não foram encontrados cortes das ações/conteúdo; há foco visível, justificativa/data e capa alternativa. Isto não substitui feedback de leitores.

## Ambiente e resultados de capacidade

Host: Windows 11 x64 (build 10.0.26200), AMD Ryzen 5 5500X3D, seis cores/12 processadores lógicos, 34.279.276.544 bytes de RAM. MySQL 8.0.32 em containers novos e descartáveis, sem volumes/banco da aplicação ou dados de produção. Node 24.18.0, npm 11.16.0, Next 16.2.4/React 19.2.4 e lockfile preservados.

O backend completo/opt-in usou a JVM efetiva do Maven **Java 17.0.17**, heap máximo 8.573.157.376 bytes e Hikari **60**. O harness do navegador usou **Java 17.0.19**, mesmo heap máximo e Hikari **10**, capturados no JSON. São perfis diferentes, explicitados em cada artefato.

| Capacidade backend | Resultado |
| --- | --- |
| Dataset | 100.000 livros, 1.000 leitores, 1.000 vínculos por leitor (1.000.000 no total) |
| Seed | 65.164ms |
| Geração | 197.918ms, dois workers, fila 100; 1.000 READY, zero falhas observadas |
| Referência independente | Ordenação integral do top-30 aprovada para leitores determinísticos 1, 500 e 1.000 |
| Leituras backend | 50 leitores, 1.000 amostras MockMvc; p95 182,1174ms, sem transporte de socket/browser |
| CPU na janela de geração | 323.328.125.000ns |
| Heap observado na geração | Pico amostrado 894.586.680 bytes, intervalo 100ms |
| RSS antes/depois | 794.558.464 / 1.556.946.944 bytes; não é pico instantâneo completo |
| GC na janela | 655 coleções young, 674ms agregados; zero coleções old |
| Espera de locks InnoDB | Zero waits e zero ms de espera; demais contadores disponíveis no JSON |

EXPLAIN: batch por unicidade leitor/data (uma linha), leitura de até 30 itens, livros por PK e anti-join pelo índice único leitor/livro. Filesort limitado aos 30 itens; nenhum ranking/varredura de catálogo no GET pronto. Retenção e leitura por novo adapter após persistência aprovadas. Duas projeções de 100 mil entradas/datas diferentes foram mantidas simultaneamente e medidas em janela separada, fora do tempo do job e sem GC forçado; diferenças entre amostras de heap não representam custo marginal isolado.

## SC-006 no navegador

Chromium **148.0.7778.96**, viewport **1280x800**, build de produção, rede local sem throttling e cache normal do navegador; API personalizada no-store/SW network-only. Execução serializada, sem outros builds ou ensaios de carga durante a série.

O harness semeou o mesmo perfil integral e gerou mil READY pelo motor real antes da medição: seed **66.728ms**, geração **235.402ms**. Em seguida executou cinco aquecimentos e 100 navegações completas. A observação foi instalada antes da navegação; fim somente com conteúdo READY/EMPTY visível e duas requestAnimationFrame. Não foi usado skeleton, título da seção ou recebimento do GET como fim.

| Série válida | Valor |
| --- | --- |
| Amostras | 100, sem exclusão de lentas |
| p95 nearest-rank | **264,5ms** |
| Média / mínimo / máximo | 182,997 / 109,9 / 351,4ms |
| Aquecimentos separados | 1.484,7 / 306,4 / 206,4 / 137,8 / 184ms |
| Carga de apoio | 50 leitores distintos; pausa 500ms entre leituras; 2.235 leituras |
| Falhas/timeouts da série | Zero; falhas da carga de apoio também zero |

## Decisões e correções relevantes

O usuário dispensou o teste humano SC-007/T051 e preferiu iterar depois. A dispensa está na spec, plano e tarefas; T051 conclui o registro dessa decisão e das verificações automatizadas mantidas. Não se atribui avaliação qualitativa ou satisfação a agentes.

As frentes de implementação foram delegadas e revisadas pelo orquestrador. Uma auditoria independente de código encontrou dois P2 de cancelamento (snapshot não interruptível e heartbeat atingindo thread reutilizada). Ambos foram corrigidos e tiveram o fechamento conferido pelo revisor, com 25 testes direcionados aprovados. Registro em [review-iterations.md](review-iterations.md).

Outras correções verificadas: timers de cooldown encerrados, 429 preservando FAILED/Retry-After, 409 sem falso anúncio, limpeza de fila no shutdown, propriedade do lease antes de pontuar, observação operacional independente da descoberta, dados de biblioteca/assinatura vinculados ao dono da sessão, cancelamento/ignorância de respostas antigas e SW sem fallback privado.

Falhas intermediárias foram tratadas sem enfraquecer as provas: asserção de CHECK ajustada para a tradução real do driver exigindo o nome da restrição; fixtures DONE com progress null e autores válidos; espera explícita da ativação do SW; retry limitado de rename atômico para compartilhamento transitório de arquivo no Windows. As suítes finais acima passaram após as correções.

## Reprodução e limites

Comandos, propriedades, autostart, método de medição e perfis opt-in estão em [quickstart.md](quickstart.md). Não foram adicionadas dependências obrigatórias ou infraestrutura externa, nem alterados os gates de assinatura/leitura.

Validação documental: 52 IDs únicos, cobertura dos 25 critérios ativos e rastreabilidade da dispensa SC-007; links presentes, sem ciclos de dependências/referências; nove schemas compilados com nullable adaptado do OpenAPI 3.0, 14 exemplos sintéticos aceitos e 18 inválidos rejeitados, além do exemplo do contrato. Não é validação completa de toda a especificação OpenAPI; estados/respostas runtime são também conferidos pelas suítes REST/E2E.

Os resultados são do cenário sintético/local de referência, não medição de produção ou de todos os navegadores/redes. A referência integral de capacidade abrange três leitores controlados; a correção do comparador/heap também tem testes próprios. O reinício E2E é do serviço Spring na mesma JVM, com MySQL mantido; não foi medido kill do processo do sistema operacional sob carga. RSS foi amostrado nas fronteiras da janela; não se alega pico instantâneo completo. Nenhum resultado de satisfação com leitores foi inventado.

Não existem hooks after_implement configurados. O estado real das tarefas e da branch foi conferido no encerramento; os processos/containers de teste foram encerrados. O snapshot Git local foi autorizado posteriormente pelo usuário; não foi realizado push ou deploy.

## Revisão visual posterior — 2026-10-30

Resultado final solicitado pelo usuário: título “Você também pode gostar”, sem data, quantidade ou motivo visíveis; capas 2:3 em grupos de 2/3/4/5 conforme a largura; controles com cursor pointer. O BookCover é compartilhado com o cartão efetivamente usado no Explorar: hover/foco mostra a mesma sobreposição e o +; clique na capa abre os detalhes, sem botões abaixo. Somente nas recomendações, título e autor permanecem visíveis abaixo da capa. Em telas sem hover, o + permanece acessível sobre a capa nítida; o fundo/blur aparece apenas no foco. Metadados, ranking e persistência diária permanecem na API.

| Verificação da revisão | Resultado |
| --- | --- |
| Frontend completo | 83 testes em 19 arquivos aprovados |
| Regressões focalizadas após refinamento touch | 26/26 aprovadas |
| Lint / TypeScript | Zero erros; cinco avisos de imagem preexistentes / aprovado |
| E2E completo com MySQL/backend reais | 19/19 aprovados, 2,8min |
| Verificação final desktop e toque após CSS | 2/2 aprovados |
| Revisão independente | Sem achados relevantes pendentes |
| Documentação e rastreabilidade | 55 tarefas concluídas, 26 identificadores cobertos, sem ciclos |

O E2E adicional reproduz BOOK_NOT_FOUND real recebido depois de navegar e desmontar o cartão. A confirmação agora chega ao hook da conta mesmo após a desmontagem; respostas genéricas revalidam sem confirmar indisponibilidade. Guards de sessão e estado local desmontado foram preservados. Navegação por grupos, último grupo parcial, mil voltas sem rede/clones, gestos sem clique residual, foco, inclusão, 409, 404 e respostas GET atrasadas continuam cobertos.

Capturas finais revistas pelo orquestrador com capas reais: [desktop](validation/ui-refresh-desktop.png), [hover](validation/ui-refresh-hover.png), [390px](validation/ui-refresh-mobile390.png), [viewport móvel](validation/ui-refresh-mobile390-viewport.png) e [dispositivo com toque](validation/ui-refresh-mobile-touch.png). Referência compartilhada do Explorar: [normal](validation/explore-reference-card.png) e [hover](validation/explore-reference-hover.png).

O smoke de leitura em localhost:3000 verificou larguras 1440, 1280, 1024, 768, 390 e 320px sem overflow horizontal; uma volta completa dos 29 itens elegíveis no snapshot da demo, em seis grupos e sem novos GETs; cursor pointer e teclado. Zero alterações na biblioteca por esse smoke; interações do usuário foram preservadas. [Dados do smoke](validation/ui-refresh-demo.json). A captura touch aguardou opacity=1 do + e opacity=0 do fundo sem desabilitar animações na captura, evitando artefato do Playwright sobre transições.

O frontend E2E usa distDir separado em .next/e2e, por variável passada apenas ao processo de teste; a demo em 3000/8080 permanece disponível. Artefatos gerados de cobertura/configuração foram restaurados. Os ensaios pesados de backend/capacidade e o p95 acima pertencem à implementação inicial e não foram repetidos para esta alteração de apresentação. Não houve novo commit, push ou deploy nesta revisão.
