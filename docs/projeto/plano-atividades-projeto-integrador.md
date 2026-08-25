# Plano de Atividades — Projeto Integrador

**Projeto:** Alexandria — Biblioteca Digital
**Instituição:** PUC-SP — MBA em Engenharia de Software
**Disciplina:** Testes de Software — Prof. Jehú Lara Ramos
**Data:** 09/08/2026
**Base:** [`alinhamento-projeto-integrador.md`](alinhamento-projeto-integrador.md), [`user-stories.md`](user-stories.md), [`analise-jira.md`](analise-jira.md), [`analise-sonarcloud.md`](analise-sonarcloud.md)

---

## Escopo

Três atividades foram atribuídas ao grupo no Projeto Integrador:

1. **Cadastrar/Atualizar as histórias de usuário, ou requisitos, na ferramenta de controle de projeto de Desenvolvimento**
2. **Mapear/Documentar as personas na ferramenta do projeto**
3. **Instalar o SonarQube e executar as varreduras de qualidade de código, extraindo o relatório inicial com métricas de duplicidade, complexidade, vulnerabilidades etc.**

Este documento analisa cada uma contra o estado real do repositório, decompõe em tarefas executáveis e propõe uma ordem de execução.

---

## Sumário Executivo

As três atividades **não são independentes**, e o enunciado omite um pré-requisito bloqueante:

```
[R0] Consolidar fonte única de requisitos  ← não está no enunciado
        │
        └──> Atividade 1 (cadastrar histórias na ferramenta)
                    │
                    └──> Atividade 2 (personas) — parcialmente paralela

Atividade 3 (SonarQube) ──────> independente, pode começar hoje
```

Dois achados condicionam todo o planejamento:

> **A Atividade 1 está bloqueada pelo risco R0.** Existem três definições de requisito com identificadores colidentes — RF08 é *"Registrar empréstimo"* no Jira, *"Leitor de PDF"* em `documentacao.md`, e não existe nas user stories. Cadastrar histórias na ferramenta sem consolidar isso antes cria uma **quarta** fonte divergente.

> **A Atividade 3 não é de instalação.** O Sonar já está instalado (SonarCloud, organização `mba-puc-alexandria`) — mas mede o projeto errado: o backend Java inteiro está fora da varredura e a cobertura não é reportada. A atividade real é **corrigir a configuração antes de extrair o relatório**.

**Esforço total estimado: ~19 horas.**

---

## Atividade 1 — Cadastrar/Atualizar histórias de usuário na ferramenta

### 1.1 Estado atual

| Item | Situação |
|---|---|
| Histórias de usuário escritas | ✅ **8** (US01–US08), formato "Como/Quero/Para", com critérios de aceite e 4 épicos **de negócio** |
| Onde vivem | 🔴 Apenas em [`user-stories.md`](user-stories.md) — **nunca registradas em ferramenta** |
| Ferramenta de gestão | 🟡 Jira (`alexandria-puc.atlassian.net`), 27 issues, 4 sprints — **abandonado desde 18/06/2026** |
| Issues em formato de história | 🔴 **0 de 27** |
| Issues com critérios de aceite | 🔴 **0 de 27** |
| Sprint 4 | 🔴 Ainda "ativa", 77 dias além do prazo previsto (23/05/2026) |
| Vínculo commits ↔ issues | 🔴 **0 de 229 commits** citam uma chave `SCRUM-xx` |

O diagnóstico é de **falha de registro, não de conhecimento**: as 8 histórias são estruturalmente superiores ao que está na ferramenta. Elas guiaram os mockups do Figma (issue `SCRUM-4`), que definiram o layout do sistema — influenciaram o produto construído, mas nunca viraram issue.

### 1.2 Decomposição em tarefas

| # | Tarefa | Dependência | Esforço |
|---|---|---|---|
| 1.0 | **Decidir a ferramenta:** retomar o Jira × migrar para GitHub Projects | — | 30 min *(decisão de grupo)* |
| 1.1 | **Eleger as user stories como fonte única de requisito** e descontinuar as duas listas RF01–RF10 | 🔴 **Bloqueante** | 1h |
| 1.2 | Escrever as ~6 histórias faltantes: leitor EPUB, progresso de leitura, dashboard, autenticação Google, configurações de perfil, sync Gutendex | 1.1 | 3h |
| 1.3 | Corrigir US01 — foi implementada com senha (JWT + BCrypt) e OAuth Google, não no "modo demo sem senha" especificado | 1.1 | 15 min |
| 1.4 | Substituir critérios subjetivos por métricas objetivas | — | 1h |
| 1.5 | Adicionar caminhos de exceção às histórias | — | 1h30 |
| 1.6 | Converter os critérios de aceite para BDD/Gherkin | 1.4, 1.5 | 2h |
| 1.7 | Cadastrar tudo na ferramenta, substituindo os épicos técnicos pelos 4 épicos de negócio | 1.0–1.6 | 2h |
| 1.8 | Encerrar a Sprint 4, fechar os 4 épicos concluídos e reclassificar as 3 falsas "Histórias" como Tarefa | 1.0 | 45 min |
| 1.9 | Adotar a convenção de citar a chave da issue em commits e branches | — | 5 min |

