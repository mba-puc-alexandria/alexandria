# Feature Specification: Recomendações de livros no Painel

**Feature Branch**: `feature/002-recomendacoes-painel-wip` (criada a partir da develop a pedido do usuário).

**Created**: 2026-10-30

**Status**: Implementada e validada no ambiente local de referência em 2026-10-30. As 52 tarefas estão concluídas; SC-007 foi dispensado pelo usuário. Evidências e limites em [validation-results.md](validation-results.md); escopo executado em [tasks.md](tasks.md). Preparação histórica em [implementation-readiness.md](implementation-readiness.md).

**Input**: Recomendar livros de acordo com o perfil de leitura do usuário, considerando os livros presentes em sua biblioteca nos estados concluído, lendo e a ler. Exibir o motor de recomendações no Painel. Esclarecimento do usuário: recomendar somente livros novos do catálogo, e não priorizar livros da lista a ler.

## Clarifications

### Session 2026-10-30

- Q: As recomendações devem incluir livros cadastrados localmente, mesmo quando não possuem versão digital para ler no Alexandria? → A: Não existem mais livros cadastrados localmente no Alexandria; o catálogo contém somente livros digitais.
- Q: Qual volume devemos considerar para validar o desempenho da primeira versão? → A: O usuário definiu que o motor deve considerar o catálogo inteiro e oferecer um carrossel infinito, desde que todos os livros recomendados façam parte do estilo do leitor. Nenhum volume numérico foi definido; o dimensionamento do ensaio fica para o planejamento.
- Q: Quando o leitor já tiver visto todos os livros compatíveis disponíveis, o que o carrossel deve fazer? → A: Reiniciar o carrossel, repetindo os livros já apresentados; se o leitor adicionar um livro à biblioteca, esse livro deve sair das sugestões, inclusive nas próximas voltas.

- Atualização de escopo durante o planejamento: o usuário definiu uma única seleção diária de até 30 livros por leitor, considerando toda a base. O carrossel repete essa seleção ao chegar ao fim. Esta decisão substitui a navegação sem limite total e a proposta intermediária de duas atualizações por dia. Livros adicionados à biblioteca continuam saindo imediatamente; a seleção dos substitutos fica para a atualização seguinte.

- Decisão durante a implementação em 2026-10-30: o usuário dispensou o teste de usabilidade com cinco participantes e preferiu iterar depois. Esse aceite humano e a sessão qualitativa com participantes deixam de bloquear a entrega. Permanecem os requisitos de acessibilidade, clareza das justificativas, teclado e celular, com validação automatizada. Esta decisão substitui referências anteriores a T051/SC-007 como gate humano obrigatório.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Descobrir livros compatíveis com meus interesses (Priority: P1)

Como leitor autenticado, quero encontrar no Painel livros do catálogo relacionados aos meus interesses, para descobrir minha próxima leitura sem precisar procurar manualmente em todo o acervo.

**Why this priority**: É o objetivo central da funcionalidade e entrega valor mesmo antes das ações de inclusão direta e dos estados específicos para leitores sem histórico.

**Independent Test**: Preparar uma biblioteca com interesses conhecidos e um catálogo com candidatos relacionados e não relacionados; abrir o Painel e verificar os títulos selecionados, sua ordem e as justificativas.

**Acceptance Scenarios**:

