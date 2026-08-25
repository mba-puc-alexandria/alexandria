# Análise SonarCloud — Alexandria

**Projeto:** Alexandria — Biblioteca Digital
**Instituição:** PUC-SP — MBA em Engenharia de Software
**Data da análise:** 07/08/2026
**Fonte:** API pública do SonarCloud (`https://sonarcloud.io/api`)

---

## Sumário Executivo

O SonarCloud está instalado e analisando o repositório, mas **não está medindo o que importa**: o backend Java inteiro está fora da análise e nenhuma métrica de cobertura chega à plataforma. O Quality Gate está vermelho há uma semana e não bloqueia o deploy automático para produção.

| Indicador | Situação |
|---|---|
| Quality Gate | 🔴 **ERROR** |
| Backend Java analisado | ❌ **Não** |
| Cobertura reportada | ❌ **Não** |
| Análise em PR / `develop` | ❌ **Não** (só `main`) |
| Última análise | **31/07/2026** — 7 dias de defasagem |

---

## 1. Identificação do projeto

| Item | Valor |
|---|---|
| Organização | `mba-puc-alexandria` |
| Chave do projeto | `mba-puc-alexandria_alexandria` |
| ID interno | `AZ-5SnJGz-iNQVh3wZ8Q` |
| Branch analisada | `main` (única) |
| Último commit analisado | `d97d64d` — mensagem "sonar" |
| Data da última análise | 31/07/2026 20:17 UTC |

> **Observação:** existe **apenas um** projeto na organização. Nenhuma das chaves configuradas no repositório aponta para ele (ver seção 4).

---

## 2. Métricas gerais

| Métrica | Valor | Rating |
|---|---|---|
| Bugs | **3** | Reliability **C** (3.0) |
| Vulnerabilidades | **16** | Security **C** (3.0) |
| Code Smells | **52** | Maintainability **A** (1.0) |
| Security Hotspots | 0 | ✅ Melhor valor |
| Densidade de duplicação | 0,9% | ✅ |
| Débito técnico (`sqale_index`) | 197 min (~3h17) | — |
| Linhas de código (`ncloc`) | 4.083 | — |
| Complexidade ciclomática | 499 | — |
| Complexidade cognitiva | 258 | — |
| **Cobertura de testes** | **não reportada** | ❌ |

### Condições do Quality Gate

| Condição | Limite | Valor atual | Status |
|---|---|---|---|
| `new_security_rating` | ≤ 1 (A) | **3 (C)** | 🔴 **ERROR** |
| `new_reliability_rating` | ≤ 1 (A) | 1 (A) | ✅ OK |
| `new_maintainability_rating` | ≤ 1 (A) | 1 (A) | ✅ OK |
| `new_duplicated_lines_density` | ≤ 3% | 0,0% | ✅ OK |
| `new_security_hotspots_reviewed` | ≥ 100% | 100% | ✅ OK |

**O Quality Gate falha por uma única condição:** o rating de segurança do código novo. Todas as demais passam.

---

## 3. Problema estrutural — o backend Java não é analisado

A distribuição de linguagens retornada pela API (`ncloc_language_distribution`) é:

```
ts=3317   yaml=330   xml=177   js=77   plsql=64   css=58   docker=30   <null>=30
```

**Não há `java` na lista.** Os 4.083 ncloc são quase inteiramente o frontend TypeScript, somados aos arquivos de workflow, Dockerfiles e migrations SQL.

Consequências diretas:

- Todo o `alexandria-backend/` está invisível — os 4 domínios (Book, Author, User, UserBooks), os use cases, os controllers REST, a configuração de segurança JWT e os **245 testes** não são medidos.
- **Nenhuma métrica de cobertura** chega ao Sonar, apesar do JaCoCo estar configurado e reportar 85% de instruções / 86% de linhas / 74% de branches.
- As métricas de complexidade (499 ciclomática, 258 cognitiva) referem-se **apenas ao frontend**.

Em outras palavras: o principal ponto forte técnico do projeto — a arquitetura hexagonal com alta cobertura — não aparece em nenhum indicador do SonarCloud.

---

## 4. Causa raiz — três configurações conflitantes

Existem três configurações de Sonar no repositório, apontando para chaves diferentes, e **nenhuma delas corresponde ao projeto real**.