**Subtotal: ~12h**

### 1.3 Detalhamento das correções de qualidade (tarefas 1.4 e 1.5)

**Critérios subjetivos a substituir** — a Aula 3 §11 cita nominalmente os termos a evitar:

| US | Critério problemático | Por quê |
|---|---|---|
| US05 | "Feedback visual de sucesso" | Não define o quê, onde, nem por quanto tempo |
| US06 | "**Ações claras** disponíveis" | "Claras" é subjetivo |
| US07 | "Modal **ou** tela de empréstimo" | Ambíguo — permite duas implementações |
| US02 | "Existe estado vazio" | Não define o que deve comunicar |

**Caminhos de exceção ausentes** (Aula 2 §11):

- **US05** — e se o livro já estiver na biblioteca do usuário?
- **US07** — e se o livro já estiver emprestado? Pode emprestar livro com status "Lendo"?
- **US08** — o que acontece quando o prazo de 7 dias vence? Existe estado "atrasado"?
- **US03** — mudar de "Lido" para "Quero ler" é permitido? Reseta o progresso?
- **US01** — o que acontece se o email não existir?

### 1.4 Decisão forçada pela atividade

US07 e US08 (épico de Empréstimo) **nunca foram implementadas** — não existe domínio de empréstimo no backend. Mas a tela `/emprestimos` **está em produção**, sem persistência por trás.

Ao cadastrar as histórias, o grupo é obrigado a decidir: **implementar o épico ou remover a tela**. Não é possível cadastrar a história e deixar o estado ambíguo. Pela Aula 4 §10, uma tela em produção sem backend é risco não mapeado.

### 1.5 Recomendação sobre a ferramenta (tarefa 1.0)

| Critério | Retomar o Jira | Migrar para GitHub Projects |
|---|---|---|
| Estrutura de teste nativa | ✅ Test Plan → Test Suite → Test Case | ❌ Improvisada com labels |
| Vínculo commit ↔ PR ↔ issue | 🟡 Exige integração GitHub ↔ Jira | ✅ Nativo |
| Histórico existente | ✅ 27 issues, 4 sprints, decisões | ❌ Recomeça do zero |
| Atrito para o time | 🔴 Já foi abandonado uma vez | ✅ O time já vive no GitHub |
| Aderência à disciplina | ✅ Citado nominalmente pelo professor | 🟡 Menos convencional |

**Inclinação técnica: GitHub Projects** — resolve sozinho o risco J3/R3 (rastreabilidade zero entre ferramenta e repositório), que é o mais grave depois do conflito de requisitos. Mas a decisão vale ser levada ao professor, que pode ter preferência didática pelo Jira.

---

## Atividade 2 — Mapear/Documentar as personas

### 2.1 Estado atual

É a atividade **mais barata** e a de **resposta mais direta** ao professor: hoje a resposta à pergunta 3 do item 15 da Aula 4 é simplesmente **"não"**.

| Item | Situação |
|---|---|
| Personas nas histórias | 🔴 Persona única e genérica — **"Como usuário"** nas 8 histórias |
| Perfis identificados | 🟡 4 perfis em `docs/documentacao.md:67` ("Público-Alvo"), nunca convertidos em personas |
| Jornadas | 🔴 Inexistentes |
| Registro em ferramenta | 🔴 Inexistente |

### 2.2 Decomposição em tarefas

| # | Tarefa | Dependência | Esforço |
|---|---|---|---|
| 2.1 | Converter os 4 perfis em personas de fato: nome, contexto, objetivo, frustração, comportamento no produto | — | 1h30 |
| 2.2 | Substituir "Como usuário" pela persona correta nas 8 histórias existentes | 2.1 | 30 min |
| 2.3 | Atribuir persona também às ~6 histórias novas | 1.2, 2.1 | 30 min |
| 2.4 | Desenhar 2 jornadas ponta a ponta | 2.1–2.3 | 2h |
| 2.5 | Registrar as personas na ferramenta escolhida | 1.0, 2.1 | 30 min |

**Subtotal: ~5h**

### 2.3 Mapeamento base já existente

