# Ambientes de Desenvolvimento e Produção

## Contexto

O backend é chamado via `NEXT_PUBLIC_API_URL`, lida em `src/lib/api.ts`.  
Antes deste step, o fallback hardcoded da URL da EC2 fazia com que `npm run dev` sempre chamasse o backend de produção — impossibilitando desenvolvimento local isolado.

---

## O que foi feito

### Arquivos criados

#### `.env.local`
Carregado pelo Next.js em todos os ambientes locais (`npm run dev` e `npm run build` rodados na máquina do dev). Tem a maior prioridade e **não deve ser commitado** (já está no `.gitignore` padrão do Next.js).

```
NEXT_PUBLIC_API_URL=http://localhost:8080
```

#### `.env.production`
Carregado automaticamente em builds de produção (`npm run build` no CI/servidor). Pode ser commitado — não contém segredos.

```
NEXT_PUBLIC_API_URL=http://ec2-18-225-37-127.us-east-2.compute.amazonaws.com:8080
```

### Arquivo modificado

#### `src/lib/api.ts` — linha 1
Removido o fallback hardcoded para a URL da EC2:

```ts
// antes
const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://ec2-18-225-37-127...';

// depois
const API_URL = process.env.NEXT_PUBLIC_API_URL!;
```

O `!` (non-null assertion) garante que, se a variável não estiver definida, o erro aparece cedo e de forma explícita — em vez de silenciosamente chamar produção.

---

## Ordem de prioridade dos arquivos de env no Next.js

```
.env.local              → prevalece sempre, nunca commitado
.env.development.local  → local + ambiente dev
.env.production.local   → local + ambiente prod
.env.development        → todos os devs, ambiente dev
.env.production         → todos os devs, ambiente prod  ← usamos este
.env                    → base, todos os ambientes
```

---

## Como rodar o backend localmente

O backend é um projeto Spring Boot (Java). Para subir localmente:

```bash
# na pasta alexandria-backend
./mvnw spring-boot:run
```

Ou via Docker Compose (se configurado):

```bash
docker compose up
```

O backend sobe na porta `8080` por padrão.  
O CORS já está configurado no `CorsConfig.java` para aceitar `http://localhost:3000`, então nenhuma alteração no backend é necessária.

---

## Fluxo resultante

| Contexto | Arquivo carregado | Backend chamado |
|---|---|---|
| `npm run dev` (local) | `.env.local` | `http://localhost:8080` |
| `npm run build` no CI/servidor | `.env.production` | EC2 produção |
| Dev sem `.env.local` | `.env.production` | EC2 produção (fallback explícito) |

---

## Observação — variáveis `NEXT_PUBLIC_`

Variáveis com prefixo `NEXT_PUBLIC_` são **embutidas no bundle** em tempo de build — não são lidas em runtime.  
Isso significa que trocar o `.env.local` requer reiniciar o servidor de dev (`npm run dev`) para ter efeito.

---

## Swagger / OpenAPI (Backend)

O backend expõe a documentação interativa da API via **SpringDoc OpenAPI** (Swagger UI).

### URLs de acesso

| Recurso | Local | Produção |
|---|---|---|
| Swagger UI | [localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) | [ec2.../swagger-ui.html](http://ec2-18-225-37-127.us-east-2.compute.amazonaws.com:8080/swagger-ui.html) |
| OpenAPI JSON | [localhost:8080/api-docs](http://localhost:8080/api-docs) | [ec2.../api-docs](http://ec2-18-225-37-127.us-east-2.compute.amazonaws.com:8080/api-docs) |

### Dependência Maven

Configurada em `alexandria-backend/pom.xml`:

```xml
<!-- SpringDoc OpenAPI (Swagger) -->
<dependency>
  <groupId>org.springdoc</groupId>
  <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
  <version>2.7.0</version>
</dependency>
```

### Configuração

**`application-rds.properties`:**
```properties
springdoc.api-docs.path=/swagger-ui.html
springdoc.swagger-ui.path=/swagger-ui.html
```

**`OpenApiConfig.java`** (`alexandria-backend/src/main/java/com/pucsp/alexandria/config/`):  
Define título, descrição e esquema de segurança Bearer JWT — endpoints protegidos exigem o header `Authorization: Bearer <token>` para serem testados via Swagger UI.

### Autenticação no Swagger UI

As rotas de Swagger são **públicas** (liberadas no `SecurityConfig.java`):
```java
.requestMatchers("/swagger-ui/**", "/api-docs/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
```

Para testar endpoints autenticados diretamente no Swagger:
1. Chame `POST /auth/login` com usuário e senha
2. Copie o `token` retornado
3. Clique em **Authorize** (cadeado) no topo da página
4. Cole o token no campo `bearerAuth` e confirme