| Onde | `projectKey` configurado | Existe? |
|---|---|---|
| `alexandria-backend/pom.xml:18` | `mba-puc-alexandria_alexandria-backend` | ❌ API retorna *"Component key not found"* |
| `.github/workflows/sonar-scan-alexandria.yaml` (branch `main`) | `mba-puc-alexandria` | ❌ É a chave da **organização**, não de projeto |
| `.github/workflows/sonarqube-scan.yml` (branch `main`) | `mba-puc-alexandria` | ❌ Idem |
| **Projeto real** | `mba-puc-alexandria_alexandria` | ✅ Único existente |

### Problemas adicionais identificados

1. **Dois workflows de Sonar disparam no mesmo evento.** Tanto `sonar-scan-alexandria.yaml` quanto `sonarqube-scan.yml` reagem a `push` em `main` e a todo `pull_request`, gravando na mesma chave — competem entre si.

2. **A scan-action genérica não compila Java.** O `sonarqube-scan.yml` usa `SonarSource/sonarqube-scan-action@v4` com `-Dsonar.sources=alexandria-backend/src`. Essa action não executa build Maven e, sem `sonar.java.binaries` (bytecode compilado), o scanner ignora os arquivos `.java`. **Esta é a explicação direta para `java=0`.**

3. **O job de frontend quebra antes do scan.** O `sonar-scan-alexandria.yaml` executa `npm test -- --coverage` no diretório `alexandria-frontend`, mas o `package.json` não possui script `test` — apenas `test:e2e` e `test:e2e:ui`. O job falha antes de chegar ao passo de análise, e a cobertura de frontend também nunca é enviada.

4. **As branches divergiram.** Os workflows de Sonar existem apenas na `main`. A branch de desenvolvimento atual possui `ci.yaml`, que não menciona Sonar em nenhum ponto.

**Conclusão:** o resultado presente no SonarCloud provavelmente vem da *Automatic Analysis* da própria plataforma (varredura direta do repositório, que não compila Java), e não dos workflows de CI — que estão mal configurados e/ou falhando.

---

## 5. Bugs (3)

| # | Arquivo | Linha | Regra | Descrição | Severidade |
|---|---|---|---|---|---|
| 1 | `alexandria-backend/src/main/resources/db/migration/V002__Create_Authors_And_BookAuthors.sql` | 18 | `plsql:NullComparison` | Comparação direta com `NULL` em vez de `IS NULL` / `IS NOT NULL` | **MAJOR** |
| 2 | `alexandria-frontend/src/components/LoginModal.tsx` | 67 | `typescript:S1082` | Elemento visível não-interativo com `onClick` sem listener de teclado (acessibilidade) | MINOR |
| 3 | `alexandria-frontend/src/components/LoginModal.tsx` | 75 | `typescript:S1082` | Idem | MINOR |

> **Maior risco funcional:** o bug #1. Em SQL, uma comparação `= NULL` sempre resulta em desconhecido, portanto a condição **nunca é satisfeita**. Vale verificar se essa migration produziu o efeito esperado no banco de dados.

---

## 6. Vulnerabilidades (16)

Distribuição por regra — **14 das 16 estão no próprio pipeline de CI/CD**, não no código da aplicação:

| Regra | Qtd | Localização | Descrição |
|---|---|---|---|
| `githubactions:S7636` — *Avoid expanding secrets in a run block* | **6** | `deploy-aws.yml:36,38,42,42,43,43` | Secrets interpolados dentro de blocos `run:` — expostos em log e sujeitos a injeção de comando |
| `githubactions:S7637` — *Use full commit SHA hash* | **6** | `deploy-aws.yml:46,59,101,130` · `sonar-scan-alexandria.yaml:63` · `sonarqube-scan.yml:19` | Actions referenciadas por tag mutável (`@v4`) em vez de SHA fixo — risco de supply chain |
| `githubactions:S7631` — *untrusted code from a fork* | 1 | `deploy-aws.yml:18` | O trigger `workflow_run` faz checkout de SHA que pode ter origem em fork |
| `docker:S6505` — *Omitting `--ignore-scripts`* | 2 | `alexandria-frontend/Dockerfile:4` · `sonar-scan-alexandria.yaml:56` | `npm ci` executa lifecycle scripts das dependências |
| `docker:S6471` — *runs as root* | 1 | `alexandria-backend/Dockerfile:7` | Imagem `eclipse-temurin` roda como `root` por padrão |