| Persona sugerida | Histórias que a caracterizam |
|---|---|
| **Leitor frequente** — acervo grande, organiza leituras | US02, US03, US05 |
| **Estudante / pesquisador** — controla leituras acadêmicas | US04, US06 |
| **Membro de clube de leitura** — empresta e toma emprestado | US07, US08 |
| **Colecionador** — cataloga edições e raridades | US06 |

### 2.4 Lacuna crítica

⚠️ **Nenhuma das 4 personas descreve quem usa o leitor EPUB** — que é hoje a funcionalidade central do produto, concentra os bugs conhecidos e tem o único CRITICAL do SonarCloud.

A jornada de leitura precisa de persona própria, e ela só pode nascer junto com as histórias criadas na tarefa 1.2. Por isso a tarefa 2.4 (jornadas) é posicionada depois da Atividade 1.

**Jornadas sugeridas para a tarefa 2.4:**
1. *Descobrir → adicionar → ler* (cobre US04, US05, US02, US03 + leitor EPUB e progresso)
2. *Emprestar → acompanhar → devolver* (cobre US07, US08 — condicionada à decisão da seção 1.4)

---

## Atividade 3 — SonarQube e relatório inicial

### 3.1 O enunciado diverge da realidade

O Sonar **já está instalado e analisando** — SonarCloud, organização `mba-puc-alexandria`, projeto `mba-puc-alexandria_alexandria`. A atividade real não é instalar, é **fazer a instalação existente medir o projeto certo**.

### 3.2 Por que o relatório extraído hoje seria enganoso

| Métrica pedida no enunciado | Valor atual | Vale? |
|---|---|---|
| **Duplicidade** | 0,9% | ⚠️ Apenas frontend |
| **Complexidade** | Ciclomática 499 · cognitiva 258 | ⚠️ Apenas frontend |
| **Vulnerabilidades** | 16 | ⚠️ 14 estão no pipeline CI/CD, não no código da aplicação |
| **Cobertura** | **não reportada** | ❌ Os 85% do JaCoCo não chegam ao Sonar |
| Bugs | 3 | ⚠️ Apenas frontend + 1 migration SQL |
| Code Smells | 52 (1 CRITICAL) | ⚠️ Apenas frontend |
| Quality Gate | 🔴 **ERROR** (`new_security_rating` = C) | — |
| Última análise | 31/07/2026 | ⚠️ 9 dias de defasagem |

A distribuição de linguagens retornada pela API confirma o problema:

```
ts=3317   yaml=330   xml=177   js=77   plsql=64   css=58   docker=30   <null>=30
```

**Não há `java`.** Todo o `alexandria-backend/` está invisível — os 4 domínios (Book, Author, User, UserBooks), os use cases, os controllers REST, a configuração de segurança JWT e os **245 testes unitários**.

O principal ponto forte técnico do projeto — arquitetura hexagonal com 85% de cobertura — **não aparece em nenhum indicador**.

### 3.3 Causa raiz

Três configurações de `projectKey` conflitantes, **nenhuma apontando para o projeto que existe**:

| Onde | `projectKey` configurado | Existe? |
|---|---|---|
| `alexandria-backend/pom.xml:18` | `mba-puc-alexandria_alexandria-backend` | ❌ Não existe |
| `.github/workflows/sonar-scan-alexandria.yaml` | `mba-puc-alexandria` | ❌ É a chave da **organização** |
| `.github/workflows/sonarqube-scan.yml` | `mba-puc-alexandria` | ❌ Idem |
| **Projeto real** | `mba-puc-alexandria_alexandria` | ✅ Único existente |

Somado a isso, o `sonarqube-scan.yml` usa a scan-action genérica, que **não compila Java** — sem `sonar.java.binaries`, o scanner ignora os arquivos `.java`. É a explicação direta para `java=0`.

### 3.4 Decomposição em tarefas

| # | Tarefa | Efeito | Esforço |
|---|---|---|---|
| 3.0 | **Decidir: SonarQube self-hosted × manter SonarCloud** — o enunciado diz "instalar o SonarQube"; confirmar com o professor se o SonarCloud já em uso atende | — | *decisão* |
| 3.1 | Unificar em **um único** workflow com `-Dsonar.projectKey=mba-puc-alexandria_alexandria` e remover o duplicado | Para a corrida entre workflows e grava no projeto correto | 15 min |
| 3.2 | Analisar o backend via `mvn verify sonar:sonar` com `-Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml` | 🔴 **Traz o Java e os 85% de cobertura** | 30 min |
| 3.3 | Corrigir `sonar.projectKey` em `alexandria-backend/pom.xml:18` | Elimina a 3ª configuração conflitante | 2 min |
| 3.4 | Remover o passo `npm test -- --coverage` do job de frontend (script inexistente no `package.json`) | Destrava o job que falha antes do scan | 5 min |
| 3.5 | Executar a análise também em pull requests e na branch `develop` | Gate de qualidade no fluxo real | 10 min |
| 3.6 | **Reexecutar a varredura e extrair o relatório inicial** | 🎯 **Entregável da atividade** | 30 min |
| 3.7 | *(alto retorno)* Corrigir as 6 ocorrências de `S7636` em `deploy-aws.yml` — passar secrets via `env:` em vez de interpolar em `run:` | **Deve restaurar o Quality Gate para verde** — é a única condição em falha | 30 min |

