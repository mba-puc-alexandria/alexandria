# BDD — Aplicativo de Biblioteca Pessoal

Cenários escritos em Gherkin (Given/When/Then), organizados por Feature, com base nos Requisitos Funcionais (RF) e Não Funcionais (RNF) fornecidos.
Cada bloco pode ser usado como um arquivo `.feature` separado.

---

## Feature: Exploração e curadoria (RF01)

```gherkin
Funcionalidade: Tela de exploração com curadorias editoriais
  Como usuário do aplicativo
  Quero visualizar uma tela de exploração com curadorias
  Para descobrir novos livros de forma organizada

  Cenário: Acessar a tela de exploração
    Dado que o usuário está autenticado no aplicativo
    Quando ele acessa a aba "Explorar"
    Então o sistema deve exibir seções de curadoria editorial (ex: "Destaques da semana", "Mais lidos")
    E cada seção deve conter ao menos uma capa de livro com título e autor visíveis

  Cenário: Tela de exploração sem curadorias disponíveis
    Dado que não há curadorias cadastradas no momento
    Quando o usuário acessa a aba "Explorar"
    Então o sistema deve exibir uma mensagem informando a ausência de conteúdo curado
    E não deve exibir seções vazias ou quebradas
```

---

## Feature: Busca de livros (RF02)

```gherkin
Funcionalidade: Busca de livros por título, autor ou ISBN
  Como usuário do aplicativo
  Quero buscar livros por título, autor ou ISBN
  Para encontrar rapidamente uma obra específica

  Cenário: Buscar por título
    Dado que o usuário está na tela de busca
    Quando ele digita o título "Dom Casmurro" no campo de busca
    Então o sistema deve exibir os livros cujo título corresponda total ou parcialmente ao termo buscado

  Cenário: Buscar por autor
    Dado que o usuário está na tela de busca
    Quando ele digita o nome de um autor cadastrado
    Então o sistema deve exibir todos os livros associados a esse autor

  Cenário: Buscar por ISBN
    Dado que o usuário está na tela de busca
    Quando ele digita um código ISBN válido
    Então o sistema deve exibir o livro correspondente a esse ISBN

  Cenário: Busca sem resultados
    Dado que o usuário está na tela de busca
    Quando ele digita um termo que não corresponde a nenhum livro, autor ou ISBN cadastrado
    Então o sistema deve exibir uma mensagem de "nenhum resultado encontrado"
```

---

## Feature: Acervo pessoal com filtros (RF03)

```gherkin
Funcionalidade: Exibição do acervo pessoal com filtros por status
  Como usuário do aplicativo
  Quero visualizar meu acervo pessoal filtrado por status
  Para organizar minha leitura de forma eficiente

  Cenário: Visualizar acervo completo
    Dado que o usuário possui livros cadastrados em seu acervo
    Quando ele acessa a tela "Meu Acervo"
    Então o sistema deve listar todos os livros do usuário

  Cenário: Filtrar acervo por status "Lendo"
    Dado que o usuário está na tela "Meu Acervo"
    Quando ele seleciona o filtro "Lendo"
    Então o sistema deve exibir apenas os livros com status "Lendo"

  Cenário: Filtrar acervo por status "Lido"
    Dado que o usuário está na tela "Meu Acervo"
    Quando ele seleciona o filtro "Lido"
    Então o sistema deve exibir apenas os livros com status "Lido"

  Cenário: Filtrar acervo por status "Quero ler"
    Dado que o usuário está na tela "Meu Acervo"
    Quando ele seleciona o filtro "Quero ler"
    Então o sistema deve exibir apenas os livros com status "Quero ler"

  Cenário: Acervo vazio para o filtro selecionado
    Dado que o usuário está na tela "Meu Acervo"
    E não possui nenhum livro com o status "Emprestado"
    Quando ele seleciona o filtro "Emprestado"
    Então o sistema deve exibir uma mensagem indicando que não há livros nesse status
```

---

## Feature: Progresso de leitura (RF04)

```gherkin
Funcionalidade: Indicação do progresso de leitura
  Como usuário do aplicativo
  Quero visualizar o percentual de progresso de cada livro
  Para acompanhar meu avanço na leitura

  Cenário: Exibir progresso de um livro em andamento
    Dado que o usuário está lendo um livro e já concluiu 40% das páginas
    Quando ele visualiza o livro no acervo ou em sua página de detalhes
    Então o sistema deve exibir "40%" como progresso de leitura, com indicador visual (barra ou anel de progresso)

  Cenário: Exibir progresso de um livro não iniciado
    Dado que o usuário ainda não começou a leitura de um livro
    Quando ele visualiza esse livro no acervo
    Então o sistema deve exibir o progresso como "0%"

  Cenário: Exibir progresso de um livro concluído
    Dado que o usuário concluiu a leitura de um livro
    Quando ele visualiza esse livro no acervo
    Então o sistema deve exibir o progresso como "100%" com indicação visual de "Concluído"
```

