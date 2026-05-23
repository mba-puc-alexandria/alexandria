# 🏗️ C4 Model — Alexandria

> **Legenda:** ✅ Implementado | 🔧 Parcialmente implementado | 📋 Plano (não implementado)

---

## Nível 1 — Context (System Context)

```mermaid
graph TB
    subgraph "Sistema Alexandria"
        FRONTEND["Site Biblioteca Alexandria<br/>Next.js<br/>✅ Implementado"]
        BACKEND["Alexandria API<br/>Spring Boot<br/>✅ Implementado"]
    end

    USUARIO(["Usuário<br/>(Navegador)"])
    GUTENDEX_API(["Gutendex API<br/>(API pública externa)"])
    EVENTBRIDGE(["AWS EventBridge Scheduler<br/>✅ Implementado"])

    USUARIO -->|"Navega no site"| FRONTEND
    FRONTEND -->|"HTTP REST (JSON)"| BACKEND
    BACKEND -->|"Busca livros"| GUTENDEX_API
    EVENTBRIDGE -->|"POST /api/jobs/sync-gutendex"| BACKEND
```

---

## Nível 2 — Container

```mermaid
graph TB
    subgraph "📱 Frontend (Next.js)"
        BROWSER["Navegador<br/>✅ Implementado"]
    end

    subgraph "☕ Backend (Spring Boot)"
        REST["Controllers REST<br/>✅ Implementado"]
        JOB["Job Async Service<br/>✅ Implementado"]
    end

    subgraph "🗄️ Banco de Dados"
        MYSQL_LOCAL["MySQL (Docker Compose)<br/>✅ Implementado"]
        MYSQL_RDS["MySQL RDS (AWS)<br/>✅ Implementado"]
    end

    subgraph "🔗 Integrações Externas"
        GUTENDEX["Gutendex API<br/>✅ Implementado"]
        EVENTBRIDGE["EventBridge Scheduler<br/>✅ Implementado"]
        SWAGGER["Swagger UI<br/>✅ Implementado"]
    end

    BROWSER -->|"HTTPS"| REST
    REST -->|"JDBC"| MYSQL_LOCAL
    REST -->|"JDBC"| MYSQL_RDS
    REST -->|"HTTP"| GUTENDEX
    REST --> SWAGGER
    JOB -->|"Async"| REST
    EVENTBRIDGE -->|"POST /api/jobs/sync-gutendex"| JOB
```

---

## Nível 3 — Component (Backend)

