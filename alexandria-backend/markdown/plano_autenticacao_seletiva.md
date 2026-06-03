# 📋 Plano: Autenticação Seletiva + API Key para Job

## 🎯 Objetivo

Proteger operações de escrita (`POST`, `PUT`, `DELETE`) com JWT, manter leitura do catálogo pública, e proteger o endpoint do cronjob com API Key (já que o EventBridge não suporta JWT Bearer).

---

## 🧱 1. SecurityConfig — Ajustar Rotas

**Arquivo:** `src/main/java/com/pucsp/alexandria/config/SecurityConfig.java`

**Regra de ouro:** *Tudo é autenticado por padrão, exceto o que for explicitamente permitido.*

### Código atual (trecho relevante):

```java
.requestMatchers("/auth/**", "/api/jobs/**").permitAll()
.requestMatchers("/books/search").permitAll()
.requestMatchers("/books").permitAll()
.requestMatchers("/books/{id}").permitAll()
```

### Código após ajuste:

```java
// 🔓 Públicos (absolutamente necessários)
.requestMatchers("/auth/**").permitAll()                        // registrar e login
.requestMatchers("/actuator/health").permitAll()                // health check
.requestMatchers("/error").permitAll()                          // error handling
.requestMatchers("/swagger-ui/**", "/api-docs/**",
                 "/v3/api-docs/**", "/swagger-ui.html").permitAll()  // docs

// 🔓 Leitura pública do catálogo (somente GET)
.requestMatchers(HttpMethod.GET, "/books/**").permitAll()

// 🔓 Job de sincronização (EventBridge não suporta JWT)
.requestMatchers("/api/jobs/**").permitAll()

// 🔒 Todo o resto requer autenticação
.anyRequest().authenticated()
```

> **Importante:** Adicionar o import `org.springframework.web.bind.annotation.RequestMethod` ou usar `org.springframework.http.HttpMethod`.

### Matriz de mudanças:

| Endpoint | Antes | Depois |
|---|---|---|
| `GET /books/**` | ✅ Público | ✅ Público (mantém) |
| `POST /books` | ✅ Público | 🔒 Autenticado |
| `PUT /books/{id}` | ✅ Público | 🔒 Autenticado |
| `DELETE /books/{id}` | ✅ Público | 🔒 Autenticado |
| `POST /api/jobs/**` | ✅ Público | ✅ Público (mantém, mas com API Key) |

---

## 🔑 2. JobController — Validar API Key

**Arquivo:** `src/main/java/com/pucsp/alexandria/adapter/in/rest/JobController.java`

### O que adicionar:

```java
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private static final Logger log = LoggerFactory.getLogger(JobController.class);

    private final SyncGutendexJobService syncGutendexJobService;

    @Value("${jobs.api-key}")
    private String expectedApiKey;

    public JobController(SyncGutendexJobService syncGutendexJobService) {
        this.syncGutendexJobService = syncGutendexJobService;
    }

    @PostMapping("/sync-gutendex")
    public ResponseEntity<Void> triggerSync(
            @RequestHeader("X-API-Key") String apiKey) {

        if (!expectedApiKey.equals(apiKey)) {
            log.warn("Tentativa de sincronização com API Key inválida.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            syncGutendexJobService.triggerSync();
            return ResponseEntity.accepted().build();
        } catch (TaskRejectedException e) {
            log.warn("Job de sincronização já está em execução. Requisição rejeitada.");
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
    }
}
```

### Configuração nos `.properties`:

**`src/main/resources/application.properties` (dev):**
```properties
jobs.api-key=dev-api-key
```

**`src/main/resources/application-rds.properties` (produção):**
```properties
jobs.api-key=${JOBS_API_KEY}
```

### Variável de ambiente:

| Variável | Obrigatória | Descrição |
|---|---|---|
| `JOBS_API_KEY` | Sim (RDS) | Chave secreta para autenticar o EventBridge |

---

## 📖 3. OpenApiConfig — Corrigir Swagger

**Arquivo:** `src/main/java/com/pucsp/alexandria/config/OpenApiConfig.java`

