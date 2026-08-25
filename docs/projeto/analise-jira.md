# Análise do Jira — Alexandria

**Projeto:** Alexandria — Biblioteca Digital
**Instituição:** PUC-SP — MBA em Engenharia de Software
**Data da análise:** 08/08/2026
**Site:** `https://alexandria-puc.atlassian.net`
**Projeto Jira:** Alexandria (chave `SCRUM`, board 1) — tipo *team-managed* (next-gen)

---

## Sumário Executivo

O grupo **possui** um Jira estruturado e o utilizou de forma real entre abril e maio de 2026, com sprints, épicos e distribuição de trabalho. O uso foi descontinuado a partir de meados de maio.

O achado mais grave não é o abandono em si, mas o que ficou para trás:

> **Existem três definições de requisito paralelas e divergentes** — as oito user stories escritas na concepção ([`user-stories.md`](user-stories.md)), a lista do Jira (`SCRUM-17`) e a de `docs/documentacao.md`. As duas últimas usam **os mesmos identificadores RF01–RF10 para requisitos completamente diferentes**.

| Indicador | Valor |
|---|---|
| Total de issues | **27** |
| Issues concluídas | 18 (67%) |
| Issues pendentes | 9 (33%) |
| Período de criação | 02/04/2026 a 14/05/2026 |
| Última atualização de qualquer issue | **18/06/2026** |
| Tempo desde a última movimentação | **~7 semanas** |
| Sprint 4 | **ainda "ativa"** — prazo terminou em 23/05/2026 |
| Issues no formato de história de usuário | **0** |
| Issues com critérios de aceite | **0** |
| Commits vinculados a issues | **0 de 229** |

---

## 1. Composição do backlog

### Por tipo

| Tipo | Qtd | Observação |
|---|---|---|
| Epic | 4 | Todos com descrição **nula** e todos ainda em "Tarefas pendentes" |
| História | 3 | Nenhuma é de fato uma história de usuário (ver seção 3) |
| Tarefa | 18 | Maioria absoluta do backlog |
| Bug | 2 | **As descrições mais completas de todo o projeto** |

### Por status

| Status | Qtd |
|---|---|
| Concluído | 18 |
| Tarefas pendentes | 9 |

### Épicos

| Chave | Épico | Status | Descrição |
|---|---|---|---|
| SCRUM-1 | MVP - Definições e planejamento | Tarefas pendentes | *(nula)* |
| SCRUM-2 | Frontend React | Tarefas pendentes | *(nula)* |
| SCRUM-3 | Autenticação e Autorização | Tarefas pendentes | *(nula)* |
| SCRUM-22 | Desenvolvimento Backend | Tarefas pendentes | *(nula)* |

**Problema estrutural:** três dos quatro épicos estão organizados por **camada técnica** ("Frontend React", "Desenvolvimento Backend") e não por entrega de valor de negócio. A Aula 3 §6 define Epic como *"módulo / grande entrega"* na camada de Management — um agrupamento de negócio, não de tecnologia.

A consequência prática é direta: com épicos organizados por camada, **é impossível responder "quanto do módulo de Empréstimos está coberto por testes?"** — porque Empréstimos não existe como agrupamento. Isso inviabiliza a métrica de cobertura por objetivo de negócio que a Aula 3 §13 propõe.

Além disso, **nenhum dos 4 épicos foi fechado**, mesmo com 18 issues filhas concluídas.

---

## 2. Linha do tempo e sprints

| Marco | Data |
|---|---|
| Primeiras issues criadas | 02/04/2026 |
| Épico "Desenvolvimento Backend" criado | 25/04/2026 |
| Sprint 3 — início | 25/04/2026 |
| Últimas issues criadas | 14/05/2026 |
| Sprint 3 — encerrada | 12/05/2026 |
| Sprint 4 — início | 12/05/2026 |
| Sprint 4 — fim previsto | **23/05/2026** |
| Última atualização de qualquer issue | 18/06/2026 |
| Data desta análise | 08/08/2026 |

