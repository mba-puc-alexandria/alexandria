# User Stories — Alexandria

**Projeto:** Alexandria — Biblioteca Digital
**Instituição:** PUC-SP — MBA em Engenharia de Software
**Autoria:** Ronaldo Luiz
**Escritas em:** anterior ao início do desenvolvimento (fase de concepção do MVP)
**Documentado em:** 08/08/2026

---

## Sobre este documento

Estas oito histórias de usuário foram escritas na concepção do produto, antes do início do desenvolvimento. **Nunca foram registradas como issue no Jira nem versionadas no repositório em formato de história** — o backlog do Jira acabou recebendo tarefas técnicas em vez destas histórias.

Elas não foram, porém, simplesmente esquecidas: serviram de base para os mockups desenhados no **Figma**, que por sua vez definiram o layout do sistema. Essa ponte fica visível na issue `SCRUM-4` ("Design de Mockups", concluída, com PR vinculado), cujas 5 telas anexadas — Tela principal, Biblioteca/acervo pessoal, Cadastro de livros, Empréstimos, Explorar — correspondem diretamente aos épicos destas histórias (Biblioteca do Usuário, Catálogo de Livros, Empréstimo de Livros). A exceção é "Cadastro de livros", tela que existe no layout mas não tem história correspondente nestas oito — está mais alinhada ao RF01 do Jira.

Ou seja: o conteúdo das histórias **influenciou o produto de fato construído**, só não foi formalizado como história no backlog — o gap não é de conhecimento, é de registro.

Este documento cumpre três funções:

1. **Preservar** as histórias no repositório, encerrando sua existência apenas informal
2. **Avaliá-las** contra os critérios da disciplina de Testes de Software (INVEST, critérios de aceite testáveis, BDD)
3. **Confrontá-las** com o produto efetivamente construído

> **Nota importante:** estas histórias representam o escopo concebido para o MVP. O produto evoluiu significativamente desde então (leitor EPUB, autenticação Google, integração Gutendex, dashboard) e as histórias não foram atualizadas. A seção 4 mapeia essa divergência.

---

## 1. As histórias, como foram escritas

### 🔐 Épico: Acesso

#### US01 — Login simplificado

> **Como** usuário
> **Quero** entrar no aplicativo usando meu nome ou email
> **Para** acessar minha biblioteca pessoal

**Critérios de aceite**
- Existe uma tela de login
- O usuário entra sem senha (modo demo)
- Após login, é redirecionado para "Minha Biblioteca"

---

### 📚 Épico: Biblioteca do Usuário

#### US02 — Visualizar minha biblioteca

> **Como** usuário
> **Quero** ver todos os livros que adicionei à minha biblioteca
> **Para** acompanhar minhas leituras

**Critérios de aceite**
- Lista de livros em formato de cards
- Cada livro mostra título, autor e status
- Existe estado vazio (biblioteca sem livros)

#### US03 — Ver status de leitura

> **Como** usuário
> **Quero** identificar o status de cada livro
> **Para** organizar minha leitura

**Critérios de aceite**
- Status possíveis: "Quero ler", "Lendo", "Lido"
- Status visível no card do livro

---

### 🔍 Épico: Catálogo de Livros

#### US04 — Explorar catálogo

> **Como** usuário
> **Quero** explorar um catálogo de livros
> **Para** descobrir novos títulos

**Critérios de aceite**
- Tela separada do catálogo
- Lista de livros com capa, título, autor e gênero
- Campo de busca visível

#### US05 — Adicionar livro à biblioteca

> **Como** usuário
> **Quero** adicionar um livro do catálogo à minha biblioteca
> **Para** lê-lo futuramente

**Critérios de aceite**
- Botão "Adicionar à biblioteca"
- Feedback visual de sucesso
- Livro aparece na biblioteca

---

### 📖 Épico: Detalhe do Livro

#### US06 — Ver detalhes de um livro

> **Como** usuário
> **Quero** ver detalhes completos de um livro
> **Para** decidir se quero adicioná-lo ou emprestá-lo

**Critérios de aceite**
- Tela com capa, título, autor, editora e descrição
- Ações claras disponíveis

---

### 🔁 Épico: Empréstimo de Livros

#### US07 — Emprestar livro

> **Como** usuário
> **Quero** emprestar um livro da minha biblioteca para outro usuário
> **Para** compartilhar leituras

**Critérios de aceite**
- Modal ou tela de empréstimo
- Seleção de usuário
- Prazo fixo (ex: 7 dias)

#### US08 — Visualizar empréstimos

> **Como** usuário
> **Quero** ver livros que emprestei e recebi
> **Para** acompanhar devoluções