```mermaid
graph TB
    subgraph "adapter.in.rest — Controllers"
        AUTH_CTRL["AuthController<br/>✅ /auth"]
        BOOK_CTRL["BookController<br/>✅ /books"]
        UB_CTRL["UserBooksController<br/>✅ /user-books 🔒"]
        JOB_CTRL["JobController<br/>✅ /api/jobs"]
    end

    subgraph "adapter.in.job — Jobs"
        SYNC_JOB["SyncGutendexJobService<br/>✅ @Async"]
    end

    subgraph "application — Use Cases"
        AUTH_UC["AuthenticateUserUseCase<br/>✅"]
        REG_UC["RegisterUserUseCase<br/>✅"]
        CR_BOOK_UC["CreateBookUseCase<br/>✅"]
        GET_BOOK_UC["GetBookUseCase<br/>✅"]
        LIST_BOOK_UC["ListBooksUseCase<br/>✅"]
        UPD_BOOK_UC["UpdateBookUseCase<br/>✅"]
        DEL_BOOK_UC["DeleteBookUseCase<br/>✅"]
        SEARCH_UC["SearchBookByTitleUseCase<br/>✅"]
        SYNC_ALL_UC["SyncAllGutendexBooksUseCase<br/>✅"]
        ADD_UB_UC["AddUserBooksUseCase<br/>✅"]
        LIST_UB_UC["ListUserBooksUseCase<br/>✅"]
        UPD_UB_UC["UpdateUserBooksUseCase<br/>✅"]
        REM_UB_UC["RemoveUserBooksUseCase<br/>✅"]
    end

    subgraph "domain — Núcleo"
        BOOK_DOMAIN["Book<br/>✅"]
        AUTHOR_DOMAIN["Author<br/>✅"]
        USER_DOMAIN["User<br/>✅"]
        USERBOOKS_DOMAIN["UserBooks<br/>✅"]
        PORTS["BookRepository (port)<br/>AuthorRepository (port)<br/>UserRepository (port)<br/>UserBooksRepository (port)<br/>BookApiClient (port)<br/>✅"]
    end

    subgraph "adapter.out — Infraestrutura"
        REPOS["BookRepositoryImpl<br/>AuthorRepositoryImpl<br/>UserRepositoryImpl<br/>UserBooksRepositoryImpl<br/>✅"]
        JPA["BookJpaRepository<br/>AuthorJpaRepository<br/>UserJpaRepository<br/>UserBooksJpaRepository<br/>✅"]
        ENTITIES["BookEntity<br/>AuthorEntity<br/>UserEntity<br/>UserBooksEntity<br/>✅"]
        MAPPERS["BookMapper<br/>AuthorMapper<br/>UserMapper<br/>UserBooksMapper<br/>✅"]
        GUTENDEX_CLIENT["GutendexClient<br/>✅"]
        GUTENDEX_MAPPER["GutendexMapper<br/>✅"]
    end

    subgraph "config — Infraestrutura"
        BEANS["BeanConfiguration<br/>✅"]
        SECURITY["SecurityConfig<br/>✅"]
        CORS["CorsConfig<br/>✅"]
        JWT["JwtTokenProvider<br/>JwtAuthenticationFilter<br/>JwtAuthenticationEntryPoint<br/>✅"]
        ASYNC["AsyncConfig<br/>✅"]
        SWAGGER_CFG["OpenApiConfig<br/>✅"]
    end

    subgraph "advice — Error Handling"
        ERR_HANDLER["GlobalExceptionHandler<br/>✅"]
    end

    AUTH_CTRL --> AUTH_UC
    AUTH_CTRL --> REG_UC
    BOOK_CTRL --> CR_BOOK_UC
    BOOK_CTRL --> GET_BOOK_UC
    BOOK_CTRL --> LIST_BOOK_UC
    BOOK_CTRL --> UPD_BOOK_UC
    BOOK_CTRL --> DEL_BOOK_UC
    BOOK_CTRL --> SEARCH_UC
    JOB_CTRL --> SYNC_JOB
    SYNC_JOB --> SYNC_ALL_UC
    SYNC_ALL_UC --> CR_BOOK_UC
    UB_CTRL --> ADD_UB_UC
    UB_CTRL --> LIST_UB_UC
    UB_CTRL --> UPD_UB_UC
    UB_CTRL --> REM_UB_UC

    CR_BOOK_UC --> PORTS
    GET_BOOK_UC --> PORTS
    LIST_BOOK_UC --> PORTS
    UPD_BOOK_UC --> PORTS
    DEL_BOOK_UC --> PORTS
    SEARCH_UC --> PORTS
    ADD_UB_UC --> PORTS
    LIST_UB_UC --> PORTS
    UPD_UB_UC --> PORTS
    REM_UB_UC --> PORTS
    AUTH_UC --> PORTS
    REG_UC --> PORTS

    PORTS --> REPOS
    REPOS --> JPA
    JPA --> ENTITIES
    REPOS --> MAPPERS
    PORTS --> GUTENDEX_CLIENT
    GUTENDEX_CLIENT --> GUTENDEX_MAPPER
```

---

## Nível 3 — Component (Frontend)