### A Sprint 4 nunca foi encerrada

O dado mais eloquente sobre o abandono: a **Sprint 4 continua com `state: "active"`**, com data de término prevista para 23/05/2026. Está aberta há **77 dias além do prazo**.

Ambas as sprints (3 e 4) têm o campo **`goal` vazio** — não há objetivo de sprint declarado, o que remove o principal instrumento de alinhamento entre o time e a entrega (Aula 2 §4).

O trabalho não parou: entre junho e agosto o repositório recebeu features relevantes (leitor EPUB, configurações de perfil, autenticação Google, cache de EPUB, testes de carga). **O que parou foi o registro desse trabalho.**

---

## 3. Qualidade das issues — avaliação contra os critérios da disciplina

### 3.1 As três "Histórias" não são histórias de usuário

| Chave | Título | Tipo declarado | O que realmente é |
|---|---|---|---|
| SCRUM-4 | Design de Mockups | História | Tarefa de design (contém 5 imagens de telas) |
| SCRUM-6 | Desenhar Arquitetura da Aplicação | História | Tarefa técnica de arquitetura |
| SCRUM-7 | Definir Modelo de Dados (Schema) | História | Tarefa técnica de modelagem |

Nenhuma expressa um objetivo de usuário. Este é exatamente o erro que o professor apontou na Aula 2 §5 e prometeu revisar com os grupos:

> *"Um erro comum apontado: confundir subtasks com histórias, o que dificulta derivar critérios de aceite e casos de teste de forma consistente."*

E na ponte para a Aula 3 (§14):

> *"revisar juntos se as histórias realmente são histórias (e não subtasks disfarçadas)"*

**Nenhuma das 27 issues** está escrita no formato *"Como [persona], quero [ação], para [benefício]"*.

> **Contexto importante:** o grupo **escreveu** oito histórias de usuário bem formadas (US01–US08, com critérios de aceite e quatro épicos de negócio) na concepção do produto — elas simplesmente **nunca foram registradas no Jira**. Ver [`user-stories.md`](user-stories.md). O backlog recebeu tarefas técnicas no lugar delas, e os épicos do Jira acabaram organizados por camada ("Frontend React", "Desenvolvimento Backend") em vez de reproduzir os épicos de negócio que já existiam nas histórias. Isso reposiciona o diagnóstico: **não é falta de conhecimento, é falha na passagem da história para a ferramenta.**

### 3.2 Avaliação INVEST (Aula 2 §10)

Aplicando o critério às três issues tipadas como História:

| Critério | Avaliação | Justificativa |
|---|---|---|
| **I** — Independente | 🔴 | "Desenhar Arquitetura" bloqueia praticamente todo o resto |
| **N** — Negociável | 🔴 | Sem descrição, não há o que negociar — SCRUM-6 e SCRUM-7 têm descrição nula |
| **V** — Valiosa | 🔴 | Nenhuma entrega valor perceptível ao usuário final; são pré-requisitos técnicos |
| **E** — Estimável | 🟡 | Estimáveis por serem técnicas, mas sem story points registrados |
| **S** — Pequena | 🔴 | "Desenhar Arquitetura da Aplicação" não cabe em uma sprint com escopo definido |
| **T** — Testável | 🔴 | Sem critérios de aceite, não há como validar conclusão objetivamente |

Pelo critério de *Definition of Ready* da Aula 3 §10, **as três seriam reprovadas**.

### 3.3 Critérios de aceite e BDD

| Item | Situação |
|---|---|
| Issues com critérios de aceite explícitos | **0** |
| Issues em formato BDD/Gherkin (Dado/Quando/Então) | **0** |
| Issues com regras de negócio detalhadas | **0** |
| Issues com cenários de exceção documentados | **0** |

A Aula 3 §11 afirma que critérios objetivos reduzem em até 80% o tempo de escrita de roteiros de teste. Sem eles, cada caso de teste do projeto precisou ser derivado diretamente do código — o que explica por que os 245 testes unitários estão organizados por classe, e não por requisito.

