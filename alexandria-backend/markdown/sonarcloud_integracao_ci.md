# Integração SonarCloud com o Pipeline CI

## O que foi feito

### 1. `pom.xml` — Propriedades do SonarCloud

Adicionadas no bloco `<properties>`:

```xml
<sonar.projectKey>mba-puc-alexandria_alexandria-backend</sonar.projectKey>
<sonar.organization>mba-puc-alexandria</sonar.organization>
<sonar.host.url>https://sonarcloud.io</sonar.host.url>
```

### 2. `pom.xml` — Plugin do Sonar Scanner

Adicionado no bloco `<build><plugins>`:

```xml
<plugin>
    <groupId>org.sonarsource.scanner.maven</groupId>
    <artifactId>sonar-maven-plugin</artifactId>
    <version>5.1.0.14797</version>
</plugin>
```

### 3. `.github/workflows/ci.yaml` — Step de análise SonarCloud

Adicionado ao job `backend`, após o `./mvnw clean verify` (testes) e antes do upload de artefatos:

```yaml
- name: Análise SonarCloud
  working-directory: alexandria-backend/
  env:
    GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
    SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
  run: ./mvnw sonar:sonar -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml
```

## O que ainda precisa ser feito

### 1. Token do SonarCloud no GitHub Secrets

> 🔑 **Quem pode fazer:** alguém com acesso admin ao repositório no GitHub.

**Passos:**
1. Acessar [sonarcloud.io](https://sonarcloud.io) e fazer login
2. Ir em **My Account > Security**
3. Gerar um token (ex.: `alexandria-ci-token`)
4. Copiar o token gerado
5. No GitHub, ir em:
   `Settings > Secrets and variables > Actions > New repository secret`
6. Adicionar:
   - **Name:** `SONAR_TOKEN`
   - **Secret:** (o token copiado do SonarCloud)

> ⚠️ **Importante:** Se o SonarCloud GitHub App já está instalado, ele pode já ter o token configurado automaticamente como `SONAR_TOKEN`. Vale verificar se o secret já existe antes de criar um novo.

### 2. Verificar se o projeto existe no SonarCloud

O `projectKey` configurado foi:
```
mba-puc-alexandria_alexandria-backend
```

Alguém com acesso ao SonarCloud precisa confirmar que:
- O projeto com essa chave existe na organização `mba-puc-alexandria`
- Caso não exista, criar o projeto manualmente no SonarCloud

### 3. (Opcional) Remover container PostgreSQL do CI

O job `backend` do `ci.yaml` sobe um container PostgreSQL como service, mas os testes usam H2 em memória. O PostgreSQL não está sendo utilizado e poderia ser removido para agilizar o pipeline — porém isso não afeta a análise do Sonar.

---

## Fluxo esperado após tudo configurado

```
Push/PR
  ↓
Guardrails (valida política de branches)
  ↓
Backend:
  1. ./mvnw clean verify   → testes + relatório JaCoCo
  2. ./mvnw sonar:sonar    → envia análise + cobertura para SonarCloud
  ↓
Frontend (lint + testes + build)
  ↓
Integração E2E (Playwright)
```

A cobertura de testes aparecerá no SonarCloud após a primeira execução bem-sucedida do CI com o `SONAR_TOKEN` configurado.