**Critérios de aceite**
- Tela "Meus Empréstimos"
- Aba "Emprestados por mim"
- Aba "Emprestados para mim"

---

## 2. Avaliação contra os critérios da disciplina

### 2.1 O que estas histórias acertam

| Critério | Aula | Avaliação |
|---|---|---|
| Formato "Como / Quero / Para" | 2 §10 | ✅ **8 de 8** — todas expressam persona, ação e benefício |
| Critérios de aceite presentes | 2 §11 | ✅ **8 de 8** |
| Comportamento observável | 2 §11 | ✅ Maioria — "Livro aparece na biblioteca", "Status visível no card" são verificáveis |
| Agrupamento em épicos | 3 §6 | ✅ 4 épicos **por domínio de negócio**, não por camada técnica |
| Valor perceptível ao usuário | 2 §10 (V) | ✅ Todas entregam algo que o usuário percebe |

**Comparação relevante:** os épicos destas histórias (Biblioteca do Usuário, Catálogo, Detalhe do Livro, Empréstimo) são agrupamentos de **negócio**. Os épicos registrados no Jira são "Frontend React" e "Desenvolvimento Backend" — agrupamentos de **camada técnica**, que a Aula 3 §6 desaconselha.

Estas histórias são estruturalmente **superiores ao que está registrado na ferramenta de gestão**. O problema não foi de conhecimento — foi de passagem da história para a ferramenta.

### 2.2 Avaliação INVEST (Aula 2 §10)

| US | I | N | V | E | S | T | Observação |
|---|---|---|---|---|---|---|---|
| US01 Login | 🔴 | 🟢 | 🟢 | 🟢 | 🟢 | 🟢 | Bloqueia todas as demais — dependência inevitável |
| US02 Ver biblioteca | 🟡 | 🟢 | 🟢 | 🟢 | 🟢 | 🟢 | Depende de US05 para ter conteúdo |
| US03 Status de leitura | 🟡 | 🟢 | 🟢 | 🟢 | 🟢 | 🟢 | Fortemente acoplada a US02 |
| US04 Explorar catálogo | 🟢 | 🟢 | 🟢 | 🟢 | 🟢 | 🟢 | A mais bem formada do conjunto |
| US05 Adicionar livro | 🟢 | 🟢 | 🟢 | 🟢 | 🟢 | 🟡 | "Feedback visual de sucesso" não é objetivo |
| US06 Detalhes do livro | 🟢 | 🟢 | 🟡 | 🟢 | 🔴 | 🟡 | Pequena demais — é mais um critério de aceite de US04 |
| US07 Emprestar livro | 🟢 | 🟢 | 🟢 | 🟡 | 🟡 | 🟡 | "Seleção de usuário" pressupõe regra não definida |
| US08 Ver empréstimos | 🟡 | 🟢 | 🟢 | 🟢 | 🟢 | 🟢 | Depende de US07 |

**Resumo:** 6 das 8 passariam num *Definition of Ready* razoável. As exceções são US06 (pequena demais, deveria ser absorvida por US04) e US01 (dependência estrutural, aceitável por ser história de fundação).

Para comparação: as três issues tipadas como "História" no Jira (`Design de Mockups`, `Desenhar Arquitetura`, `Definir Schema`) **reprovariam em todos os seis critérios**.

### 2.3 Gaps identificados

#### Gap 1 — Uma persona única e genérica

As oito histórias dizem **"Como usuário"**. A pergunta 3 do professor é justamente sobre personas mapeadas.

O próprio `docs/documentacao.md:67` já identifica quatro perfis distintos, e eles se comportam de forma diferente:

| Persona sugerida | Histórias que a caracterizam |
|---|---|
| **Leitor frequente** — acervo grande, organiza leituras | US02, US03, US05 |
| **Estudante / pesquisador** — controla leituras acadêmicas | US04, US06 |
| **Membro de clube de leitura** — empresta e toma emprestado | US07, US08 |
| **Colecionador** — cataloga edições e raridades | US06 |

Diferenciar a persona em cada história é trabalho de menos de uma hora e responde diretamente a uma das quatro perguntas da sessão.

#### Gap 2 — Critérios de aceite sem métrica objetiva

A Aula 3 §11 exige objetividade e precisão, e cita explicitamente termos a evitar: "rápido", "fácil", "adequado", "correto".

