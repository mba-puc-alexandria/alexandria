# 🏗️ C4 Model — Alexandria

> **Legenda:** ✅ Implementado | 🔧 Parcialmente implementado | 📋 Plano (não implementado)

---

## Nível 1 — Context (System Context)

```mermaid
graph TB
    subgraph "Sistema Alexandria"
        FRONTEND["Site Biblioteca Alexandria<br/>Next.js + PWA<br/>✅ Implementado"]
        BACKEND["Alexandria API<br/>Spring Boot<br/>✅ Implementado"]
    end

    USUARIO(["Usuário<br/>(Navegador / PWA)"])
    GUTENDEX_API(["Gutendex API<br/>(API pública externa)"])
    GOOGLE(["Google OAuth 2.0<br/>(Login social)"])
    EVENTBRIDGE(["AWS EventBridge Scheduler<br/>✅ Implementado"])

    USUARIO -->|"Navega no site"| FRONTEND
    FRONTEND -->|"HTTP REST (JSON)"| BACKEND
    FRONTEND -->|"OAuth (token Google)"| GOOGLE
    BACKEND -->|"Valida token"| GOOGLE
    BACKEND -->|"Busca livros"| GUTENDEX_API
    EVENTBRIDGE -->|"POST /api/jobs/sync-gutendex"| BACKEND
```

---

## Nível 2 — Container

```mermaid
graph TB
    subgraph "📱 Frontend (Next.js + PWA)"
        BROWSER["Navegador / PWA<br/>Service Worker + Cache<br/>✅ Implementado"]
        EPUB_CACHE["Cache EPUB<br/>IndexedDB + LRU (30 itens, 250MB)<br/>✅ Implementado"]
        LEITOR_EPUB["Leitor EPUB (react-reader)<br/>✅ Implementado"]
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
        GOOGLE["Google OAuth 2.0<br/>✅ Implementado"]
    end

    BROWSER -->|"HTTPS"| REST
    BROWSER -->|"Validação remota"| GOOGLE
    REST -->|"Valida token"| GOOGLE
    LEITOR_EPUB -->|"Proxy /api/epub"| BROWSER
    LEITOR_EPUB -->|"Salva/recupera"| EPUB_CACHE
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
        AUTH_CTRL["AuthController<br/>✅ /auth, /auth/google"]
        BOOK_CTRL["BookController<br/>✅ /books"]
        UB_CTRL["UserBooksController<br/>✅ /user-books 🔒<br/>✅ /user-books/book/{bookId}"]
        JOB_CTRL["JobController<br/>✅ /api/jobs 🔑 ADMIN"]
        PROFILE_CTRL["ProfileController<br/>✅ /profile 🔒"]
    end

    subgraph "adapter.in.job — Jobs"
        SYNC_JOB["SyncGutendexJobService<br/>✅ @Async"]
    end

    subgraph "application — Use Cases"
        AUTH_UC["AuthenticateUserUseCase<br/>✅"]
        REG_UC["RegisterUserUseCase<br/>✅"]
        GOOGLE_AUTH_UC["GoogleAuthUseCase<br/>✅"]
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
        GET_UB_BOOK_UC["GetUserBookByBookIdUseCase<br/>✅"]
        GET_PROFILE_UC["GetProfileUseCase<br/>✅"]
        UPD_PROFILE_UC["UpdateProfileUseCase<br/>✅"]
        UPD_PASSWORD_UC["UpdatePasswordUseCase<br/>✅"]
    end

    subgraph "domain — Núcleo"
        BOOK_DOMAIN["Book<br/>✅"]
        AUTHOR_DOMAIN["Author<br/>✅"]
        USER_DOMAIN["User (com Role: USER|ADMIN)<br/>✅"]
        USERBOOKS_DOMAIN["UserBooks<br/>✅"]
        AUTHENTICATED_USER["AuthenticatedUser (record)<br/>✅"]
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
        SECURITY["SecurityConfig (role-based)<br/>✅"]
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
    AUTH_CTRL --> GOOGLE_AUTH_UC
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
    UB_CTRL --> GET_UB_BOOK_UC
    PROFILE_CTRL --> GET_PROFILE_UC
    PROFILE_CTRL --> UPD_PROFILE_UC
    PROFILE_CTRL --> UPD_PASSWORD_UC

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
    GET_UB_BOOK_UC --> PORTS
    GET_PROFILE_UC --> PORTS
    UPD_PROFILE_UC --> PORTS
    UPD_PASSWORD_UC --> PORTS
    AUTH_UC --> PORTS
    REG_UC --> PORTS
    GOOGLE_AUTH_UC --> PORTS

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
        HOME["/ (redireciona → /explorar)<br/>✅ page.tsx"]
        LOGIN["/login<br/>✅ page.tsx"]
        REGISTER["/registrar<br/>✅ page.tsx"]
        EXPLORE["/explorar<br/>✅ page.tsx (com busca e top books)"]
        BOOK_DETAIL["/explorar/[id]<br/>✅ page.tsx (add to library + leitor link)"]
        BIBLIOTECA["/biblioteca<br/>✅ page.tsx (filtros, progresso, remoção)"]
        LEITOR["/leitor<br/>✅ page.tsx (placeholder estático)"]
        LEITOR_ID["/leitor/[id]<br/>✅ page.tsx (react-reader + progresso real)"]
        DASHBOARD["/dashboard<br/>✅ page.tsx (stats reais da API)"]
        EMPRESTIMOS["/emprestimos<br/>✅ page.tsx (mock estático)"]
        CONFIG["/configuracoes<br/>✅ page.tsx (perfil + senha reais)"]
        SUPORTE["/suporte<br/>✅ page.tsx (FAQ + contato)"]
    end

    subgraph "🧩 Componentes"
        BOOK_CARD["BookCard<br/>✅"]
        SIDEBAR["Sidebar<br/>✅"]
        HEADER["Header / MobileHeader<br/>✅"]
        BOTTOM_NAV["BottomNav<br/>✅"]
        LOGIN_MODAL["LoginModal<br/>✅ (modal de login com Google)"]
        AUTH_MODAL_TRIGGER["AuthModalTrigger<br/>✅ (?auth=1)"]
        GOOGLE_PROVIDER["GoogleProvider<br/>✅ (GoogleOAuthProvider)"]
        SERVICE_WORKER["ServiceWorkerRegister<br/>✅ (PWA)"]
    end

    subgraph "🔧 Contextos"
        AUTH_CTX["AuthContext<br/>✅ (login, logout, loginWithGoogle, updateUsername)"]
        AUTH_MODAL_CTX["AuthModalContext<br/>✅ (modal login tracking)"]
    end

    subgraph "📡 API Layer"
        API_LIB["api.ts<br/>✅ login, register, loginWithGoogle,<br/>getBooks, searchBooks, getBookById,<br/>addUserBook, getUserBooks, updateUserBook,<br/>removeUserBook, getProfile, updateProfile,<br/>updatePassword, getTopBooks, apiFetch"]
        PROXY["proxy.ts<br/>✅"]
        EPUB_ROUTE["/api/epub/route.ts<br/>✅ proxy download EPUB"]
    end

    subgraph "📚 Hooks / Utilitários"
        USE_EPUB["useEpub<br/>✅ (cache + IndexedDB)"]
        EPUB_CACHE["epub-cache<br/>✅ (LRU + IndexedDB)"]
    end

    subgraph "📁 Dados Mockados"
        BOOKS_DATA["data/books.ts<br/>✅ (mock estático p/ leitor sem API)"]
    end

    EXPLORE --> BOOK_CARD
    BIBLIOTECA --> BOOK_CARD
    AUTH_CTX --> API_LIB
    LOGIN --> API_LIB
    REGISTER --> API_LIB
    EXPLORE --> API_LIB
    BIBLIOTECA --> API_LIB
    CONFIG --> API_LIB
    DASHBOARD --> API_LIB
    BOOK_DETAIL --> API_LIB
    LEITOR_ID --> USE_EPUB
    LEITOR_ID --> API_LIB
    USE_EPUB --> EPUB_CACHE
    USE_EPUB --> EPUB_ROUTE
    LOGIN --> AUTH_CTX
    REGISTER --> AUTH_CTX
    LOGIN_MODAL --> AUTH_CTX
    LOGIN_MODAL --> AUTH_MODAL_CTX
    AUTH_MODAL_TRIGGER --> AUTH_MODAL_CTX
    BOOK_DETAIL --> AUTH_MODAL_CTX
```