**Problema atual:** O `SecurityRequirement` global faz todos os endpoints aparecerem com cadeado 🔒 no Swagger, inclusive os públicos.

### O que mudar:

| Atual | Novo |
|---|---|
| `.addSecurityItem(new SecurityRequirement().addList("bearerAuth"))` global | Remover o `addSecurityItem` global |
| Nenhuma anotação nos controllers | Adicionar `@SecurityRequirement` apenas nos métodos autenticados |

### Código após ajuste:

```java
return new OpenAPI()
    .info(new Info()
        .title("Alexandria API")
        .version("0.0.1-SNAPSHOT")
        .description("API Rest para gerenciamento de livros e biblioteca pessoal")
        .license(new License()
            .name("Apache 2.0")
            .url("https://www.apache.org/licenses/LICENSE-2.0")))
    .components(new Components()
        .addSecuritySchemes("bearerAuth", new SecurityScheme()
            .name("bearerAuth")
            .type(SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT")));
// ⚠️ NÃO usar .addSecurityItem() global — o cadeado aparece
//    apenas onde houver @SecurityRequirement anotado no método
```

### Anotações nos controllers:

**Métodos autenticados** — adicionar `@SecurityRequirement` (ex: `BookController.create`, `BookController.update`, `BookController.delete`, `UserBooksController.*`):

```java
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@PostMapping
@SecurityRequirement(name = "bearerAuth")
public ResponseEntity<Void> create(@RequestBody CreateBookRequest request) {
    // ...
}
```

**Métodos públicos** — nenhuma anotação de segurança. O Swagger não exibirá o cadeado.

> **Nota:** Para evitar repetir `@SecurityRequirement` em cada método, é possível criar uma anotação customizada `@AuthenticatedEndpoint` que encapsule a lógica. Mas para este plano, manteremos a anotação direta para simplicidade.

---

## 🏗️ 4. Fluxo de DTOs — Sem Alterações

O fluxo de DTOs entre camadas **não muda**. A única diferença é que o controller agora exige um token JWT válido (ou API Key) antes de processar a requisição.

```
Request com JWT (ou API Key)
    ↓
SecurityConfig / JwtAuthenticationFilter (valida token)
    ↓
Controller (adapter/in/rest/dto/*Request.java)
    ↓
UseCase Input (application/*/dto/*Input.java)
    ↓
UseCase.execute()
    ↓
UseCase Output (application/*/dto/*Output.java)
    ↓
Controller
    ↓
Response (adapter/in/rest/dto/*Response.java)
    ↓
HTTP Response (JSON)
```

---

## 📋 5. Resumo das Alterações por Arquivo

| # | Arquivo | Ação |
|---|---|---|
| 1 | `config/SecurityConfig.java` | Separar `GET /books/**` (público) de `POST/PUT/DELETE /books/**` (autenticado) |
| 2 | `adapter/in/rest/JobController.java` | Adicionar campo `expectedApiKey` + validação do header `X-API-Key` |
| 3 | `config/OpenApiConfig.java` | Remover `addSecurityItem()` global |
| 4 | `adapter/in/rest/BookController.java` | Adicionar `@SecurityRequirement(name = "bearerAuth")` nos métodos `create`, `update`, `delete` |
| 5 | `adapter/in/rest/UserBooksController.java` | Adicionar `@SecurityRequirement(name = "bearerAuth")` nos métodos `list`, `add`, `update`, `remove` |
| 6 | `application.properties` | Adicionar `jobs.api-key=dev-api-key` |
| 7 | `application-rds.properties` | Adicionar `jobs.api-key=${JOBS_API_KEY}` |
| 8 | `markdown/ARCHITECTURE.md` | Atualizar tabela de endpoints públicos/autenticados |

---

## 🔄 6. Matriz Final de Endpoints