| US | Critério problemático | Por quê |
|---|---|---|
| US05 | "Feedback visual de sucesso" | Não define o quê, onde, nem por quanto tempo |
| US06 | "**Ações claras** disponíveis" | "Claras" é subjetivo — é a palavra que o professor critica em aula |
| US07 | "Modal **ou** tela de empréstimo" | Ambíguo: permite duas implementações diferentes |
| US02 | "Existe estado vazio" | Não define o que o estado vazio deve comunicar |

Nenhuma das oito tem critério de desempenho, disponibilidade ou taxa de sucesso.

#### Gap 3 — Ausência de caminhos de exceção

A Aula 2 §11 exige "condições de entrada, regras de negócio aplicadas e **caminhos de exceção**". Perguntas sem resposta:

- **US05** — e se o livro já estiver na biblioteca do usuário?
- **US07** — e se o livro já estiver emprestado? Pode emprestar um livro com status "Lendo"?
- **US08** — o que acontece quando o prazo de 7 dias vence? Existe estado "atrasado"?
- **US03** — mudar o status de "Lido" para "Quero ler" é permitido? Reseta o progresso?
- **US01** — o que acontece se o email não existir?

#### Gap 4 — Não estão em BDD/Gherkin

O professor menciona BDD nas quatro aulas e a Aula 5 vai aprofundar a derivação de casos de teste a partir das histórias. A conversão é mecânica. Exemplo com US05:

```gherkin
Funcionalidade: Adicionar livro à biblioteca

  Cenário: Adicionar livro disponível no catálogo
    Dado que estou autenticado e na tela de detalhe de um livro
    E esse livro não está na minha biblioteca
    Quando eu clicar em "Adicionar à biblioteca"
    Então devo ver a confirmação "Livro adicionado" em até 2 segundos
    E o livro deve aparecer em "Minha Biblioteca" com status "Quero ler"

  Cenário: Tentar adicionar livro já presente na biblioteca
    Dado que estou na tela de detalhe de um livro
    E esse livro já está na minha biblioteca
    Então o botão "Adicionar à biblioteca" deve estar desabilitado
    E devo ver o texto "Já está na sua biblioteca"
```

Escritas assim, as histórias servem diretamente como roteiro de teste automatizado — que é o ganho de 80% de tempo que a Aula 3 §11 menciona.

---

## 3. Status de implementação

Confrontando cada história com o código efetivamente entregue:

| US | História | Status | Evidência no código |
|---|---|---|---|
| US01 | Login simplificado | 🟡 **Implementado diferente** | Existe login, mas **com senha** (JWT + BCrypt) e OAuth Google — não é o "modo demo sem senha" especificado |
| US02 | Visualizar minha biblioteca | ✅ Implementado | `/biblioteca`, domínio `userbook`, `GET /user-books` |
| US03 | Ver status de leitura | ✅ Implementado | `UserBooksStatus` (enum de domínio) |
| US04 | Explorar catálogo | ✅ Implementado | `/explorar`, `GET /books`, `GET /books/search`, integração Gutendex |
| US05 | Adicionar livro à biblioteca | ✅ Implementado | `AddUserBooksUseCase`, `POST /user-books` |
| US06 | Ver detalhes de um livro | ✅ Implementado | `/explorar/[id]`, `GET /books/{id}` |
| US07 | Emprestar livro | 🔴 **Não implementado** | **Não existe domínio de empréstimo no backend.** Domínios: `author`, `book`, `user`, `userbook`, `shared` |
| US08 | Visualizar empréstimos | 🔴 **Não implementado** | Existe a tela `/emprestimos` no frontend, sem backend correspondente |

**O épico de Empréstimo inteiro não foi construído.** Duas das oito histórias — e uma tela em produção que não tem persistência por trás.

### Funcionalidades entregues sem história correspondente

O inverso também é verdadeiro: o produto tem funcionalidades relevantes que **nenhuma história descreve**.

| Funcionalidade em produção | História |
|---|---|
| **Leitor EPUB** (`/leitor/[id]`, `useEpub`, `epub-cache`) | ❌ Nenhuma |
| **Progresso de leitura em percentual** | ❌ Nenhuma |
| **Restauração da posição de leitura** | ❌ Nenhuma |
| Dashboard com métricas e metas | ❌ Nenhuma |
| Autenticação com Google | ❌ Nenhuma |
| Configurações de perfil | ❌ Nenhuma |
| Sincronização automática com Gutendex (job) | ❌ Nenhuma |
| Tela de suporte | ❌ Nenhuma |

O leitor EPUB é hoje a funcionalidade central do produto, concentra os bugs conhecidos (`leitor/`), tem o único CRITICAL do SonarCloud e foi alvo da última branch de trabalho — **e não existe em nenhuma definição de requisito do projeto**.

---

