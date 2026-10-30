# Contrato do Painel

## Posição e apresentação

Seção “Recomendados para você” após “Lendo Agora”, com carregamento e falha independentes das estatísticas. Exibir a data da seleção; quando stale, informar “Atualização pendente — mostrando a última seleção disponível”. Até 30 livros no estado do componente, responsivos e com controles sempre visíveis ao toque e teclado.

Cartão: capa ou substituto, título, autores, motivo e ações “Ver livro” (/explorar/{id}) e “Adicionar à biblioteca”. A API de recomendação traz um DTO compacto próprio; adaptar getAuthorDisplay para receber os campos necessários, sem forçar esse DTO a implementar BookApiResponse inteiro.

Não mostrar contagem enganosa de 30 se houver menos. Pode-se exibir “N sugestões disponíveis”. Não prometer renovação de todos os títulos diariamente: os melhores podem permanecer iguais se o perfil/base não mudou.

## Estados

Precedência: READY de hoje é reutilizada, mesmo se o perfil mudar depois. Sem READY de hoje, NO_PROFILE prevalece sobre uma seleção antiga; STALE é permitido somente se houver sinais positivos atuais. O frontend segue esse estado do backend, sem montar perfil a partir da primeira página da biblioteca.

| Estado recebido | Comportamento |
| --- | --- |
| carregando inicialmente | indicador sem bloquear outras seções |
| READY | lista circular de itens retornados |
| EMPTY + NO_PROFILE | mensagem para formar biblioteca e link Explorar |
| EMPTY + NO_MATCHES | sem sugestões compatíveis na seleção diária; link Explorar |
| EMPTY + ALL_ADDED_OR_UNAVAILABLE | seleção esgotada; informar próxima atualização diária |
| STALE | mesma navegação, aviso de data e atualização pendente; filtro atual preservado |
| GENERATING / 202 | “Preparando suas recomendações”; polling limitado por Retry-After |
| 503 ou falha de rede | preservar cartões anteriores; mensagem e nova tentativa |
| 401 | seguir fluxo de autenticação; apagar dados e cancelar requisições |

Polling: no máximo 10 consultas automáticas consecutivas por abertura, com intervalo >= Retry-After (default 3s). Após isso manter mensagem e permitir nova tentativa; jamais laço ilimitado. STALE não inicia polling contínuo; revalidar no foco/retorno e após tentativa explícita. Botão “Tentar novamente” usa POST /recommendations/generation somente para falha/ausência de geração, seguido de GET; falha de transporte com seleção pronta repete apenas GET.

## Navegação circular

- Avançar e voltar alteram o índice modular na lista retornada. Ao fim, avançar vai ao início sem rede, sem novo ranking e sem crescer o DOM.
- Sem autoplay, timers ou movimento por ausência de interação. “Reinício automático” significa continuar a navegação sem botão separado de reiniciar.
- Sem clones infinitos de cartões. Até 30 cartões/dados; usar keys por bookId e uma única ocorrência por cartão, mesmo nas várias voltas.
- Ao sobrar um livro, avançar pode mantê-lo/reapresentá-lo sem novas consultas. Com zero, desabilitar controles e mostrar estado vazio.
- Rolagem por gesto precisa mapear para o mesmo índice e comportamento do controle, inclusive wrap; não confundir fim do scroll nativo com fim das recomendações. Anteriores/próximos sempre funcionam por teclado.
- Ao excluir o cartão em foco, manter foco no próximo controle/cartão válido ou no título do estado vazio; não deslocar foco a cada consulta.

## Inclusão e concorrência

1. Usar addUserBook existente; desabilitar só o cartão pendente e evitar clique duplicado.
2. Após confirmação, remover bookId de toda a lista, cancelar requisições anteriores e incrementar geração local de requests. Revalidar com GET; não solicitar nova seleção do dia.
3. Resposta antiga não pode recolocar bookId recém-adicionado. Filtrar exclusões confirmadas locais até a revalidação atual; limpar/reconciliar quando houver remoção explícita da biblioteca.
4. Em 409 de add, tratar como já existente e retirar cartão, sem anunciar criação de novo vínculo. Cobrir também corrida de restrição única no backend.
5. Falha no add mantém sugestão e oferece retry. Sem reposição até próxima seleção diária.
6. Em retorno do detalhe/biblioteca ou foco da aba, GET aplica pertencimento atual. Eventos de mutação em lib/library-events.ts notificam a aba; comunicação entre abas, se usada, carrega apenas invalidação. Nunca biblioteca, títulos ou tokens.
7. Logout/troca de userId cancela requests e apaga lista. Não persistir recomendações no localStorage nem reutilizar estado entre contas.

Remover posteriormente o livro da biblioteca pode fazê-lo reaparecer se ele já constava da seleção diária. Isso não recalcula interesses nem escolhe um novo candidato.

## Acessibilidade e verificação

Região rotulada, controles com nomes claros, foco visível, Tab/Shift+Tab naturais, Enter/Espaço nas ações, anúncio discreto de inclusão/falha e respeito a prefers-reduced-motion. Não depender de hover/capa/cor. Verificar celular de 390px, desktop e teclado. Sem autoplay não é necessário botão de pausar rotação.

## Operações existentes preservadas

POST /user-books para inclusão (status TOREAD, conforme cliente atual), GET /books/{id} para detalhes e gate próprio de leitura/assinatura. Recomendar um livro não concede acesso de leitura ou muda a assinatura.