| Método | Path | Autenticação | Swagger mostra cadeado? |
|---|---|---|---|
| `POST` | `/auth/register` | ❌ Público | ❌ |
| `POST` | `/auth/login` | ❌ Público | ❌ |
| `GET` | `/books/search` | ❌ Público | ❌ |
| `GET` | `/books` | ❌ Público | ❌ |
| `GET` | `/books/{id}` | ❌ Público | ❌ |
| `POST` | `/books` | 🔒 JWT | ✅ |
| `PUT` | `/books/{id}` | 🔒 JWT | ✅ |
| `DELETE` | `/books/{id}` | 🔒 JWT | ✅ |
| `POST` | `/api/jobs/sync-gutendex` | 🔑 API Key (header `X-API-Key`) | ❌ (público) |
| `GET` | `/user-books` | 🔒 JWT | ✅ |
| `POST` | `/user-books` | 🔒 JWT | ✅ |
| `PUT` | `/user-books/{id}` | 🔒 JWT | ✅ |
| `DELETE` | `/user-books/{id}` | 🔒 JWT | ✅ |
| `GET` | `/actuator/health` | ❌ Público | ❌ |
| `GET` | `/swagger-ui/**`, `/api-docs/**` | ❌ Público | ❌ |

---

## 🧪 7. Testes

### O que precisa ser atualizado:

**Testes de integração dos controllers:**
- `BookControllerIntegrationTest.java` — adicionar token JWT nos testes de `POST`, `PUT`, `DELETE`
- `JobControllerIntegrationTest.java` — adicionar header `X-API-Key` no teste

**Novos cenários a testar:**

| Cenário | Controller | Teste |
|---|---|---|
| `POST /books` sem token | BookController | Deve retornar **401** |
| `POST /books` com token válido | BookController | Deve retornar **204** |
| `PUT /books/{id}` sem token | BookController | Deve retornar **401** |
| `DELETE /books/{id}` sem token | BookController | Deve retornar **401** |
| `POST /api/jobs/sync-gutendex` sem API Key | JobController | Deve retornar **403** |
| `POST /api/jobs/sync-gutendex` com API Key errada | JobController | Deve retornar **403** |
| `POST /api/jobs/sync-gutendex` com API Key correta | JobController | Deve retornar **202** |

---

## ✅ 8. Critérios de Aceitação

- [ ] `GET /books` funciona sem token
- [ ] `POST /books` retorna **401** sem token e **204** com token válido
- [ ] `PUT /books/{id}` retorna **401** sem token e **200** com token válido
- [ ] `DELETE /books/{id}` retorna **401** sem token e **204** com token válido
- [ ] `POST /api/jobs/sync-gutendex` retorna **403** sem header `X-API-Key`
- [ ] `POST /api/jobs/sync-gutendex` retorna **403** com header `X-API-Key` incorreto
- [ ] `POST /api/jobs/sync-gutendex` retorna **202** com header `X-API-Key` correto
- [ ] Swagger UI mostra cadeado 🔒 apenas nos endpoints que exigem JWT
- [ ] Testes de integração passando com as novas regras
- [ ] `ARCHITECTURE.md` atualizado com a nova divisão de endpoints

---

## ⏱️ 9. Estimativa de Esforço

| Tarefa | Tempo |
|---|---|
| SecurityConfig — separar métodos HTTP | 10 min |
| JobController — validar API Key | 15 min |
| OpenApiConfig — remover security global | 5 min |
| Anotações `@SecurityRequirement` nos controllers | 15 min |
| Propriedades + variáveis de ambiente | 10 min |
| Atualizar testes de integração | 30 min |
| Atualizar ARCHITECTURE.md | 15 min |
| **Total** | **~1h40** |

---

## ☁️ 10. Configuração no EventBridge (AWS)

Após o deploy, a API Destination do EventBridge deve ser configurada para enviar o header `X-API-Key`:

1. Acesse **Amazon EventBridge** > **API destinations**
2. Edite a API Destination `GutendexSyncEndpoint`
3. Em **Connection**, configure:
   - **Authorization Type**: `API Key`
   - **API Key name**: `X-API-Key`
   - **API Key value**: valor definido na variável `JOBS_API_KEY`
4. Salve e teste o schedule