1. **Given** um leitor com livros na biblioteca e candidatos relacionados disponíveis no catálogo, **When** acessa o Painel, **Then** vê a seção “Recomendados para você”, abaixo de “Lendo Agora”, em um carrossel com até 30 livros da seleção diária, sem incluir qualquer título já presente em sua biblioteca.
2. **Given** um leitor com livros concluídos, lendo e a ler, **When** recebe recomendações, **Then** os três estados são considerados como sinais de interesse, respeitando a avaliação dos concluídos.
3. **Given** dois interesses representados apenas por livros concluídos, um com nota 5 e outro com nota 1, e candidatos equivalentes nos demais fatores, **When** as recomendações são ordenadas, **Then** o interesse associado à nota 5 recebe prioridade e a nota 1 não é usada como justificativa positiva.
4. **Given** uma recomendação personalizada, **When** o leitor visualiza seu cartão, **Then** encontra título, autor, capa ou substituto visual e uma justificativa apoiada em assuntos ou autores de sua própria biblioteca.
5. **Given** dois leitores com bibliotecas diferentes, **When** cada um acessa o Painel, **Then** nenhum recebe informações ou justificativas que revelem a biblioteca ou as avaliações do outro.
6. **Given** candidatos compatíveis distribuídos por todo o catálogo, **When** a seleção diária é construída, **Then** o sistema considera a base inteira para escolher até 30 livros por afinidade, sem limitar a busca à primeira página ou aos mais populares.
7. **Given** uma biblioteca que ocupa mais de uma página e contém um livro na última página, **When** as sugestões são geradas, **Then** esse livro também é excluído e seus sinais de interesse são considerados.

---

### User Story 2 - Explorar e guardar uma recomendação (Priority: P2)

Como leitor, quero consultar os detalhes de uma sugestão e adicioná-la à biblioteca pelo Painel, para transformar uma descoberta em intenção de leitura.

**Why this priority**: Completa o caminho de descoberta até a inclusão, aproveitando as operações de biblioteca existentes.

**Independent Test**: A partir de uma recomendação disponível, abrir os detalhes e adicionar o livro; conferir a biblioteca e o desaparecimento da sugestão após confirmação.

**Acceptance Scenarios**:

1. **Given** um cartão de recomendação, **When** seleciona “Ver livro”, **Then** o leitor acessa os detalhes do título correspondente.
2. **Given** um livro recomendado que ainda não pertence à biblioteca, **When** seleciona “Adicionar à biblioteca” e a operação tem sucesso, **Then** o livro passa a constar como “a ler” e deixa de aparecer nas sugestões exibidas.
3. **Given** uma tentativa de inclusão que falha, **When** o resultado é apresentado, **Then** o leitor recebe uma mensagem compreensível e pode tentar novamente, sem uma confirmação falsa de inclusão.
4. **Given** um título incluído na biblioteca por outra aba, **When** o leitor tenta adicioná-lo novamente, **Then** não é criada duplicidade e o Painel atualiza a condição do título.
5. **Given** alteração de status, avaliação, inclusão ou remoção de livros na biblioteca, **When** o leitor volta ao Painel ou o recarrega, **Then** os livros que pertencem à biblioteca são excluídos imediatamente; a nova afinidade e a reposição da seleção passam a valer na próxima geração diária.
6. **Given** o Painel em celular ou computador, **When** o leitor percorre os cartões usando apenas teclado, **Then** consegue identificar a sugestão, abrir detalhes e acionar a inclusão, com foco visível e sem depender da imagem ou de cores.

---

### User Story 3 - Navegar pela seleção diária em ciclos (Priority: P1)

Como leitor, quero navegar continuamente por uma seleção diária de até 30 livros compatíveis, para rever minhas opções sem recalcular recomendações a cada avanço.

**Why this priority**: Concretiza o carrossel em ciclos e controla o custo do motor sem limitar a base analisada.

**Independent Test**: Gerar uma seleção com candidatos compatíveis no início, no meio e no fim do catálogo; navegar até o fim dos 30 selecionados e verificar a repetição, a estabilidade ao longo do dia e a exclusão de livros adicionados.

**Acceptance Scenarios**:

