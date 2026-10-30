# Contrato interno de geração diária

## Agendamento e identidade

- Fuso: America/Sao_Paulo. Default configurável: cron `0 0 0 * * *` (00h); esse horário é hipótese de engenharia, não horário explicitamente escolhido pelo usuário.
- Clock injetável; data de referência capturada na criação de PENDING e conservada no claim. Leases e instantes operacionais usam UTC do banco. A virada do dia durante o cálculo não muda a data do header.
- Uma seleção READY por (userId, referenceDate). A navegação não a reconstrói. Não há endpoint que força reconstrução de READY do mesmo dia.
- A ordem de decisão é: (1) reutilizar READY de hoje, inclusive vazia, aplicando exclusões atuais; (2) sem READY de hoje, verificar o perfil atual antes de qualquer fallback; sem sinal positivo, responder EMPTY/NO_PROFILE, selection=null e items=[], sem servir seleção antiga ou iniciar ranking; (3) com sinal positivo, solicitar/retomar a geração diária e usar a última READY como STALE enquanto necessário, ou 202 sem anterior; falha sem anterior retorna 503. Mudanças intradiárias de perfil não invalidam uma READY de hoje: influenciam somente a próxima seleção. O caminho NO_PROFILE não publica READY e permite a primeira geração do dia se surgirem sinais positivos depois.
- READY de zero candidatos é uma geração finalizada e não volta a tentar gerar por mudanças de perfil no mesmo dia.

## Execução

Dispatcher diário e reconciliador de 5 minutos percorrem userIds em lotes. Executor separado: dois trabalhadores e fila de 100. Fila cheia adia claim/submissão; não usar descarte silencioso nem manter lease parado por tempo indeterminado na fila. Registrar PENDING com created_at na primeira solicitação de trabalho (sem duplicar header por leitor/data), antes de aguardar a fila; isso não adquire lease. Fazer claim quando o trabalhador inicia; múltiplos dispatchers podem ofertar o mesmo userId, e só um claim vence.

Cada processo compartilha uma projeção de catálogo completa por execução/referenceDate e controla single-flight da leitura para não criar uma cópia por trabalhador. As projeções são consistentes, lidas por ID-keyset com autores em lote na mesma transação read-only de isolamento REPEATABLE_READ explícito; não chamar API externa. Liberar a projeção somente após encerrar a execução e terminar todas as suas tarefas/handles; medir o pico durante construção/substituição, inclusive sobreposição entre datas. Novos leitores tardios podem iniciar uma execução própria; isso não reconstrói seleções READY.

Scheduler cron/startup/recovery não percorre leitores nem espera espaço na fila: apenas submete gatilho coalescido e retorna. Descoberta/backpressure ocorre em executor de controle separado (um controlador), nunca nos dois workers de ranking. Heartbeats usam scheduler dedicado que não executa descoberta/ranking. Todos os recursos têm shutdown controlado; cada heartbeat é cancelado em finally. Validar heartbeat >0 e <lease. Saturação da fila não pode impedir renovação de um lease válido.

Para cada claim:

1. Assumir PENDING existente ou FAILED elegível via atualização condicional; antes disso normalizar BUILDING expirado para FAILED conforme recuperação abaixo; READY não pode ser assumido. Gerar novo lease_token aleatório.
2. Conceder lease de 5 minutos e renovar a cada 60 segundos por token durante leitura/cálculo. Renovação exige status BUILDING, token coincidente e lease_until > UTC_TIMESTAMP(6). Lease expirado não pode ser renovado; o trabalhador deve parar e obter novo claim/token. Renovação falha cancela tentativa sem publicar.
3. Revalidar perfil antes de obter catálogo. Sem sinais, liberar PENDING dormente sem READY/falha e reverter consumo AUTO por token; novos sinais reativam no mesmo dia. Com perfil positivo, avaliar toda a projeção, retendo top-30 com comparador de research.md.
4. Publicar itens e READY numa única transação curta, com verificação do token e lease válidos. Zero itens é publicação válida.
5. Em exceção, atualizar FAILED e failure_code somente com BUILDING, token coincidente e lease ainda válido pelo UTC do banco; não alterar outra tentativa nem última seleção pronta.

Origem AUTO para GET/cron/startup/recovery; MANUAL exclusivamente no POST explícito. Tentativas automáticas: no máximo três claims efetivos por data (automatic_attempt_count), backoff de 60s e depois 300s. Retry manual pode retomar FAILED após cooldown de 60s, mantendo o mesmo header. Solicitações repetidas enquanto BUILDING com lease válido retornam ALREADY_RUNNING e não criam cópias; lease expirado passa pela normalização de falha antes de decidir a resposta. Se já READY, retornam READY sem mudar itens ou generated_at. Uma requisição com nova data nunca usa a data enviada pelo cliente.