### 3.4 Preenchimento de descrição

| Grupo | Com descrição | Sem descrição |
|---|---|---|
| Epics (4) | 0 | **4** |
| Histórias (3) | 1 (SCRUM-4, só imagens) | 2 |
| Bugs (2) | **2** | 0 |
| Tarefas (18) | poucas | maioria |

**Observação crítica:** as duas descrições mais completas e bem escritas de todo o projeto são as dos **bugs** (SCRUM-33 e SCRUM-34), com diagrama de fluxo, referência a arquivos (`RegisterUserUseCase.java`, `api.ts`), código de status HTTP e mensagens de erro literais.

Isso inverte exatamente a lógica do shift left (Aula 1 §4): **o time documenta melhor o defeito do que o requisito**. Documentar bem no lado direito do processo custa 100; documentar bem no lado esquerdo custaria 1 (regra 1-10-100, Aula 1 §3).

---

## 4. 🔴 Conflito de requisitos — duas listas RF incompatíveis

O achado mais grave da análise. A issue **SCRUM-17** ("Definir requisitos funcionais x não-funcionais", concluída em 28/04/2026) contém uma lista completa de RF01–RF10 e RNF01–RNF09. O arquivo `docs/documentacao.md` contém **outra** lista RF01–RF10 e RNF01–RNF07.

**Os identificadores são os mesmos. Os requisitos são diferentes.**

### Requisitos Funcionais — comparação

| ID | Jira (SCRUM-17) | `docs/documentacao.md` |
|---|---|---|
| RF01 | Cadastro manual de livros | Tela de exploração com curadorias editoriais |
| RF02 | Listar livros | Busca por título, autor ou ISBN |
| RF03 | Atualizar livro | Acervo pessoal com filtros por status |
| RF04 | Remover livro | Progresso de leitura (percentual) |
| RF05 | Status de leitura (quero ler / lendo / finalizado) | Dashboard com estatísticas e metas |
| RF06 | Visualizar detalhes do livro | Registro de empréstimos |
| RF07 | Explorar livros (mock no MVP) | Sinalização de empréstimos atrasados |
| RF08 | **Registrar empréstimo** | **Leitor de PDF integrado** |
| RF09 | Listar empréstimos | Sugestões personalizadas |
| RF10 | Paginação de resultados | Funcionamento em dispositivos móveis |

### Requisitos Não Funcionais — comparação

| ID | Jira (SCRUM-17) | `docs/documentacao.md` |
|---|---|---|
| RNF01 | Arquitetura cliente-servidor (REST) | Interface carrega em menos de 3 segundos |
| RNF02 | Backend em Java + Spring Boot + JPA | Responsivo a partir de 390px |
| RNF03 | Frontend em **React + TypeScript + Vite** | TypeScript com tipagem estrita |
| RNF04 | Banco relacional (H2 dev / MySQL prod) | Componentes reutilizáveis e desacoplados |
| RNF05 | Usabilidade simples e intuitiva | Paleta seguindo tokens do Figma |
| RNF06 | Resposta < 2 segundos em ambiente local | Google Fonts com carregamento otimizado |
| RNF07 | Escalabilidade: futura autenticação, APIs externas e **leitor de livros** | Navegação SPA sem recarregamento |
| RNF08 | Padronização REST (GET/POST/PUT/DELETE) | *(não existe)* |
| RNF09 | Compatibilidade Chrome / Edge / Firefox | *(não existe)* |

### Por que isso é grave

Este é o caso exato de violação do atributo de **consistência** descrito na Aula 2 §9:

> *"Uma história não pode entrar em contradição com regras já definidas em outras histórias."*

Consequências concretas:

1. **A rastreabilidade fica impossível.** Dizer "o teste X cobre o RF08" não significa nada — RF08 é *Registrar empréstimo* no Jira e *Leitor de PDF* na documentação.
2. **Ambas as listas estão desatualizadas.** O RNF03 do Jira exige React + **Vite**; o projeto usa **Next.js**. O RNF07 do Jira trata o leitor de livros como escalabilidade *futura*; hoje o leitor EPUB é funcionalidade central com bugs abertos. O RF08 da documentação fala em **PDF**; o sistema entrega **EPUB**.
3. **Nenhuma das duas descreve o produto atual.** Nem o Jira nem a documentação mencionam autenticação Google, cache de EPUB, restauração de posição de leitura ou sincronização com Gutendex — funcionalidades que existem e estão em produção.

Pela matriz de classificação da Aula 1 §9, isso não é *Discovery Contínuo*: é **falha de análise** — a regra existia e não foi mantida rastreável.

---

## 5. Rastreabilidade — Jira ↔ repositório

| Verificação | Resultado |
|---|---|
| Commits citando chave `SCRUM-xx` | **0 de 229** |
| Branches citando chave `SCRUM-xx` | **0** |
| Pull Requests vinculados a issues | Nenhum identificado |
| Issues referenciando commits, PRs ou branches | Nenhuma |

**Não existe nenhum vínculo entre o Jira e o repositório.** As duas ferramentas foram usadas em paralelo, sem integração — o que, na prática, transforma o Jira num registro paralelo que envelhece sozinho.

A cadeia **Requisito → Caso de Teste → Evidência** (Aula 3 §12) fica quebrada em todos os elos:

| Elo | Estado |
|---|---|
| Requisito | Existe, mas duplicado e contraditório entre Jira e docs |
| Caso de Teste | 245 unitários + 5 integração + 29 E2E, nenhum referenciando requisito |
| Evidência | Relatórios JaCoCo, surefire, k6 e SonarCloud gerados, sem vínculo a requisito |
| **Vínculo** | 🔴 **Inexistente em todos os pontos** |

Sem esse vínculo, o **risco remanescente** (Aula 4 §10) não é calculável — não há como afirmar se os 23 testes E2E que falharam representam risco alto ou irrelevante.

---

## 6. Distribuição de trabalho

| Responsável | Issues atribuídas |
|---|---|
| Carla Talita Alves dos Santos | **17** |
| Ronaldo Luiz | 2 |
| David de Oliveira | 1 |
| Gabriel Renato | 1 |
| *(sem responsável)* | 6 |

**Concentração acentuada:** uma única pessoa consta como responsável por 17 das 21 issues atribuídas (81%), incluindo todo o épico de backend e ambos os bugs.

Duas leituras possíveis, e vale esclarecer qual é a real antes da sessão:

- **Se reflete a realidade:** é um risco de *bus factor* e de sobrecarga que a Aula 2 §2 trataria no planejamento de papéis da squad.
- **Se é artefato do registro** (uma pessoa criava e assumia as issues em nome do time): é mais um sintoma de que o Jira deixou de refletir o trabalho real — o que reforça o diagnóstico de abandono.

Observação adicional: as chaves **SCRUM-5 e SCRUM-9 a SCRUM-16 não existem** — foram criadas e excluídas. A numeração salta de SCRUM-8 para SCRUM-17.

---

## 7. O que o Jira faz bem

Para equilibrar o diagnóstico — e porque isso importa na conversa com o professor:

| Ponto positivo | Evidência |
|---|---|
| Hierarquia existe e é usada | 22 das 27 issues têm parent vinculado a um épico |
| Tipos de issue configurados além do padrão | Epic, Feature, História, Tarefa, Bug, Request, Subtask |
| Sprints reais foram executadas | Sprint 3 fechada dentro do ciclo (25/04 a 12/05) |
| Bugs bem documentados | SCRUM-33 e SCRUM-34 com causa, solução e fluxo detalhado |
| Requisitos foram formalmente definidos | SCRUM-17 é uma issue de definição de RF/RNF — o grupo fez o exercício |
| Decisão de arquitetura registrada | SCRUM-32 documenta a migração MVC → Clean Architecture |

O grupo **sabe usar a ferramenta**. O que faltou foi manutenção e a decisão explícita sobre continuar ou migrar.

---