## 4. 🔴 Três fontes de requisitos divergentes

Somando este documento aos já existentes, o projeto tem **três definições paralelas de requisito**, criadas em momentos diferentes, que não se referenciam e não coincidem:

| # | Fonte | Formato | Onde vive | Estado |
|---|---|---|---|---|
| 1 | **Estas user stories** | US01–US08 com épicos e critérios de aceite | Nenhum lugar — informal até hoje | Épico de Empréstimo nunca construído |
| 2 | **Jira `SCRUM-17`** | RF01–RF10 + RNF01–RNF09 | Jira, concluída em 28/04/2026 | RNF03 exige Vite; projeto usa Next.js |
| 3 | **`docs/documentacao.md`** | RF01–RF10 + RNF01–RNF07 | Repositório | RF08 fala em PDF; sistema entrega EPUB |

E os identificadores colidem: **RF08 é "Registrar empréstimo" no Jira e "Leitor de PDF" na documentação**. Nenhuma das três descreve o produto em produção.

Isso viola diretamente o atributo de **consistência** da Aula 2 §9 e torna impossível construir a matriz de rastreabilidade requisito → caso de teste → evidência (Aula 3 §12).

---

## 5. Recomendações

| # | Ação | Efeito | Esforço |
|---|---|---|---|
| 1 | **Eleger estas user stories como fonte única de requisito** e descontinuar as duas listas RF | Resolve o conflito de três fontes; parte do artefato mais bem formado | 30 min (decisão) |
| 2 | Diferenciar as personas nas 8 histórias, usando os 4 perfis já mapeados | Responde à pergunta 3 do professor | 1h |
| 3 | Escrever as histórias faltantes: leitor EPUB, progresso de leitura, dashboard, autenticação Google, configurações | Cobre o produto real; ~6 histórias novas | 3h |
| 4 | Substituir critérios subjetivos por métricas objetivas | Atende Aula 3 §11 | 1h |
| 5 | Adicionar caminhos de exceção a cada história | Atende Aula 2 §11 | 1h30 |
| 6 | Converter os critérios para BDD/Gherkin | Viabiliza derivar casos de teste automatizados | 2h |
| 7 | Registrar as histórias na ferramenta de gestão escolhida, com vínculo a casos de teste | Fecha a cadeia de rastreabilidade | 2h |
| 8 | Decidir sobre o épico de Empréstimo: implementar, ou remover a tela `/emprestimos` do produto | Elimina funcionalidade fantasma em produção | Decisão do grupo |

O item **8 merece atenção especial na sessão**: existe uma tela em produção sem backend. Pela Aula 4 §10, isso é risco não mapeado — um usuário real pode acessar `/emprestimos` e interagir com algo que não persiste.

---

## 6. O que dizer ao professor

Reformulando a resposta à pergunta 2 do item 15 da Aula 4:

> O grupo escreveu oito histórias de usuário no formato correto, com critérios de aceite e agrupadas em quatro épicos de negócio, antes de iniciar o desenvolvimento. Elas guiaram o design: viraram mockups no Figma, cujas telas (registradas na issue `SCRUM-4`) definiram o layout do sistema. O que não aconteceu foi o registro formal — elas nunca viraram issue de história no Jira, que acabou recebendo tarefas técnicas no lugar delas. O resultado é que existem hoje três definições de requisito paralelas e divergentes, e nenhuma descreve integralmente o produto em produção: seis das oito histórias foram implementadas (uma delas de forma diferente da especificada), o épico de Empréstimo nunca foi construído apesar de ter tela em produção, e o leitor EPUB — funcionalidade central hoje — não aparece em nenhuma definição de requisito.

Essa resposta é substancialmente melhor que "não temos histórias". Mostra que o grupo **sabe escrever história de usuário** — o que falhou foi a passagem da história para a ferramenta e a manutenção ao longo do tempo. É um problema de processo, não de conhecimento, e é exatamente o tema da disciplina.

---

## Documentos relacionados

- [`alinhamento-projeto-integrador.md`](alinhamento-projeto-integrador.md) — Análise do projeto contra as Aulas 1 a 4
- [`analise-jira.md`](analise-jira.md) — Análise do backlog no Jira
- [`analise-sonarcloud.md`](analise-sonarcloud.md) — Análise de qualidade de código
- [`documentacao.md`](documentacao.md) — Documentação funcional (contém a terceira lista de requisitos)

---

*Documento gerado em 08/08/2026. Histórias de autoria de Ronaldo Luiz, escritas na concepção do MVP; avaliação e mapeamento de implementação elaborados a partir da leitura do código-fonte.*