---

## Feature: Dashboard de estatísticas e metas (RF05)

```gherkin
Funcionalidade: Dashboard com estatísticas de leitura e metas
  Como usuário do aplicativo
  Quero visualizar um dashboard com minhas estatísticas de leitura
  Para acompanhar meu desempenho e minhas metas

  Cenário: Visualizar estatísticas gerais
    Dado que o usuário possui histórico de leitura registrado
    Quando ele acessa a tela "Dashboard" ou "Estatísticas"
    Então o sistema deve exibir dados como número de livros lidos, páginas lidas e tempo médio de leitura

  Cenário: Acompanhar meta de leitura em andamento
    Dado que o usuário definiu uma meta de leitura (ex: 12 livros no ano)
    Quando ele acessa o dashboard
    Então o sistema deve exibir o progresso da meta em relação ao total definido (ex: "5 de 12 livros")

  Cenário: Usuário sem meta definida
    Dado que o usuário não configurou nenhuma meta de leitura
    Quando ele acessa o dashboard
    Então o sistema deve sugerir a criação de uma meta de leitura
```

---

## Feature: Registro de empréstimos (RF06)

```gherkin
Funcionalidade: Registro de empréstimos de livros
  Como usuário do aplicativo
  Quero registrar o empréstimo de um livro informando o nome do portador e o prazo
  Para controlar quem está com meus livros e quando devem ser devolvidos

  Cenário: Registrar um novo empréstimo com sucesso
    Dado que o usuário está na tela de detalhes de um livro disponível em seu acervo
    Quando ele seleciona "Registrar empréstimo"
    E informa o nome do portador "Maria Silva"
    E define o prazo de devolução para "20/09/2026"
    E confirma o registro
    Então o sistema deve salvar o empréstimo com os dados informados
    E o status do livro deve ser alterado para "Emprestado"

  Cenário: Tentar registrar empréstimo sem informar o nome do portador
    Dado que o usuário está registrando um empréstimo
    Quando ele deixa o campo "nome do portador" em branco
    E tenta confirmar o registro
    Então o sistema deve exibir uma mensagem de validação exigindo o preenchimento do campo
    E o empréstimo não deve ser registrado

  Cenário: Tentar registrar empréstimo sem definir prazo
    Dado que o usuário está registrando um empréstimo
    E já informou o nome do portador
    Quando ele não define um prazo de devolução
    E tenta confirmar o registro
    Então o sistema deve exigir a definição de um prazo antes de salvar
```

---

## Feature: Sinalização de empréstimos atrasados (RF07)

```gherkin
Funcionalidade: Sinalização visual de empréstimos atrasados
  Como usuário do aplicativo
  Quero identificar visualmente empréstimos atrasados
  Para cobrar a devolução dos livros emprestados

  Cenário: Empréstimo dentro do prazo
    Dado que um livro foi emprestado com prazo de devolução ainda não vencido
    Quando o usuário visualiza a lista de empréstimos
    Então o sistema não deve exibir nenhum alerta de atraso para esse item

  Cenário: Empréstimo com prazo vencido
    Dado que um livro foi emprestado e a data atual é posterior ao prazo de devolução definido
    Quando o usuário visualiza a lista de empréstimos
    Então o sistema deve sinalizar visualmente o item como "Atrasado" (ex: cor vermelha, ícone de alerta)

  Cenário: Empréstimo com prazo vencendo em breve
    Dado que um livro foi emprestado com prazo de devolução dentro dos próximos 2 dias
    Quando o usuário visualiza a lista de empréstimos
    Então o sistema deve sinalizar o item com um alerta de "vencimento próximo", diferenciado visualmente do alerta de atraso
```

---

## Feature: Leitor de PDF integrado (RF08)

```gherkin
Funcionalidade: Leitor de PDF integrado
  Como usuário do aplicativo
  Quero ler arquivos PDF diretamente no aplicativo
  Para não precisar de um aplicativo externo

  Cenário: Abrir um livro em PDF
    Dado que o usuário possui um livro em formato PDF em seu acervo
    Quando ele seleciona a opção "Ler agora"
    Então o sistema deve abrir o leitor de PDF integrado exibindo o conteúdo do arquivo

  Cenário: Retomar leitura a partir da última página
    Dado que o usuário já leu até a página 50 de um PDF anteriormente
    Quando ele reabre o leitor de PDF desse livro
    Então o sistema deve posicionar a leitura na página 50

  Cenário: Navegar entre páginas do PDF
    Dado que o usuário está com o leitor de PDF aberto
    Quando ele desliza ou navega para a próxima página
    Então o sistema deve exibir a página seguinte
    E deve atualizar o progresso de leitura do livro (ver RF04)

  Cenário: Arquivo PDF corrompido ou inválido
    Dado que o usuário tenta abrir um arquivo PDF corrompido
    Quando o sistema tenta carregar o leitor
    Então o sistema deve exibir uma mensagem de erro informando que o arquivo não pôde ser aberto
```