## 8. Riscos identificados

| # | Risco | Pilar violado | Severidade |
|---|---|---|---|
| J1 | Duas listas RF/RNF conflitantes com IDs idênticos | Aula 2 §9 (consistência) | 🔴 Alta |
| J2 | Ambas as listas de requisitos desatualizadas em relação ao produto real | Aula 4 §7 (estratégia viva) | 🔴 Alta |
| J3 | Nenhum vínculo entre Jira e repositório (0/229 commits) | Aulas 2 §6, 3 §12 | 🔴 Alta |
| J4 | Zero critérios de aceite e zero BDD em 27 issues | Aulas 2 §11, 3 §11 | 🔴 Alta |
| J4b | **As 8 user stories existentes nunca foram registradas na ferramenta** — o backlog recebeu tarefas no lugar de requisitos | Aula 3 §6 | 🔴 Alta |
| J5 | Histórias que são tarefas técnicas disfarçadas | Aula 2 §5 | 🟡 Média |
| J6 | Épicos organizados por camada técnica, ignorando os 4 épicos de negócio já definidos nas user stories | Aula 3 §6 | 🟡 Média |
| J7 | Sprint 4 aberta há 77 dias além do prazo; sprint goals vazios | Aula 2 §4 | 🟡 Média |
| J8 | Épicos sem descrição e nunca encerrados | Aula 3 §6 | 🟡 Média |
| J9 | Trabalho de jun–ago (leitor EPUB, Google Auth, testes) sem registro algum | Aula 1 §11 | 🟡 Média |
| J10 | Bugs mais bem documentados que requisitos (inversão do shift left) | Aula 1 §4 | 🟡 Média |
| J11 | Concentração de 81% das atribuições em uma pessoa | Aula 2 §2 | 🟢 A esclarecer |

---

## 9. Recomendações

### Decisão a tomar: retomar o Jira ou migrar para GitHub Projects

| Critério | Retomar o Jira | Migrar para GitHub Projects |
|---|---|---|
| Estrutura de teste nativa | ✅ Test Plan → Test Suite → Test Case (a hierarquia da Aula 3 §7) | ❌ Precisa ser improvisada com labels/issues |
| Vínculo com commits e PRs | 🟡 Exige configurar a integração GitHub ↔ Jira | ✅ Nativo — issue, PR, commit e branch já se conectam |
| Histórico já existente | ✅ 27 issues, 4 sprints, decisões registradas | ❌ Recomeça do zero (migração manual) |
| Atrito para o time | 🔴 Foi abandonado uma vez — há razão para isso | ✅ O time já vive no GitHub diariamente |
| Aderência ao que o professor discute em aula | ✅ Jira é uma das duas ferramentas que ele cita nominalmente | 🟡 Menos convencional no contexto corporativo |

**Recomendação:** levar a decisão para a sessão com o professor, apresentando os dois lados. A inclinação técnica é pelo **GitHub Projects** (o time já está lá, e o vínculo issue ↔ PR ↔ commit resolve sozinho o risco J3, que é o mais grave depois do conflito de requisitos). Mas o Jira oferece a estrutura de Test Plan que a disciplina usa como referência — e o professor pode ter preferência didática.

**O que não é aceitável é o estado atual:** duas ferramentas de registro (Jira + docs markdown) desatualizadas e contraditórias, com o trabalho real acontecendo em nenhuma das duas.

### Ações imediatas — independentes da ferramenta escolhida