**Subtotal: ~2h** (3h incluindo a tarefa 3.7)

### 3.5 Por que 3.6 depende de 3.1–3.5

Extrair o relatório antes de corrigir a configuração produz um documento afirmando *"duplicação 0,9%, complexidade ciclomática 499"* sem qualificar que isso mede **3.317 linhas de TypeScript** e ignora 100% do backend.

O professor pediu esse relatório na Aula 1 §11 como **"fotografia do código no início e outra no fim do semestre"**. Uma foto do enquadramento errado inutiliza a comparação futura — e é justamente o que a Aula 3 §13 descreve como *cobertura que parece existir mas não atende ao objetivo*.

---

## Ordem de execução recomendada

| Ordem | Atividade | Esforço | Justificativa |
|---|---|---|---|
| 1º | **3.1 → 3.6** (Sonar) | ~2h | Independente das demais, gera entregável concreto e destrava a discussão de complexidade prevista para a Aula 5 |
| 2º | **1.0 + 1.1** (ferramenta + fonte única) | ~1h30 | Decisões de grupo — precisam acontecer antes de qualquer cadastro |
| 3º | **2.1 + 2.2** (personas nas 8 histórias) | ~2h | Responde sozinho uma das quatro perguntas do professor |
| 4º | **1.2 → 1.9** (histórias novas + BDD + cadastro) | ~10h | O grosso do esforço, já com persona definida |
| 5º | **2.3 → 2.5** (personas novas + jornadas + registro) | ~3h | Depende das histórias do leitor EPUB existirem |
| — | **3.7** (Quality Gate verde) | 30 min | Pode ser feito a qualquer momento em paralelo |

**Total: ~19h**

---

## Riscos do plano

| # | Risco | Mitigação |
|---|---|---|
| P1 | Cadastrar as histórias sem executar a tarefa 1.1 cria uma **quarta** fonte de requisitos divergente | Tratar 1.1 como bloqueante absoluto de 1.7 |
| P2 | Extrair o relatório Sonar antes de 3.1–3.5 produz fotografia inicial inválida para comparação de fim de semestre | Tratar 3.6 como dependente de 3.1–3.5 |
| P3 | A decisão sobre a ferramenta (1.0) travar o grupo e paralisar as demais tarefas | As tarefas 1.1–1.6 e 2.1–2.4 independem da ferramenta — só o cadastro (1.7, 2.5) depende |
| P4 | O épico de Empréstimo permanecer indefinido, mantendo a tela fantasma em produção | Forçar a decisão da seção 1.4 antes da tarefa 1.7 |
| P5 | O professor exigir SonarQube self-hosted, invalidando o trabalho sobre o SonarCloud | Confirmar na sessão (tarefa 3.0); as correções de `projectKey` e do build Maven aproveitam nos dois cenários |

---

## Observação final

O item que mais gera valor por hora investida é a **tarefa 1.1 — eleger a fonte única de requisitos**. Custa 1 hora e desbloqueia matriz de rastreabilidade, cobertura por objetivo de negócio e análise de risco remanescente.

**Ele não aparece no enunciado das três atividades.** Mas sem ele, a Atividade 1 apenas adiciona uma quarta fonte divergente ao problema que a disciplina se propõe a corrigir.

---

## Documentos relacionados

- [`alinhamento-projeto-integrador.md`](alinhamento-projeto-integrador.md) — Análise do projeto contra as Aulas 1 a 4
- [`user-stories.md`](user-stories.md) — As 8 histórias, avaliação INVEST e status de implementação
- [`analise-jira.md`](analise-jira.md) — Análise detalhada do backlog no Jira
- [`analise-sonarcloud.md`](analise-sonarcloud.md) — Análise detalhada do SonarCloud
- [`documentacao.md`](documentacao.md) — Documentação funcional (contém a lista RF/RNF conflitante)

---

*Documento elaborado em 09/08/2026 a partir da análise das três atividades do Projeto Integrador contra o estado real do repositório Alexandria.*
