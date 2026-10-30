# Contrato do Painel

## Posição e apresentação

Seção “Você também pode gostar” após “Lendo Agora”, com carregamento e falha independentes das estatísticas. Não exibir data, quantidade de sugestões nem motivo. Quando stale, informar “Atualização pendente — mostrando a última seleção disponível”. Até 30 livros no estado do componente, responsivos e com controles de navegação sempre visíveis ao toque e teclado.

Cartão igual ao CoverCard efetivamente usado em Explorar: somente capa ou substituto em proporção 2:3. Ao passar o mouse, exibir a mesma sobreposição escura com blur, título, autores e botão circular +. Nas recomendações, título e autor permanecem visíveis abaixo da capa por solicitação final do usuário; não há botões abaixo. O Explorar mantém sua apresentação atual sem legenda. Clique na capa abre /explorar/{id}; o + adiciona sem navegar. Foco por teclado também revela a sobreposição. Em dispositivos sem hover, o + fica visível sobre a capa nítida; fundo/blur e textos da sobreposição aparecem apenas no foco. Compartilhar a apresentação entre Explorar e recomendações para preservar a consistência. A API mantém seu DTO compacto próprio, inclusive metadados internos de justificativa e seleção.

Não exibir contagem visual, inclusive “X de N”. Um status sr-only pode informar a posição para leitores de tela. Não prometer renovação de todos os títulos diariamente: os melhores podem permanecer iguais se o perfil/base não mudou.

## Estados

Precedência: READY de hoje é reutilizada, mesmo se o perfil mudar depois. Sem READY de hoje, NO_PROFILE prevalece sobre uma seleção antiga; STALE é permitido somente se houver sinais positivos atuais. O frontend segue esse estado do backend, sem montar perfil a partir da primeira página da biblioteca.

| Estado recebido | Comportamento |
| --- | --- |
| carregando inicialmente | indicador sem bloquear outras seções |
| READY | lista circular de itens retornados |
| EMPTY + NO_PROFILE | “Ainda não encontramos interesses suficientes para recomendar. Explore o catálogo e adicione livros que quer ler.” e Explorar; não pressupor biblioteca vazia |
| EMPTY + NO_MATCHES | sem sugestões compatíveis na seleção diária; link Explorar |
| EMPTY + ALL_ADDED_OR_UNAVAILABLE | seleção esgotada; informar próxima atualização diária |
| STALE | aviso de atualização pendente, com ou sem itens e sem data; filtro atual preservado; “Verificar atualização” faz somente GET |
| GENERATING / 202 | “Preparando suas recomendações”; polling limitado por Retry-After |
| 503 ou falha de rede | preservar cartões anteriores; mensagem e nova tentativa |
| 401 | seguir fluxo de autenticação; apagar dados e cancelar requisições |

Polling: no máximo 10 consultas automáticas consecutivas por abertura, com intervalo >= Retry-After (default 3s). Após isso manter mensagem e permitir nova tentativa; jamais laço ilimitado. STALE não inicia polling contínuo; revalidar no foco/retorno e após tentativa explícita. Botão “Tentar novamente” usa POST /recommendations/generation somente quando generationStatus=FAILED, respeitando retryAfterSeconds; em IDLE/PENDING/BUILDING, “Verificar atualização” faz somente GET; falha de transporte com seleção pronta repete apenas GET.

## Navegação circular