1. **Given** mais de 30 candidatos compatíveis, **When** ocorre a geração diária, **Then** são selecionados até 30 livros pela ordem de afinidade, após considerar todo o catálogo.
2. **Given** um livro de alta afinidade no final do catálogo, **When** a seleção diária é construída, **Then** ele pode integrar os 30 escolhidos e não é omitido pela posição de armazenamento.
3. **Given** menos de 30 livros compatíveis, **When** a seleção é construída, **Then** apenas esses livros são apresentados, sem completar a lista com títulos sem afinidade.
4. **Given** nenhum histórico positivo utilizável ou nenhum candidato compatível, **When** o leitor abre o Painel, **Then** vê um estado vazio explicativo e acesso à exploração do catálogo.
5. **Given** uma seleção diária pronta, **When** o leitor recarrega a página ou avança pelo carrossel no mesmo dia, **Then** essa seleção é reutilizada sem reconstrução motivada pela navegação.
6. **Given** todos os livros ainda elegíveis da seleção apresentados, **When** o leitor continua avançando, **Then** o carrossel reinicia automaticamente a seleção, sem repetir livros antes do fim da volta.
7. **Given** um livro adicionado à biblioteca com sucesso pelo Painel, **When** a inclusão é confirmada, **Then** todas as suas ocorrências saem imediatamente do carrossel e o livro não aparece nas próximas voltas nem após recarregar; não há reposição fora da geração diária.
8. **Given** todos os livros da seleção diária adicionados à biblioteca, **When** a última inclusão é confirmada, **Then** é exibido o estado vazio até a próxima geração diária, mesmo que existam outros candidatos no catálogo.
9. **Given** uma falha na geração diária e uma seleção anterior pronta, **When** o leitor acessa o Painel, **Then** pode usar a última seleção válida, com aviso de atualização pendente e exclusão atual dos livros da biblioteca; sem seleção anterior, vê uma mensagem de indisponibilidade temporária e nova tentativa.
10. **Given** a virada do período diário, **When** a nova seleção fica pronta, **Then** as mudanças de perfil e de catálogo passam a influenciar os livros selecionados, sem publicação parcial.
11. **Given** uma seleção antiga e nenhum sinal positivo atual na virada do dia, **When** o leitor acessa o Painel sem READY de hoje, **Then** recebe EMPTY/NO_PROFILE, sem seleção antiga e sem ranking; se surgirem sinais positivos depois, pode iniciar a primeira geração do dia.
12. **Given** READY de hoje e alteração posterior das avaliações que elimina os sinais positivos, **When** o leitor recarrega, **Then** a seleção de hoje continua sendo reutilizada com exclusões atuais; sem novos sinais no dia seguinte, aplica-se EMPTY/NO_PROFILE.

---

### Edge Cases