```mermaid
graph TB
    subgraph "📱 Páginas (Next.js App Router)"
        LANDING["/ (Landing Page)<br/>✅ page.tsx"]
        LOGIN["/login<br/>✅ page.tsx"]
        REGISTER["/registrar<br/>✅ page.tsx"]
        EXPLORE["/explorar<br/>✅ page.tsx (com busca)"]
        BOOK_DETAIL["/explorar/[id]<br/>✅ page.tsx"]
        BIBLIOTECA["/biblioteca<br/>✅ page.tsx (coleção pessoal)"]
        LEITOR["/leitor<br/>✅ page.tsx"]
        LEITOR_ID["/leitor/[id]<br/>✅ page.tsx"]
        DASHBOARD["/dashboard<br/>✅ page.tsx"]
        EMPRESTIMOS["/emprestimos<br/>✅ page.tsx"]
        CONFIG["/configuracoes<br/>✅ page.tsx"]
        SUPORTE["/suporte<br/>✅ page.tsx"]
    end

    subgraph "🧩 Componentes"
        BOOK_CARD["BookCard<br/>✅"]
        SIDEBAR["Sidebar<br/>✅"]
        HEADER["Header / MobileHeader<br/>✅"]
        BOTTOM_NAV["BottomNav<br/>✅"]
    end

    subgraph "🔧 Contextos"
        AUTH_CTX["AuthContext<br/>✅"]
    end

    subgraph "📡 API Layer"
        API_LIB["api.ts<br/>✅ login, register, getBooks,<br/>searchBooks, getBookById,<br/>addUserBook, getUserBooks,<br/>updateUserBook"]
        PROXY["proxy.ts<br/>✅"]
    end

    EXPLORE --> BOOK_CARD
    BIBLIOTECA --> BOOK_CARD
    AUTH_CTX --> API_LIB
    LOGIN --> API_LIB
    REGISTER --> API_LIB
    EXPLORE --> API_LIB
    BIBLIOTECA --> API_LIB
    LOGIN --> AUTH_CTX
    REGISTER --> AUTH_CTX
```

---

## 📋 Planos (Não Implementados)

### 🔮 Autenticação Seletiva
**Arquivo:** `markdown/plano_autenticacao_seletiva.md`

| Endpoint | Hoje | Plano |
|---|---|---|
| `GET /books/**` | ✅ Público | ✅ Público (mantém) |
| `POST /books` | ✅ Público | 🔒 **Autenticado** |
| `PUT /books/{id}` | ✅ Público | 🔒 **Autenticado** |
| `DELETE /books/{id}` | ✅ Público | 🔒 **Autenticado** |
| `POST /api/jobs/sync-gutendex` | ✅ Público | 🔑 **API Key** |
| `/user-books/**` | 🔒 Autenticado | 🔒 Autenticado (mantém) |

---

### 🔮 Flyway em Produção (pós-MVP)
| O que | Status |
|---|---|
| Migrations V0, V001, V002 | ✅ Código pronto |
| Flyway ativo no RDS | 📋 **Aguardando pós-MVP** |
| Migration V003 (schema atual) | 📋 **Precisa ser criada** |

---

## ✅ Resumo Geral

| Componente | Implementado | Em Plano |
|---|---|---|
| **Backend** — Domain, Application, Adapters | ✅ 100% | — |
| **Backend** — Controllers REST | ✅ 4 controllers | — |
| **Backend** — Security (JWT) | ✅ | 🔒 Reforçar escrita |
| **Backend** — Job Sincronização | ✅ | — |
| **Backend** — Flyway | ✅ Migrações | 📋 Ativar pós-MVP |
| **Backend** — Swagger | ✅ | Ajustes finos |
| **Backend** — Testes | ✅ 237 testes | — |
| **Frontend** — Páginas | ✅ 12 páginas | — |
| **Frontend** — API integration | ✅ | — |
| **Infra** — Docker (local) | ✅ | — |
| **Infra** — Docker (AWS) | ✅ | — |
| **Infra** — Kubernetes | ✅ | — |
| **Infra** — CI/CD (.github) | ⚠️ Apenas hooks | 📋 GitHub Actions |
| **Infra** — AWS EventBridge | ✅ | — |