- Vários cartões visíveis por grupo conforme a largura disponível (dois em 390px, três a partir de sm, quatro em lg e cinco em xl). Avanço de um grupo, com último grupo parcial para não repetir livros antes do fim da volta. Somente os cartões do grupo visível participam do foco e da árvore de acessibilidade; desmontar ou aplicar hidden aos demais.
- Volta sem repetição refere-se a avanços consecutivos em Próximo; Anterior pode reapresentar um grupo. Com um item, manter o cartão. Anunciar a posição somente via sr-only; sem indicador visual ou contagem de sugestões.
- Avançar e voltar alteram o grupo na lista retornada. Ao fim, avançar vai ao início sem rede, sem novo ranking e sem crescer o DOM. Controles no cabeçalho têm nomes acessíveis Anterior/Próximo, cursor pointer e foco visível.
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
5. Falha transitória no add mantém sugestão e oferece retry. Indisponibilidade definitiva confirmada retira o cartão, informa “Este livro não está mais disponível” e revalida via GET, preservando foco e mostrando vazio se era o último. Não repor. Não inferir indisponibilidade de qualquer 404 ou texto livre: usar código BOOK_NOT_FOUND da operação ou exclusão confirmada pela revalidação autoritativa.
6. Em retorno do detalhe/biblioteca ou foco da aba, GET aplica pertencimento atual. Eventos de mutação em lib/library-events.ts notificam a aba; comunicação entre abas, se usada, carrega apenas invalidação. Nunca biblioteca, títulos ou tokens.
7. Logout/troca de userId cancela requests e apaga lista. Não persistir recomendações no localStorage nem reutilizar estado entre contas.

Remover posteriormente o livro da biblioteca pode fazê-lo reaparecer se ele já constava da seleção diária. Isso não recalcula interesses nem escolhe um novo candidato.

## Acessibilidade e verificação

Por solicitação do usuário em 2026-10-30, data, quantidade e razão não aparecem na interface. selection.referenceDate e reason continuam no contrato de API para rastreabilidade histórica, sem alterar ranking ou persistência. Mudança de nota/remoção da evidência não altera motivos congelados do dia, inclusive em STALE.

Região rotulada, controles com nomes claros, foco visível, Tab/Shift+Tab naturais, Enter/Espaço nas ações, anúncio discreto de inclusão/falha e respeito a prefers-reduced-motion. Oferecer nomes acessíveis para capa e botão +; o foco por teclado revela a mesma sobreposição do hover. Verificar celular de 390px, desktop e teclado. Sem autoplay não é necessário botão de pausar rotação.

## Operações existentes preservadas

POST /user-books para inclusão (status TOREAD, conforme cliente atual), GET /books/{id} para detalhes e gate próprio de leitura/assinatura. Recomendar um livro não concede acesso de leitura ou muda a assinatura.

## Seleção anterior vazia e atualização

STALE pode ter items=[] por seleção originalmente vazia ou filtragem atual. Mostrar “A atualização de hoje está pendente. Ainda não há sugestões disponíveis”, sem data, com Explorar e Verificar atualização (GET), sem prometer aguardar amanhã. generationStatus distingue PENDING/BUILDING de FAILED; só FAILED oferece POST após cooldown. Mesma regra com cartões STALE. EMPTY de READY de hoje é final do dia e não oferece POST para repor.

generationStatus=IDLE indica trabalho ainda não aceito pela fila; GET pode solicitá-lo dentro do orçamento automático. FAILED permite retry manual; retryAfterSeconds é cooldown restante (null se já elegível). Falha de transporte com seleção pronta repete GET. Após dez polls GENERATING, manter Verificar atualização sem afirmar falha técnica.

## Indisponibilidade confirmada na ação

O POST /user-books existente lança BookNotFoundException para livro ausente, mapeado hoje para 404 genérico; addUserBook hoje descarta esses detalhes. A implementação deve acrescentar código opcional BOOK_NOT_FOUND apenas a esse erro e preservá-lo no cliente, mantendo os campos/contratos atuais. Um 404 sem esse código é erro não classificado: preservar cartão, informar falha e revalidar GET, retirando-o somente se a resposta autoritativa o excluir. Falha de transporte/5xx não confirma remoção. Livro existente que perdeu URL digital pode ser incluído pela operação atual; após sucesso, aplicar remoção normal e GET, sem alterar regras de inclusão/acesso.