---

## 📋 Planos (Não Implementados)

### 🔮 Autenticação Seletiva
✅ **Implementado!** A segurança por roles foi aplicada:

| Endpoint | Hoje |
|---|---|
| `GET /books/**` | ✅ Público |
| `POST /books` | 🔒 **ROLE_ADMIN** |
| `PUT /books/{id}` | 🔒 **ROLE_ADMIN** |
| `DELETE /books/{id}` | 🔒 **ROLE_ADMIN** |
| `POST /api/jobs/sync-gutendex` | 🔒 **ROLE_ADMIN** |
| `/user-books/**` | 🔒 Autenticado |
| `/profile/**` | 🔒 Autenticado |

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
| **Backend** — Controllers REST | ✅ 5 controllers (Auth, Book, UserBooks, Job, Profile) | — |
| **Backend** — Google OAuth | ✅ | — |
| **Backend** — Profile (GET/PUT /profile/me, PUT /profile/password) | ✅ | — |
| **Backend** — Security (Role-based: ADMIN x USER) | ✅ | — |
| **Backend** — Job Sincronização | ✅ | — |
| **Backend** — Flyway | ✅ Migrações | 📋 Ativar pós-MVP |
| **Backend** — Swagger | ✅ | Ajustes finos |
| **Backend** — Testes | ✅ | — |
| **Frontend** — Páginas | ✅ 12 páginas (incl. leitor real com react-reader) | — |
| **Frontend** — Leitor EPUB com cache (IndexedDB + LRU) | ✅ | — |
| **Frontend** — Login modal com Google OAuth | ✅ | — |
| **Frontend** — PWA (Service Worker + Manifest) | ✅ | — |
| **Frontend** — Dashboard com stats reais | ✅ | — |
| **Frontend** — Biblioteca com filtros, progresso e remoção | ✅ | — |
| **Frontend** — Configurações (perfil e senha reais) | ✅ | — |
| **Frontend** — API integration | ✅ | — |
| **Infra** — Docker (local) | ✅ | — |
| **Infra** — Docker (AWS) | ✅ | — |
| **Infra** — CI/CD (.github) | ⚠️ Apenas hooks | 📋 GitHub Actions |
| **Infra** — AWS EventBridge | ✅ | — |