- Catálogo vazio ou todos os livros do catálogo já presentes na biblioteca: apresentar estado vazio sem sugerir os livros existentes.
- Apenas livros lendo ou a ler: permitir personalização por seus assuntos e autores, sem exigir uma conclusão ou avaliação prévia.
- Apenas concluídos com avaliações de 0 a 2: não inferir gosto positivo a partir da conclusão; apresentar ausência de recomendações personalizadas quando não houver outros sinais.
- Assuntos, idiomas, capa ou popularidade ausentes: usar os dados disponíveis, sem falha nem justificativas inventadas; popularidade ausente não pode ser tratada como maior popularidade.
- Livros com vários autores ou muitos títulos de um autor: não aplicar teto por autor que exclua títulos da seleção dos 30 mais compatíveis; o mesmo autor pode aparecer várias vezes se a ordem de afinidade o justificar.
- Seleção finita: repetir apenas os livros restantes da seleção diária; com apenas um, reapresentá-lo mediante avanço. Se todos forem adicionados, mostrar estado vazio sem completar a lista fora da atualização diária.
- Catálogo com livros em vários idiomas: priorizar os idiomas observados sem presumir exclusividade; coincidência apenas de idioma não justifica personalização.
- Notas 0 e 5: tratar ambas como avaliações válidas, sem confundir zero com ausência de informação.
- Livro removido ou indisponível entre a sugestão e a ação: informar a indisponibilidade, atualizar a seção e preservar o funcionamento do Painel.
- Leitor sem autenticação: não apresentar biblioteca, perfil inferido ou sugestões personalizadas de outra pessoa.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: O sistema DEVE disponibilizar recomendações no Painel do leitor autenticado, abaixo de “Lendo Agora”, sem exigir navegação para outra página para visualizar a lista.
- **FR-002**: O sistema DEVE formar o perfil de leitura usando a biblioteca completa do próprio leitor, considerando os estados concluído, lendo e a ler, independentemente da paginação da biblioteca.
- **FR-003**: O sistema DEVE atribuir sinal positivo forte aos concluídos com notas 4 ou 5, fraco aos de nota 3, intermediário aos livros lendo e leve aos livros a ler. Concluídos com notas de 0 a 2 NÃO DEVEM reforçar interesses positivos. Para características equivalentes, a prioridade dos sinais DEVE ser: concluído 4–5, lendo, a ler, concluído 3.
- **FR-004**: O sistema DEVE recomendar somente livros digitais disponíveis no catálogo que não pertençam à biblioteca do leitor em nenhum dos três estados. “Novos” significa novos para a biblioteca do leitor, sem restrição à data de publicação ou de inclusão no catálogo.
- **FR-005**: O sistema DEVE priorizar afinidade por assuntos, seguida de autores; idiomas observados podem complementar essa preferência e a popularidade por downloads pode desempatar candidatos de afinidade equivalente. Um candidato personalizado DEVE compartilhar ao menos um assunto ou autor associado a um sinal positivo.
- **FR-006**: O sistema DEVE considerar o catálogo inteiro para construir uma seleção diária de até 30 livros compatíveis por leitor. A posição no catálogo e a popularidade NÃO DEVEM restringir o universo avaliado; a seleção DEVE respeitar a ordem de afinidade e NÃO DEVE ser completada com livros sem afinidade. Recarregar o Painel ou avançar no carrossel NÃO DEVE reconstruir a seleção.
- **FR-007**: Cada recomendação DEVE apresentar título, autor, capa ou substituto visual e uma justificativa verificável nos dados reais da biblioteca do leitor na data da geração. A apresentação DEVE indicar essa data, sem afirmar gosto ou avaliação atuais; nenhuma sugestão geral sem afinidade comprovável DEVE ser incluída.
- **FR-008**: O leitor DEVE poder consultar os detalhes de cada livro recomendado.
- **FR-009**: O leitor DEVE poder adicionar um livro recomendado à biblioteca como “a ler”, receber confirmação apenas após sucesso e deixar de vê-lo nas recomendações imediatamente após essa confirmação, sem exigir recarregar a página. A exclusão DEVE abranger todos os cartões e as próximas voltas do carrossel, independentemente do status posterior do livro na biblioteca. Tentativas repetidas NÃO DEVEM gerar duplicidade.
- **FR-010**: A seleção DEVE ser gerada uma vez por dia por leitor; mudanças de avaliação, status e catálogo influenciam a próxima seleção diária. A exclusão dos livros atualmente na biblioteca DEVE valer em toda consulta, sem aguardar a próxima geração. Adicionar um livro NÃO DEVE gerar um substituto fora da atualização diária. Visualizar recomendações NÃO DEVE modificar o perfil ou os estados dos livros.
- **FR-011**: Sem seleção READY de hoje, o sistema DEVE verificar o perfil atual antes de usar uma seleção anterior: se não houver sinais positivos utilizáveis, DEVE mostrar EMPTY/NO_PROFILE sem sugestões antigas e sem iniciar ranking. Uma READY de hoje, mesmo vazia, DEVE ser reutilizada até o próximo dia, respeitando FR-010. Uma seleção diária sem candidatos compatíveis DEVE mostrar estado vazio explicativo e acesso à exploração do catálogo. NÃO DEVE substituir recomendações personalizadas por livros populares sem afinidade comprovável.
- **FR-012**: O sistema DEVE tratar dados incompletos sem interromper as sugestões e sem inventar preferências, metadados, notas ou relações entre livros.
- **FR-013**: A seção DEVE distinguir carregamento, resultados, ausência de candidatos e falha. Em caso de falha, DEVE oferecer nova tentativa e permitir o uso das demais seções do Painel.
- **FR-014**: O sistema DEVE usar somente os dados do leitor autenticado para formar seu perfil pessoal e suas justificativas, sem revelar ou usar bibliotecas privadas de outros leitores nessa personalização.
- **FR-015**: A seção DEVE permitir leitura e acionamento dos cartões por teclado e em telas de celular e computador, com identificação textual das ações e sem depender exclusivamente da capa ou de cores.