### Observações

- **11 dos 16 achados concentram-se em `deploy-aws.yml`** — o mesmo arquivo responsável pelo deploy automático em produção.
- Nenhuma vulnerabilidade foi apontada na lógica de negócio da aplicação.
- Corrigir os 6 achados de `S7636` deve, isoladamente, elevar o `new_security_rating` de C para A e **restaurar o Quality Gate para verde**.

---

## 7. Code Smells (52)

**Por severidade:** 1 CRITICAL · 40 MAJOR · 11 MINOR

| Qtd | Regra | Descrição |
|---|---|---|
| **26** | `typescript:S9011` | Faltando atributo `type` explícito em `<button>` — metade de todos os smells |
| 10 | `typescript:S6759` | Props de componente não marcadas como `readonly` |
| 5 | `typescript:S6671` | Rejeição de `Promise` com valor que não é `Error` |
| 2 | `typescript:S6848` | Elemento interativo não-nativo (acessibilidade) |
| 2 | `typescript:S6853` | `<label>` sem controle associado (acessibilidade) |
| 1 | `typescript:S2004` | **CRITICAL** — funções aninhadas em mais de 5 níveis |
| 1 | `typescript:S6481` | Valor do `Context.Provider` recriado a cada render |
| 1 | `typescript:S6479` | Uso de índice de array como `key` |
| 1 | `typescript:S7721` | Função `handleSearch` deveria ser movida para escopo externo |
| 1 | `typescript:S3358` | Ternário aninhado deveria ser extraído |
| 1 | `typescript:S6772` | Espaçamento ambíguo antes de elemento `span` |
| 1 | `typescript:S7765` | Usar `.includes()` em vez de `.some()` para verificação de existência |

### O achado CRITICAL

| Campo | Valor |
|---|---|
| Arquivo | `alexandria-frontend/src/app/(main)/leitor/[id]/page.tsx` |
| Linha | **155** (aninhamentos a partir das linhas 29, 115, 117, 118, 121) |
| Regra | `typescript:S2004` — *Refactor this code to not nest functions more than 5 levels deep* |
| Impacto | MAINTAINABILITY — **HIGH** |
| Tags | `brain-overload` |
| Esforço estimado | 20 min |
| Criado em | 18/06/2026 |

**Relevância:** este é o arquivo da tela do leitor EPUB — a mesma área onde estão os bugs conhecidos de cálculo de percentual e barra de progresso (documentados em `docs/leitor/logica_zoom_paginas.md`) e alvo da branch `feature/restaurar-posicao-leitor-epub`. O Sonar apontou complexidade excessiva exatamente no ponto do código que já vem gerando defeitos.

---

## 8. Riscos identificados

| # | Risco | Impacto |
|---|---|---|
| R1 | Backend Java fora da análise estática | Nenhuma medição de qualidade, complexidade ou duplicação sobre 100% da lógica de negócio do servidor |
| R2 | Cobertura não reportada ao Sonar | Os 85% de cobertura JaCoCo não são visíveis nem monitorados; regressões de cobertura passam despercebidas |
| R3 | Análise apenas em `main` | Sem gate de qualidade no fluxo de trabalho real (`feature/*` → `develop` → `main`); problemas só aparecem depois do merge final |
| R4 | Quality Gate vermelho não bloqueia deploy | O `deploy-aws.yml` publica em produção independentemente do status do Quality Gate |
| R5 | Defasagem de 7 dias na análise | Features desenvolvidas na última semana não passaram por nenhuma análise estática |
| R6 | Secrets expostos em blocos `run:` | 6 ocorrências no workflow de deploy — risco concreto de vazamento em logs de execução |
| R7 | Actions referenciadas por tag mutável | 6 ocorrências — exposição a supply chain attack caso a tag seja reapontada |
| R8 | Migration `V002` com comparação `= NULL` | A condição nunca é satisfeita; possível inconsistência de dados já aplicada em produção |

---

## 9. Plano de correção

Ordenado por relação impacto/esforço.

