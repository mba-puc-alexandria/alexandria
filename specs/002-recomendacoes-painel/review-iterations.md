# Revisão crítica do planejamento — dois loops iniciais e uma revisão independente

Data: 2026-10-30. Branch de trabalho: `feature/002-recomendacoes-painel-wip`.

Escopo: especificação, plano, tarefas, pesquisa, modelo, contratos e guia de validação. Dois subagentes somente de leitura: `critico_produto` (produto/UI/UX/valor) e `critico_tecnico` (arquitetura/consistência/escala). O orquestrador realizou as alterações. Nenhum código da aplicação foi implementado nesta revisão.

## Loop 1 — crítica e refinamento

| ID | Achado | Resolução e rastreabilidade |
| --- | --- | --- |
| P1, alta | Proibir repetição conflita com Anterior e seleção unitária | FR-016/SC-009 definem volta por avanços em Próximo; rever é permitido. T031/T036. |
| P2, alta | Quantidade visível, passo e foco de cartões ocultos indefinidos | Default de um cartão por vez em celular/desktop, passo unitário, indicador X de N e ocultos fora do foco/leitura. Contrato do Painel, T031/T036. |
| P3, alta | NO_PROFILE pressupõe biblioteca vazia | Mensagem neutra e Explorar, inclusive para notas baixas/metadados insuficientes. Painel/OpenAPI, T017/T029. |
| P4, alta | STALE sem itens e atualização pendente sem ação definida | STALE admite lista vazia; Verificar atualização faz GET; generationStatus distingue progresso/falha e orienta POST explícito após cooldown. OpenAPI/Painel, T016/T024/T026/T027/T037. |
| P5, média | Motivo congelado pode parecer preferência atual falsa | Motivos históricos e data da seleção via referenceDate, sem inventar data de avaliação. FR-007, Painel, T028. |
| P6, média | Usabilidade não mede utilidade percebida | Avaliação qualitativa do top-5 com bibliotecas representativas, rejeições, idiomas e autores, separada do gate SC-007; resultados ruins geram análise/proposta de ajuste. Spec/quickstart, T051. |
| T1, alta | Data no claim conflita com PENDING anterior à fila | Data imutável criada no PENDING; nenhum claim novo de ontem; worker iniciado pode concluir sob data original. Modelo/geração, T012/T022/T023/T030/T033. |
| T2, alta | GET poderia contornar orçamento automático por retry manual | Origem AUTO/MANUAL, automatic_attempt_count separado do diagnóstico; cem recargas após três falhas não geram claim. Modelo/geração, T005/T007/T023/T024/T030. |
| T3, alta | Perda de sinais enquanto aguarda fila congela READY vazia indevida | Revalidar no worker antes do catálogo; liberar PENDING dormente por token válido sem READY/falha nem consumo AUTO; novos sinais reativam. Modelo/geração, T007/T012/T022/T030. |
| T4, alta | Retry-After não está exposto ao navegador via CORS atual | Prever exposição na configuração efetiva de segurança e testes de Origin/browser. Plano, T016/T025/T026/T037. |
| T5, média | Vida útil do snapshot compartilhado indefinida | Sessão imutável por processo com handles, construção single-flight e liberação após último consumidor; origens sobrepostas aderem à sessão ativa. Plano/geração, T021. |

Todos os refinamentos foram enviados aos mesmos dois subagentes para releitura na segunda rodada. Foram preservadas as decisões de catálogo inteiro, até 30 itens/dia, repetição local, ausência de reposição intradiária e nenhuma nova infraestrutura externa.

## Loop 2 — releitura e correções finais

Os revisores consideraram atendidos os achados da primeira rodada e emitiram pareceres favoráveis ao desenho, condicionados às correções locais abaixo. O problema de schema foi identificado independentemente por ambos.