## Leitura, atualização e fallback

A ordem de decisão é: (1) reutilizar READY de hoje, inclusive vazia, aplicando exclusões atuais; (2) sem READY de hoje, verificar o perfil atual antes de qualquer fallback; sem sinal positivo, responder EMPTY/NO_PROFILE, selection=null e items=[], sem servir seleção antiga ou iniciar ranking; (3) com sinal positivo, solicitar/retomar a geração diária e usar a última READY como STALE enquanto necessário, ou 202 sem anterior; falha sem anterior retorna 503. Mudanças intradiárias de perfil não invalidam uma READY de hoje: influenciam somente a próxima seleção. O caminho NO_PROFILE não publica READY e permite a primeira geração do dia se surgirem sinais positivos depois. Aplicar anti-join e disponibilidade em toda seleção retornada. Nunca usar seleção de outro leitor ou repor livros filtrados.

Mudança de status/nota tem efeito na próxima geração. Inclusão afeta filtros já no próximo GET e remoção imediata na interface da própria ação. A seleção mantém motivos do dia de geração. Excluir posteriormente vínculo da biblioteca pode reexibir item selecionado; não há supressão permanente adicional.

## Retenção e observabilidade

Reter duas últimas READY por usuário após nova publicação, além do header atual em andamento. Remover excedente em lotes curtos com cascade de itens, sem lock prolongado. Última READY não expira por falha ou inatividade. Limpar tentativas antigas PENDING/FAILED e BUILDING com lease vencido após sete dias, preservando qualquer lease ativo. Não criar histórico de todo o catálogo por usuário.

Registrar totais de livros lidos, usuários processados, pendentes/falhos, tempo de geração e GET, retries e idade da última READY. Labels de métricas são estados e versão; não userId/assuntos/avaliações. Logs sanitizados podem incluir batchId e código de falha, sem biblioteca, recomendação individual ou token. Verificar a cada 5 minutos se há gerações PENDING/BUILDING/FAILED sem READY cuja pendência dura mais que a janela configurável, default 60 minutos. A idade começa em operational_pending_since do header do dia, inicialmente igual a created_at e reiniciado ao reativar após dormência; não usar simplesmente a meia-noite para leitores recém-chegados. Emitir WARN agregado com referenceDate, quantidade de leitores pendentes e idade da pendência mais antiga, sem IDs pessoais, títulos, assuntos, avaliações ou tokens. Limitar a uma emissão por hora por instância/data de referência, sem suprimir a primeira ocorrência elegível; com a fila normalizada, não emitir aviso. Reinício pode reiniciar o limitador local. Notificações externas ficam fora do MVP.

## Invariantes de teste

- Dois workers/réplicas concorrentes -> uma publicação por usuário/dia.
- Token expirado e retomado -> worker antigo não publica nem marca a tentativa nova como falha.
- Crash entre inserção dos itens e READY -> rollback, sem seleção parcial visível.
- Crash após criar PENDING e antes de BUILDING -> o PENDING existente é recuperado por claim, sem ficar bloqueado indefinidamente.
- Downtime às 00h -> startup/reconciliador recupera o dia faltante.
- Inclusão durante cálculo/antes de resposta -> item pode existir no snapshot, mas GET o filtra e frontend não o reintroduz após confirmação.
- Virada de dia/fuso do host diferente -> datas corretas, sem segunda publicação do mesmo período.
- Falha de geração e seleção anterior -> STALE com exclusões atuais.
- Recarregar repetidamente ou percorrer mil voltas -> mesma seleção e zero novas publicações.

- Seleção antiga e ausência de sinais positivos no novo dia -> EMPTY/NO_PROFILE antes de STALE; READY de hoje mantém a estabilidade intradiária.
- Pendência de mais de 60min -> WARN agregado sem dados pessoais; checagens subsequentes antes de uma hora não repetem o aviso na mesma instância/data; fila sem atraso não emite aviso.

## Regras operacionais refinadas

