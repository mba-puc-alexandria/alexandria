# Contrato interno de geração diária

## Agendamento e identidade

- Fuso: America/Sao_Paulo. Default configurável: cron `0 0 0 * * *` (00h); esse horário é hipótese de engenharia, não horário explicitamente escolhido pelo usuário.
- Clock injetável; data de referência capturada no início do claim. Leases e instantes operacionais usam UTC do banco. A virada do dia durante o cálculo não muda a data do header.
- Uma seleção READY por (userId, referenceDate). A navegação não a reconstrói. Não há endpoint que força reconstrução de READY do mesmo dia.
- A ordem de decisão é: (1) reutilizar READY de hoje, inclusive vazia, aplicando exclusões atuais; (2) sem READY de hoje, verificar o perfil atual antes de qualquer fallback; sem sinal positivo, responder EMPTY/NO_PROFILE, selection=null e items=[], sem servir seleção antiga ou iniciar ranking; (3) com sinal positivo, solicitar/retomar a geração diária e usar a última READY como STALE enquanto necessário, ou 202 sem anterior; falha sem anterior retorna 503. Mudanças intradiárias de perfil não invalidam uma READY de hoje: influenciam somente a próxima seleção. O caminho NO_PROFILE não publica READY e permite a primeira geração do dia se surgirem sinais positivos depois.
- READY de zero candidatos é uma geração finalizada e não volta a tentar gerar por mudanças de perfil no mesmo dia.

## Execução

Dispatcher diário e reconciliador de 5 minutos percorrem userIds em lotes. Executor separado: dois trabalhadores e fila de 100. Fila cheia adia claim/submissão; não usar descarte silencioso nem manter lease parado por tempo indeterminado na fila. Registrar PENDING com created_at na primeira solicitação de trabalho (sem duplicar header por leitor/data), antes de aguardar a fila; isso não adquire lease. Fazer claim quando o trabalhador inicia; múltiplos dispatchers podem ofertar o mesmo userId, e só um claim vence.

Cada processo compartilha uma projeção de catálogo completa por execução e controla single-flight da leitura para não criar uma cópia por trabalhador. As projeções são consistentes, lidas por ID-keyset com autores em lote; não chamar API externa. Liberar a projeção ao encerrar a execução ou antes de substituir por outra; medir o pico durante construção/substituição. Novos leitores tardios podem iniciar uma execução própria; isso não reconstrói seleções READY.

Para cada claim:

1. Criar o header e assumir PENDING existente, FAILED com backoff vencido ou BUILDING expirado via atualização condicional; READY não pode ser assumido. Gerar novo lease_token aleatório.
2. Conceder lease de 5 minutos e renovar a cada 60 segundos por token durante leitura/cálculo. Renovação exige status BUILDING, token coincidente e lease_until > UTC_TIMESTAMP(6). Lease expirado não pode ser renovado; o trabalhador deve parar e obter novo claim/token. Renovação falha cancela tentativa sem publicar.
3. Ler perfil completo e avaliar toda a projeção disponível, retendo top-30 com comparador de research.md.
4. Publicar itens e READY numa única transação curta, com verificação do token e lease válidos. Zero itens é publicação válida.
5. Em exceção, atualizar FAILED e failure_code somente com BUILDING, token coincidente e lease ainda válido pelo UTC do banco; não alterar outra tentativa nem última seleção pronta.

Tentativas automáticas: no máximo três por data, backoff de 60s e depois 300s. Retry manual pode retomar FAILED após cooldown de 60s, mantendo o mesmo header. Solicitações repetidas enquanto BUILDING retornam ALREADY_RUNNING e não criam cópias. Se já READY, retornam READY sem mudar itens ou generated_at. Uma requisição com nova data nunca usa a data enviada pelo cliente.

## Leitura, atualização e fallback

A ordem de decisão é: (1) reutilizar READY de hoje, inclusive vazia, aplicando exclusões atuais; (2) sem READY de hoje, verificar o perfil atual antes de qualquer fallback; sem sinal positivo, responder EMPTY/NO_PROFILE, selection=null e items=[], sem servir seleção antiga ou iniciar ranking; (3) com sinal positivo, solicitar/retomar a geração diária e usar a última READY como STALE enquanto necessário, ou 202 sem anterior; falha sem anterior retorna 503. Mudanças intradiárias de perfil não invalidam uma READY de hoje: influenciam somente a próxima seleção. O caminho NO_PROFILE não publica READY e permite a primeira geração do dia se surgirem sinais positivos depois. Aplicar anti-join e disponibilidade em toda seleção retornada. Nunca usar seleção de outro leitor ou repor livros filtrados.

Mudança de status/nota tem efeito na próxima geração. Inclusão afeta filtros já no próximo GET e remoção imediata na interface da própria ação. A seleção mantém motivos do dia de geração. Excluir posteriormente vínculo da biblioteca pode reexibir item selecionado; não há supressão permanente adicional.

## Retenção e observabilidade

Reter duas últimas READY por usuário após nova publicação, além do header atual em andamento. Remover excedente em lotes curtos com cascade de itens, sem lock prolongado. Última READY não expira por falha ou inatividade. Limpar tentativas antigas sem lease ativo após sete dias. Não criar histórico de todo o catálogo por usuário.

Registrar totais de livros lidos, usuários processados, pendentes/falhos, tempo de geração e GET, retries e idade da última READY. Labels de métricas são estados e versão; não userId/assuntos/avaliações. Logs sanitizados podem incluir batchId e código de falha, sem biblioteca, recomendação individual ou token. Verificar a cada 5 minutos se há gerações PENDING/BUILDING/FAILED sem READY cuja pendência dura mais que a janela configurável, default 60 minutos. A idade começa em created_at do header do dia, registrado na primeira solicitação/constatação de trabalho; não usar simplesmente a meia-noite para leitores recém-chegados. Emitir WARN agregado com referenceDate, quantidade de leitores pendentes e idade da pendência mais antiga, sem IDs pessoais, títulos, assuntos, avaliações ou tokens. Limitar a uma emissão por hora por instância/data de referência, sem suprimir a primeira ocorrência elegível; com a fila normalizada, não emitir aviso. Reinício pode reiniciar o limitador local. Notificações externas ficam fora do MVP.

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