| ID | Achado | Correção final aplicada |
| --- | --- | --- |
| P7/T6, alta | generationStatus obrigatório mas não declarado em schema fechado; 503 só descrevia campos | Propriedade e enum declarados; schema RecommendationUnavailableResponse exige generationStatus e retryAfterSeconds no 503. No-store declarado nas respostas controladas. T016/T026/T027. |
| T7, média | BUILDING de dia antigo com lease vencido não seria recuperado nem limpo | Retenção após sete dias inclui BUILDING expirado, preservando lease ativo. Modelo/geração/quickstart, T034. |
| T8, média | WARN contaria dormência/headers antigos e alertaria imediatamente após reativação | Excluir dias antigos e ausência de perfil; operational_pending_since fica null na dormência e reinicia uma vez ao reativar, sem alterar created_at/contadores; polls não postergam início. Modelo/geração/quickstart, T005/T007/T035. |

Parecer de produto: “estou convencido do desenho refinado”; sanado P7, “apto para implementar quanto a produto/UX”. Parecer técnico: “estou convencido da arquitetura proposta e favorável a seguir para implementação” após T6–T8. As correções condicionantes foram aplicadas pelo orquestrador e verificadas documentalmente após esses pareceres, sem iniciar um terceiro loop de revisão.

## Verificação do orquestrador e limites

- YAML carregado com js-yaml disponível nas dependências locais; referências locais resolvidas.
- Campos required declarados em schemas com additionalProperties=false; exemplo EMPTY/NO_PROFILE confere com campos obrigatórios.
- Respostas 503 referenciam schema dedicado; respostas controladas GET/POST declaram no-store.
- Preservados 52 IDs únicos T001–T052 e todas as tarefas desmarcadas; responsabilidades e testes foram ampliados nas tarefas existentes.
- Revisão de diff sem erros de whitespace; nenhuma constituição ou hook de extensão presente no projeto.

As verificações são estruturais e documentais, não uma validação completa automatizada de OpenAPI nem execução dos testes planejados. A aprovação do desenho não comprova desempenho, concorrência MySQL, funcionamento CORS ou relevância das recomendações. Esses critérios permanecem nas tarefas de implementação e validação, incluindo participantes reais para T051. Defaults de um cartão por vez e avaliação qualitativa são propostas refinadas de planejamento, sujeitas à validação prevista.

## Loop adicional — dois revisores novos, sem histórico

A pedido do usuário, foram criados `produto_revisao_fresca` e `tecnico_revisao_fresca`, ambos com fork_turns=none. Receberam somente função, escopo e caminhos de documentos, sem conclusões anteriores. Ambos confirmaram não ter consultado este relatório antes de formar o parecer. O orquestrador aplicou as correções e solicitou apenas conferência de fechamento dos achados, sem iniciar outra revisão ampla.

| Origem/prioridade | Cenário encontrado | Correção e cobertura |
| --- | --- | --- |
| Produto, P2 | Livro removido após exibição era tratado como falha transitória, preservando cartão e retry impossível | Distinguir BOOK_NOT_FOUND confirmado de 404 genérico/rede/5xx. Confirmado: informar, retirar, preservar foco, revalidar GET, sem reposição. Código opcional específico no erro existente e preservação no cliente. Painel/plano/pesquisa, T039–T045 e quickstart. |
| Técnico, P1 | Terceiro crash AUTO deixa BUILDING expirado sem orçamento, mas interface só oferece retry para FAILED | Normalização por CAS observado para FAILED/LEASE_EXPIRED independe do orçamento; invalida token sem novo claim. failed_at orienta cooldown manual de 60s, separado do agendamento AUTO. GET retorna falha acionável e POST pode recuperar. Modelo/geração, T005/T007/T012/T024/T030/T033. |
| Técnico, P2 | Últimos workers liberam snapshot enquanto outra onda aguarda fila/perfil, causando nova varredura na mesma execução | Proprietário lazy e tarefas registradas conservam sessão durante fila/intervalos. Liberação após descoberta encerrada e todas tarefas/handles concluídos. Teste de dois workers/seis leitores com barreira exige uma construção. Geração/plano, T021/T032. |
| Orquestrador e técnico | allOf de RecommendationUnavailableResponse referenciava o próprio schema; a checagem anterior só verificava existência de refs | Corrigida base para ErrorResponse. Ampliada validação para resolução com detecção de ciclos, compilação dos schemas e exemplos positivos/negativos de 503. T047/quickstart. |

Parecer de fechamento do produto: “Aceite documental: achado encerrado, sem ressalvas.” Parecer técnico: “Aceite documental dos dois achados; ambos encerrados nesta rodada.” Os dois verificaram as correções correspondentes no disco antes desse aceite.