- **FR-016**: Após apresentar todos os livros elegíveis por avanços consecutivos em “Próximo”, o carrossel DEVE reiniciar a mesma seleção. Esses avanços NÃO DEVEM repetir antes de completar a volta; “Anterior” PODE reapresentar um livro. Com um item, avançar o reapresenta. Livros atualmente na biblioteca DEVEM permanecer excluídos. Sem itens na seleção de hoje, apresentar estado vazio até a próxima geração, sem substitutos. Seleção anterior vazia com atualização de hoje pendente DEVE informar a pendência e permitir verificar a atualização.

- **FR-017**: Uma geração em andamento ou com falha NÃO DEVE substituir a última seleção completa. Sem READY de hoje e havendo sinais positivos no perfil atual, a seleção anterior pode ser usada com aviso de atualização pendente e exclusões atuais, conforme a precedência de FR-011. O sistema DEVE impedir publicação duplicada para o mesmo leitor no mesmo período diário e permitir recuperação de uma tentativa interrompida.

### Key Entities *(include if feature involves data)*

- **Leitor**: Pessoa autenticada à qual pertencem a biblioteca e as preferências inferidas.
- **Livro do catálogo**: Livro digital disponível para descoberta; pode possuir autores, assuntos, idiomas, capa e quantidade de downloads. O catálogo atual não possui livros cadastrados localmente.
- **Livro na biblioteca**: Vínculo entre leitor e livro, com estado de leitura e avaliação quando concluído; determina sinais de interesse e exclusões.
- **Perfil de leitura inferido**: Conjunto de interesses extraídos dos dados do próprio leitor, com força de evidência conforme estado e avaliação; não exige edição manual nesta versão.
- **Seleção diária**: Conjunto ordenado de até 30 recomendações de um leitor para um dia, publicado somente quando completo; pode estar pendente, em geração, pronto ou com falha.
- **Recomendação**: Livro elegível selecionado para um leitor, sua posição e justificativa de afinidade com o perfil do leitor.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Em 100% dos cenários de aceitação, nenhum livro já presente na biblioteca aparece nas sugestões, inclusive quando a biblioteca ocupa múltiplas páginas.
- **SC-002**: Em cenários controlados com os demais fatores equivalentes, 100% das comparações respeitam a ordem dos sinais de FR-003 e notas de 0 a 2 nunca fundamentam uma justificativa positiva.
- **SC-003**: Em catálogos controlados com candidatos no início, no meio e no fim, a seleção diária coincide em 100% dos casos com os primeiros 30 candidatos elegíveis da ordenação de referência sobre a base completa, ou com todos quando houver menos de 30. Todo livro selecionado tem justificativa verificável de afinidade por assunto ou autor.
- **SC-004**: Em 100% dos cenários sem histórico utilizável ou sem candidatos compatíveis, o estado vazio é apresentado sem bloquear o Painel nem inserir sugestões gerais.
- **SC-005**: Em 100% das inclusões confirmadas, o livro consta como a ler e desaparece imediatamente de todas as ocorrências exibidas, permanece ausente nas voltas seguintes e não gera duplicidade na biblioteca.
- **SC-006**: Com seleção diária pronta, o p95 de exibição dos cartões ou do estado vazio final no navegador DEVE ser <=2s. Medir do início da navegação completa para o Painel até sua exibição após oportunidade de pintura, com cinco acessos de aquecimento e pelo menos 100 amostras válidas por perfil de execução; calcular p95 por nearest-rank sem excluir amostras lentas. Registrar falhas/timeouts separadamente, resolver antes de aprovar o ensaio e registrar navegador, máquina, viewport, rede, cache e leitores simultâneos. Tempo de geração diária é medido separadamente; o protocolo detalhado fica em quickstart.md.
- **SC-007**: Critério de aceite humano dispensado explicitamente pelo usuário em 2026-10-30 durante a implementação. Não realizar nem alegar aprovação de teste com cinco participantes. Acessibilidade e fluxos de navegação/inclusão continuam cobertos por FR-007, FR-009, FR-015 e seus testes; eventuais avaliações com leitores ficam para iterações futuras.
- **SC-008**: Em 100% dos cenários de separação entre contas e de falha da seção, nenhum dado privado de outro leitor é exposto e as demais funcionalidades do Painel permanecem utilizáveis.