---

## Feature: Sugestões personalizadas de leitura (RF09)

```gherkin
Funcionalidade: Sugestões personalizadas de leitura
  Como usuário do aplicativo
  Quero receber sugestões de leitura personalizadas
  Para descobrir livros alinhados aos meus interesses

  Cenário: Exibir sugestões com base no histórico de leitura
    Dado que o usuário possui livros lidos e avaliados em seu histórico
    Quando ele acessa a tela de exploração ou o dashboard
    Então o sistema deve exibir uma seção de "Sugestões para você" com livros relacionados aos gêneros/autores já lidos

  Cenário: Usuário sem histórico suficiente para sugestões
    Dado que o usuário é novo e não possui histórico de leitura
    Quando ele acessa a seção de sugestões
    Então o sistema deve exibir sugestões genéricas (ex: mais populares) até que haja dados suficientes para personalização
```

---

## Feature: Uso em dispositivos móveis (RF10)

```gherkin
Funcionalidade: Funcionamento adequado em dispositivos móveis
  Como usuário do aplicativo
  Quero utilizar o sistema em meu smartphone
  Para acessar minha biblioteca em qualquer lugar

  Cenário: Acessar o sistema em um smartphone
    Dado que o usuário acessa o aplicativo em um dispositivo com tela a partir de 390px de largura
    Quando a tela é carregada
    Então todos os elementos de interface devem se adaptar ao tamanho da tela sem cortes ou sobreposições
    E os botões e áreas de toque devem ter tamanho adequado para interação por toque

  Cenário: Realizar ações principais em dispositivo móvel
    Dado que o usuário está utilizando o aplicativo em um smartphone
    Quando ele realiza ações como busca, registro de empréstimo e leitura de PDF
    Então todas as funcionalidades devem operar normalmente, sem necessidade de zoom ou rolagem horizontal
```

---

## Cenários de apoio — Requisitos Não Funcionais

Requisitos não funcionais não descrevem comportamentos de negócio isolados, mas critérios de qualidade que se aplicam de forma transversal às features acima. Abaixo eles são traduzidos em cenários de aceitação, para servirem de checklist de qualidade vinculado às features correspondentes.

```gherkin
Funcionalidade: Desempenho e usabilidade da interface

  Cenário: Tempo de carregamento da interface (RNF01)
    Dado que o usuário acessa o aplicativo em uma conexão de internet padrão
    Quando qualquer tela principal é carregada
    Então o tempo de carregamento não deve ultrapassar 3 segundos

  Cenário: Responsividade em telas pequenas (RNF02)
    Dado que o usuário acessa o aplicativo em um dispositivo com largura de tela de 390px
    Quando ele navega por qualquer tela do sistema
    Então o layout deve se adaptar corretamente, sem elementos cortados ou sobrepostos

  Cenário: Carregamento otimizado de fontes (RNF06)
    Dado que o usuário acessa o aplicativo pela primeira vez
    Quando a interface é carregada
    Então as fontes do Google Fonts devem ser carregadas de forma otimizada (ex: font-display: swap ou pré-carregamento)
    E o texto não deve ficar invisível durante o carregamento da fonte

  Cenário: Navegação sem recarregamento de página (RNF07)
    Dado que o usuário está navegando entre telas do aplicativo (ex: de "Explorar" para "Meu Acervo")
    Quando ele clica em um item de menu ou link interno
    Então a navegação deve ocorrer sem recarregamento completo da página (comportamento de SPA)

  Cenário: Consistência visual com os tokens de design (RNF05)
    Dado que uma tela do sistema é renderizada
    Quando o usuário observa cores, espaçamentos e tipografia
    Então esses elementos devem corresponder aos tokens de design definidos no Figma
```

> **Observação:** RNF03 (TypeScript com tipagem estrita) e RNF04 (componentes reutilizáveis e desacoplados) são requisitos de arquitetura/código e não geram comportamento observável pelo usuário final. Eles não se traduzem em cenários BDD, mas devem ser verificados via *code review*, linters (ex: `strict: true` no `tsconfig.json`) e análise de arquitetura de componentes.

---

## Resumo de rastreabilidade

| Requisito | Feature BDD correspondente |
|---|---|
| RF01 | Exploração e curadoria |
| RF02 | Busca de livros |
| RF03 | Acervo pessoal com filtros |
| RF04 | Progresso de leitura |
| RF05 | Dashboard de estatísticas e metas |
| RF06 | Registro de empréstimos |
| RF07 | Sinalização de empréstimos atrasados |
| RF08 | Leitor de PDF integrado |
| RF09 | Sugestões personalizadas de leitura |
| RF10 | Uso em dispositivos móveis |
| RNF01, RNF02, RNF06, RNF07, RNF05 | Cenários de apoio — Desempenho e usabilidade |
| RNF03, RNF04 | Verificação técnica (fora do escopo de cenários BDD) |