| # | Ação | Efeito esperado | Esforço |
|---|---|---|---|
| 1 | Unificar em **um único** workflow de Sonar usando `-Dsonar.projectKey=mba-puc-alexandria_alexandria` e remover o duplicado | Elimina a corrida entre workflows e passa a gravar no projeto correto | 15 min |
| 2 | Analisar o backend via `mvn verify sonar:sonar` (e não pela scan-action genérica), com `-Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml` | Traz os 4 domínios Java **e os 85% de cobertura** para o Sonar | 30 min |
| 3 | Corrigir `sonar.projectKey` em `alexandria-backend/pom.xml:18` para `mba-puc-alexandria_alexandria` | Elimina a terceira configuração conflitante | 2 min |
| 4 | Remover o passo `npm test -- --coverage` do job de frontend (script inexistente) | Destrava o job que hoje falha antes do scan | 5 min |
| 5 | Executar a análise também em **pull requests** e na branch `develop` | Estabelece gate de qualidade no fluxo de trabalho real | 10 min |
| 6 | Corrigir as 6 ocorrências de `S7636` em `deploy-aws.yml` — passar secrets via `env:` em vez de interpolar em `run:` | **Deve restaurar o Quality Gate para verde** (é a única condição em falha) | 30 min |
| 7 | Corrigir a comparação `= NULL` em `V002__Create_Authors_And_BookAuthors.sql:18` | Resolve o único bug com risco funcional real | 15 min |
| 8 | Fixar as actions por SHA completo (6 ocorrências de `S7637`) | Fecha o risco de supply chain | 20 min |
| 9 | Adicionar `type="button"` nos 26 casos de `S9011` | Reduz metade dos code smells | 30 min |
| 10 | Refatorar o aninhamento em `leitor/[id]/page.tsx:155` | Remove o único CRITICAL e reduz a complexidade da área com bugs abertos | 20 min |

Os itens **1 a 5** são os que transformam *"o Sonar está instalado"* em *"o Sonar está medindo o projeto"*. O item **6**, isoladamente, deve fechar o Quality Gate em verde.

---

## 10. Relação com a disciplina de Testes de Software

Esta análise conecta-se diretamente a três pontos discutidos nas aulas do Prof. Jehú Lara Ramos:

- **Aula 3 — Cobertura orientada a objetivos:** o projeto possui 85% de cobertura JaCoCo que **não é visível em lugar nenhum do processo**. Cobertura que não é reportada nem monitorada não sustenta decisão de risco.
- **Aula 4 §13 — Segurança como não funcional e shift left:** o Sonar está sinalizando risco de *supply chain* e de pipeline (14 dos 16 achados), não de regra de negócio. É precisamente o tipo de risco que a modelagem de ameaças antecipa na fase de arquitetura, em vez de descobrir em pentest tardio.
- **Aula 5 (prevista) — Complexidade ciclomática:** o CRITICAL `S2004` no leitor EPUB é um caso concreto de complexidade excessiva correlacionada com defeitos reais em produção — material direto para a discussão de caixa branca e métricas de complexidade.

---

## Anexo — Consultas utilizadas

Todas as informações foram obtidas via API pública do SonarCloud, sem necessidade de token (o projeto é público):

```bash
# Listar projetos da organização
curl "https://sonarcloud.io/api/components/search?organization=mba-puc-alexandria&qualifiers=TRK"

# Métricas gerais
curl "https://sonarcloud.io/api/measures/component?component=mba-puc-alexandria_alexandria\
&metricKeys=alert_status,bugs,vulnerabilities,code_smells,security_hotspots,coverage,\
duplicated_lines_density,ncloc,sqale_index,sqale_rating,reliability_rating,security_rating,\
complexity,cognitive_complexity,ncloc_language_distribution"

# Status do Quality Gate
curl "https://sonarcloud.io/api/qualitygates/project_status?projectKey=mba-puc-alexandria_alexandria"

# Bugs e vulnerabilidades
curl "https://sonarcloud.io/api/issues/search?componentKeys=mba-puc-alexandria_alexandria\
&types=BUG,VULNERABILITY&statuses=OPEN,CONFIRMED,REOPENED&ps=100"

# Code smells
curl "https://sonarcloud.io/api/issues/search?componentKeys=mba-puc-alexandria_alexandria\
&types=CODE_SMELL&statuses=OPEN,CONFIRMED,REOPENED&ps=100"

# Branches analisadas
curl "https://sonarcloud.io/api/project_branches/list?project=mba-puc-alexandria_alexandria"
```

---

*Documento gerado em 07/08/2026 a partir da análise SonarCloud de 31/07/2026.*