- **SC-009**: Em 100% dos cenários com seleção diária não vazia, avanços consecutivos em “Próximo” repetem somente após esgotar a volta; voltar deliberadamente e reapresentar um único item são permitidos, sem reconstruções extras no dia. Ao adicionar o último livro elegível, passa ao estado vazio sem reposição imediata.

## Assumptions

- O escopo confirmado pelo usuário é recomendar somente livros novos para sua biblioteca a partir do catálogo existente; priorizar a lista a ler está fora do escopo.
- A primeira versão utiliza interesses inferidos da biblioteca. Coleta de gêneros preferidos, idiomas declarados ou objetivos de leitura por novo formulário fica fora do escopo.
- A escala de avaliação existente é de 0 a 5. Nota 0 é válida. As faixas e a ordem de influência descritas são defaults de produto propostos nesta especificação, ainda sujeitos à validação de qualidade com leitores.
- Concluir um livro não equivale a aprová-lo. Avaliações baixas não implicam bloquear para sempre seu autor ou assunto; apenas não reforçam interesses positivos nesta versão.
- O limite total é de 30 livros por seleção diária. O default do planejamento é um cartão visível por vez em celular e desktop, abaixo de Lendo Agora, sujeito à validação com leitores. Uma primeira geração para um leitor sem seleção no período conta como sua geração do dia; falhas podem ser repetidas sem publicar mais de uma seleção bem-sucedida no período.
- O catálogo existente contém somente livros digitais, sem livros cadastrados localmente, e é a única fonte de candidatos; a funcionalidade depende de sua disponibilidade e de seus metadados. Importar novos catálogos, enriquecer metadados e unificar edições duplicadas estão fora do escopo.
- A identidade do livro existente no catálogo define a exclusão. Reconhecer automaticamente edições ou traduções distintas como a mesma obra fica fora do escopo.
- As operações existentes de autenticação, consulta de detalhes e inclusão na biblioteca serão reutilizadas. A funcionalidade não altera regras de acesso à leitura nem benefícios de assinatura.
- Comparação entre leitores, recomendações sociais, sugestões gerais sem afinidade, botão de desinteresse, notificações e personalização por dados demográficos ficam fora do escopo.
- Não será atribuída preferência recente a uma avaliação ou mudança de status sem informação que comprove quando ocorreu; data de adição do livro não significa data de conclusão.
- Critérios de desempate estáveis e detalhes do cálculo serão definidos no planejamento. As regras de elegibilidade e a ordem de sinais aqui descritos permanecem verificáveis independentemente da técnica escolhida.


- Para o planejamento, adota-se geração diária às 00h no fuso America/Sao_Paulo como horário configurável, sem novo pedido de confirmação. Mudanças no perfil após a geração podem levar até a geração seguinte para alterar a seleção; novos leitores podem iniciar a primeira geração do dia ao abrir o Painel.

## Refinamentos da revisão crítica — 2026-10-30

- Ausência de perfil não pressupõe biblioteca vazia: explicar que ainda não foram identificados interesses suficientes e oferecer Explorar, sem incentivar avaliações artificiais.
- A avaliação qualitativa do top-5 com participantes foi proposta no refinamento inicial. Durante a implementação, o usuário dispensou essa sessão junto ao gate de usabilidade; futuras iterações podem coletar utilidade/interesse, rejeições, idiomas e repetição de autores, sem afirmar satisfação já comprovada.