- Capturar referenceDate ao criar PENDING, antes da fila; claim conserva identidade. Não iniciar claims de ontem. Cálculo iniciado ontem pode terminar sob data original; fila antiga é ignorada e o dia atual é solicitado normalmente. Nunca renomear header.
- GET nunca vira MANUAL: cem recargas após três falhas AUTO não iniciam claim. Com anterior: STALE+generationStatus=FAILED; sem anterior: 503. POST após cooldown tenta incrementando attempt_count, sem aumentar orçamento AUTO. Pedidos coalescidos não alteram origem/contadores do claim vencedor.
- NO_PROFILE no worker libera BUILDING→PENDING dormente por token válido, sem READY/falha nem consumo AUTO. Pedidos/recovery não reofertam até detectar sinais. Novos sinais permitem geração no mesmo dia; perfil positivo sem candidatos publica READY vazia.
- Snapshot: uma sessão compartilhada por processo/referenceDate com snapshot imutável e handles de consumidores. Workers de cron/recovery/GET aderem à sessão ativa da mesma data, cujo proprietário também conta como consumidor. Construção single-flight por data; liberar só após encerrar o proprietário e terminar todos os trabalhos registrados, além de todos os handles, e impedir aquisição em sessão fechada. Construção seguinte da mesma data após fechar anterior; nunca invalidar referência em uso. Na virada, a nova data abre sessão própria e nunca reutiliza a de ontem; workers já iniciados ontem conservam seus handles. Falha é propagada sem publicação parcial. Sem proprietário, trabalhos registrados ou handles, sessão é encerrada: não é cache duradouro. Testar origens sobrepostas, virada com alteração do catálogo e encerramento concorrente.

## Ajustes finais da rodada 2

WARN considera somente headers do dia atual com perfil positivo e trabalho efetivamente pendente; excluir PENDING dormente sem sinais e datas anteriores ignoradas. Não presumir falta de perfil apenas pelo status PENDING: usar verificação de sinais. Retenção abrange também BUILDING antigo com lease vencido após sete dias, sem tocar lease ativo.

Dormência observada limpa operational_pending_since no PENDING sem lease ativo (ou na liberação por token do worker). Reativação com sinais define UTC do banco apenas se null, preservando created_at e contadores. WARN mede esse início operacional; polls/retries não o renovam. Testar retorno dos sinais após dormência longa sem aviso imediato, seguido de aviso somente após novo limiar.

## Recuperação de lease e propriedade da execução — revisão independente

GET, POST e recovery normalizam BUILDING expirado antes de decidir ALREADY_RUNNING, orçamento ou retry. Uma operação curta específica do reconciliador faz compare-and-set por status BUILDING, token observado e lease_until observado <= UTC_TIMESTAMP(6), mudando para FAILED, invalidando o token e registrando failure_code=LEASE_EXPIRED. Não depende de orçamento AUTO disponível, não adquire claim nem incrementa contadores. Essa operação é distinta da marcação de falha pelo worker titular, que continua exigindo lease válido. Se perder a corrida, reler o header; nunca modificar uma nova tentativa ou READY.

Persistir failed_at: UTC do banco na falha normal; lease_until observado na expiração. Retry MANUAL fica elegível em failed_at+60s; AUTO usa next_attempt_at (failed_at+60s após primeiro claim automático, +300s após segundo; esgotado o orçamento, não há agendamento AUTO). Após falha de tentativa MANUAL com orçamento AUTO ainda disponível, AUTO aguarda pelo menos 60s. Separar cooldown manual de backoff automático: retryAfterSeconds em FAILED informa exclusivamente cooldown MANUAL restante, null se elegível; header Retry-After em 503 acompanha esse valor e é omitido se já elegível. GET depois do terceiro crash mostra FAILED (503 sem anterior, STALE com anterior) sem quarto claim AUTO. POST explícito pode recuperar após cooldown. Testar disputa entre normalização, heartbeat, publicação e novo claim.

Cada execução tem um proprietário registrado no provider, além dos handles dos workers. Proprietário não força construção: catálogo só é lido quando primeiro worker com perfil positivo solicita. Registrar tarefas antes de submetê-las; fila/espera de perfil contam como trabalho pendente. O proprietário conserva a sessão entre ondas, encerrando-a apenas após terminar a descoberta/submissão e todas as tarefas registradas terminarem ou serem canceladas. Exceção/rejeição cancela registro em finally e mantém PENDING durável para futura execução. Workers tardios da mesma referenceDate podem aderir à execução ativa; aquisição e fechamento são atômicos. Teste com dois workers e seis leitores, barreira entre ondas: exatamente uma construção completa, nenhuma liberação entre ondas, nenhuma referência perdida, liberação final. Nenhum consumidor pode invalidar snapshot ainda em uso.

Nesse teste, todos os leitores pertencem à mesma referenceDate. Acrescentar virada com handle antigo ativo e catálogo alterado: o consumidor de hoje vê a nova projeção, o antigo mantém a sua até concluir, e ambas são liberadas ao fim. Não usar a sessão do processo como cache que atravesse datas.