| # | Ação | Efeito | Esforço |
|---|---|---|---|
| 0 | **Registrar as 8 user stories na ferramenta** (ver [`user-stories.md`](user-stories.md)), substituindo os épicos técnicos pelos 4 épicos de negócio que elas já definem | Corrige a origem do problema: o backlog passa a refletir requisito, não tarefa | 2h |
| 1 | **Consolidar uma única lista de requisitos** — eleger as user stories como fonte, descontinuar as duas listas RF e cobrir o produto atual (EPUB, Google Auth, cache) | Elimina J1 e J2 — pré-requisito de qualquer rastreabilidade | 2h |
| 2 | Encerrar a Sprint 4 e fechar os 4 épicos concluídos | Torna o board honesto sobre o estado real | 15 min |
| 3 | Reescrever as 3 "Histórias" como histórias reais ou reclassificá-las como Tarefa | Elimina J5 | 30 min |
| 4 | Escrever descrição e objetivo para os épicos, reorganizando-os por valor de negócio | Elimina J6 e J8; viabiliza cobertura por objetivo | 1h |
| 5 | Adotar a convenção de citar a chave da issue nos commits e branches | Elimina J3 daqui para frente | 5 min (acordo de time) |
| 6 | Registrar as funcionalidades de jun–ago que nunca entraram no backlog | Elimina J9; recupera o histórico | 1h |
| 7 | Adicionar critérios de aceite em BDD às issues ativas | Elimina J4; viabiliza derivar casos de teste | 3h |

O item **1 é bloqueante** para os demais: enquanto houver dois "RF08" significando coisas diferentes, nenhuma matriz de rastreabilidade pode ser construída.

---

## 10. Como isso muda a resposta ao professor

A pergunta 1 do item 15 da Aula 4 é: *"em qual ferramenta cada grupo está trabalhando, e se pretendem continuar nela"*.

**Resposta anterior (incorreta):** "nenhuma ferramenta evidenciada".

**Resposta correta:**

> O grupo adotou Jira (`alexandria-puc.atlassian.net`, projeto Alexandria, team-managed) e o utilizou de forma real entre 02/04 e meados de maio de 2026 — 27 issues, 4 épicos, 4 sprints, com a Sprint 3 executada e encerrada dentro do ciclo. O uso foi descontinuado a partir de 12/05, quando a Sprint 4 foi aberta e nunca encerrada; a última movimentação de qualquer issue foi em 18/06. Desde então o desenvolvimento continuou no repositório (leitor EPUB, autenticação Google, testes de carga, análise Sonar) sem registro no backlog. A decisão sobre retomar o Jira ou migrar para GitHub Projects ainda está aberta, e é um dos pontos que o grupo quer discutir.

Essa é uma resposta muito melhor do que "não temos ferramenta" — mostra que o grupo passou pela experiência, tem material real para revisar, e chega à sessão com um diagnóstico feito e uma decisão a tomar. É exatamente o insumo que o professor pediu para trabalhar os temas da disciplina com exemplos reais.

---

## Anexo — Consultas utilizadas

```
Site:      https://alexandria-puc.atlassian.net
cloudId:   882bc9e8-6a09-4140-91d8-5555ff1d8279
Projeto:   SCRUM (Alexandria) — team-managed, board 1

JQL empregadas:
  project = SCRUM ORDER BY created ASC
  project = SCRUM AND issuetype in (História, Epic, Bug) ORDER BY key ASC
  project = SCRUM AND sprint is not EMPTY
  key in (SCRUM-4, SCRUM-6, SCRUM-7, SCRUM-17, SCRUM-31, SCRUM-32)

Verificação de rastreabilidade no repositório:
  git log --all --oneline | Select-String 'SCRUM-\d+'   → 0 de 229 commits
  git branch -a          | Select-String 'SCRUM-\d+'    → 0 branches
```

---

## Documentos relacionados

- [`user-stories.md`](user-stories.md) — As 8 histórias de usuário que nunca chegaram ao Jira
- [`alinhamento-projeto-integrador.md`](alinhamento-projeto-integrador.md) — Análise do projeto contra as Aulas 1 a 4
- [`analise-sonarcloud.md`](analise-sonarcloud.md) — Análise da qualidade de código no SonarCloud
- [`relatorio-testes-parcial.md`](relatorio-testes-parcial.md) — Relatório parcial de testes
- [`documentacao.md`](documentacao.md) — Documentação funcional (contém a lista RF/RNF conflitante)

---

*Documento gerado em 08/08/2026 a partir da leitura direta do Jira via API Atlassian.*