Validação do orquestrador: schemas compilados com AJV local após adaptar nullable do OpenAPI 3.0 para JSON Schema; referências resolvidas sem ciclos; exemplo NO_PROFILE e resposta 503 completa aceitos; cinco exemplos sem campos obrigatórios e um com generationStatus inválido rejeitados. O formato OpenAPI int64 foi ignorado pela biblioteca, mantendo verificação de tipo inteiro; não se trata de validação completa da especificação OpenAPI. Verificados também 52 IDs únicos e desmarcados e diff sem erros de whitespace. Não houve implementação nem execução das suítes da aplicação nesta rodada.

## Preparação final para implementação — 2026-10-30

Nova análise somente de leitura concluída e remediação aplicada em etapa posterior, dentro do pedido do usuário de preparar tudo antes de implementar. Não houve subagentes nesta rodada. Registro atual e comandos reproduzíveis: [implementation-readiness.md](implementation-readiness.md).

Cinco achados tratados: compatibilidade Testcontainers/Docker comprovada por probe com api.version=1.44; isolamento de heartbeat/controle/scheduler definido; sessão de catálogo delimitada por referenceDate; T052 corrigida para a branch WIP; empate de justificativa definido. Precisado também o isolamento REPEATABLE_READ da leitura em lotes. As tarefas existentes foram ampliadas sem novos IDs ou marcação de conclusão.

Foram executadas as suítes existentes (296 testes backend e 30 frontend), lint sem erros e build frontend. Um container MySQL 8.0.32 novo recebeu e validou as quatro migrações existentes; isso não valida V4, concorrência da feature ou banco legado. Acrescentados scripts de probe e validação documental; as aprovações históricas acima não são atribuídas automaticamente aos refinamentos desta rodada. Implementação e critérios de capacidade/usabilidade continuam pendentes.

## Implementação — auditoria independente de cancelamento

O usuário autorizou subagentes e pediu atuação do root como orquestrador. A implementação foi dividida entre base/persistência, execução/backend e frontend, com avaliação e devolução de correções. Um revisor adicional sem histórico recebeu somente os caminhos de artefatos/código e foco em concorrência/privacidade; confirmou não ter consultado este relatório ou validation-results antes de formar o parecer.

| Achado de código | Correção e prova |
| --- | --- |
| P2: future.join no provider ignorava interrupção do consumidor enquanto outro worker construía catálogo | Espera por future.get com interrupt restaurado e CancellationException; cleanup do handle reservado sem cancelar o future compartilhado. Teste com builder A retido e consumidor B interrompido exige saída de B antes de liberar A, construção única e zero sessões restantes. |
| P2: callback de heartbeat podia observar running=true, pausar antes de interrupt e atingir a thread já reutilizada por outra tentativa | Revogação e running=false de término sob o mesmo lock por tentativa, com JDBC fora do lock. Testes de callback JDBC tardio e pausa em Thread.interrupt provam que a tentativa antiga não termina/reutiliza thread antes da interrupção, e a nova não recebe o interrupt antigo. |

Verificação agrupada: 25 testes aprovados, zero falhas/erros/ignorados (provider seis, worker seis, recursos quatro, geração nove). O revisor conferiu os patches e o log, encerrando os dois achados sem regressão direta identificada. Não foram identificados achados críticos/altos nos caminhos examinados; o parecer não substitui E2E, MySQL integral ou capacidade.

Outras correções exigidas na revisão do orquestrador: contador frontend sem timer residual, 429 preservando FAILED/cooldown, 409 sem falso anúncio, isolamento de conta em AuthProvider/dashboard/hook, limpeza de tarefas enfileiradas e verificação do lease antes de pontuar. O teste E2E de resposta atrasada revelou o Service Worker intermediando a rota privada: correção network-only e remoção de respostas privadas legadas mantêm o SW ativo nas verificações, sem mascarar o comportamento por configuração de testes.

Decisão posterior do usuário: dispensar SC-007/T051 como aceite humano e iterar depois. A dispensa foi registrada nos artefatos; não se alega teste com participantes ou satisfação comprovada. Resultados finais e limites atuais em [validation-results.md](validation-results.md).
