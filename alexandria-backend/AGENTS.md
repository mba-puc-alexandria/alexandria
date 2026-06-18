# Alexandria Backend - Repository Overview

## Project Description

Alexandria is a REST API for personal library management. It allows users to:
- Browse and search books from both local entries and the **Gutendex** (Project Gutenberg) API
- Manage a personal reading list with status tracking (To Read / Reading / Done)
- Track reading progress and rate finished books
- Register and authenticate via JWT-based auth

The project is built by students (Trybe/PUC-SP) as a learning exercise in **Clean Architecture with Java and Spring Boot**.

## Key Technologies

- **Java 17** with **Spring Boot 3.4.4**
- **MySQL 8.0** (via Docker Compose) + H2 for tests
- **Flyway** for database migrations
- **Spring Security** + **JWT** (jjwt 0.12.6) for authentication
- **Spring Data JPA** + Hibernate
- **JaCoCo** for test coverage
- **Testcontainers** for integration tests
- **SpringDoc OpenAPI** (Swagger UI)
- **Docker** multi-stage build
- **Maven** wrapper

## Architecture Overview

The project follows **Hexagonal Architecture** (Ports & Adapters), organized in three layers:

### 1. Domain Layer (`domain/`)
Core business logic with no framework dependencies:
- **Entities**: `Book`, `User`, `Author`, `UserBooks` — rich domain models with factory methods (`create`, `restore`, `updateWith`)
- **Value Objects**: `Email`, `BookId`, `UserId`, `AuthorId`, `AuthenticatedUser`, `BookSource`, `UserBooksStatus`
- **Repository Interfaces**: `BookRepository`, `UserRepository`, `AuthorRepository`, `UserBooksRepository`
- **External API Interface**: `BookApiClient` port
- **Domain Exceptions**: e.g., `BookNotFoundException`, `InvalidUserException`, `DuplicateUserBooksException`

### 2. Application Layer (`application/`)
Use cases that orchestrate domain logic:
- `book/` — CRUD + search + Gutendex sync (`CreateBookUseCase`, `SyncAllGutendexBooksUseCase`, etc.)
- `auth/` — `RegisterUserUseCase`, `AuthenticateUserUseCase`
- `userbooks/` — `AddUserBooksUseCase`, `ListUserBooksUseCase`, etc.
- Each use case has its own input/output DTOs in a `dto/` sub-package

### 3. Adapter Layer (`adapter/`)
- **Inbound** (`adapter/in/`):
  - REST Controllers: `BookController`, `AuthController`, `UserBooksController`, `JobController`
  - Async Job: `SyncGutendexJobService`
  - DTOs for request/response mapping
- **Outbound** (`adapter/out/`):
  - JPA Persistence: `BookRepositoryImpl`, `UserRepositoryImpl`, etc. + entity classes + mappers
  - Gutendex Client: REST client to `https://gutendex.com/books`
  - JPA Repositories extending Spring Data

### Data Flow

```
Client → REST Controller → Use Case (application) → Domain Entity
                                                     ↓
                                              Repository Port (interface)
                                                     ↓
                                          Repository Impl (adapter/out)
                                                     ↓
                                              JPA Repository → Database
```

For Gutendex sync:
```
Client → POST /api/jobs/sync-gutendex → Async Job → SyncAllGutendexBooksUseCase
  → CreateBookUseCase → BookApiClient (GutendexClient) → Gutendex API
  → BookRepository → Database
```

### Authentication Flow
```
Client → POST /auth/register → RegisterUserUseCase → UserRepository
Client → POST /auth/login → AuthenticateUserUseCase → JwtTokenProvider → JWT token
Subsequent requests → JwtAuthenticationFilter → SecurityContext → UserBooksController
```

## Directory Structure

```
src/
├── main/
│   ├── java/com/pucsp/alexandria/
│   │   ├── AlexandriaApplication.java          # Entry point
│   │   ├── adapter/
│   │   │   ├── in/
│   │   │   │   ├── job/SyncGutendexJobService.java    # Async sync job
│   │   │   │   └── rest/                              # REST controllers + DTOs
│   │   │   │       ├── auth/AuthController.java
│   │   │   │       ├── BookController.java
│   │   │   │       ├── UserBooksController.java
│   │   │   │       ├── JobController.java
│   │   │   │       └── dto/                    # Request/Response DTOs
│   │   │   └── out/
│   │   │       └── persistence/                # JPA implementations
│   │   │           ├── entity/                 # JPA entities
│   │   │           ├── jpa/                    # Spring Data JPA repos
│   │   │           ├── mapper/                 # Domain <-> Entity mappers
│   │   │           ├── external/
│   │   │           │   ├── gutendex/           # Gutendex REST client
│   │   │           │   └── mapper/             # Gutendex DTO mapper
│   │   │           ├── BookRepositoryImpl.java
│   │   │           └── UserRepositoryImpl.java
│   │   ├── application/                        # Use cases
│   │   │   ├── auth/ + dto/
│   │   │   ├── book/ + dto/
│   │   │   └── userbooks/ + dto/
│   │   ├── domain/                             # Core domain
│   │   │   ├── author/ + exception/
│   │   │   ├── book/ + exception/ + external/
│   │   │   ├── user/ + exception/
│   │   │   ├── userbook/ + exception/
│   │   │   └── shared/ (Port, value objects)
│   │   ├── config/                             # Spring config
│   │   │   ├── jwt/ (JwtTokenProvider, filters)
│   │   │   ├── SecurityConfig.java
│   │   │   ├── CorsConfig.java
│   │   │   ├── OpenApiConfig.java
│   │   │   ├── AsyncConfig.java
│   │   │   └── BeanConfiguration.java
│   │   └── advice/                             # Global exception handler
│   └── resources/
│       ├── application.properties              # Main config (env vars)
│       ├── application-rds.properties          # RDS/AWS profile
│       └── db/migration/                       # Flyway migrations
│           ├── V0__Create_initial_tables.sql
│           ├── V001__Add_Gutendex_Fields_And_Remove_Genre.sql
│           └── V002__Create_Authors_And_BookAuthors.sql
├── test/
│   ├── java/.../                               # Unit + integration tests
│   └── resources/application.properties        # H2 test config

Root:
├── Dockerfile              # Multi-stage build
├── docker-compose.yaml     # MySQL 8.0
├── pom.xml                 # Maven config
├── scripts/
│   ├── deploy-backend-only.sh
│   ├── parameter-store.sh
│   └── parameter-store-query.sh
└── AGENTS.md               # This file
```

## Git Workflow — Branch `develop`

⚠️ **A branch `develop` recebe force push com frequência.** O histórico remoto pode ser reescrito, o que faz o `git pull` criar merges desnecessários e deixar seu local "ahead" do remote.

### ✅ Como sincronizar (em vez de `git pull`)
```bash
# Sempre que precisar sincronizar com a develop remota:
git fetch origin develop
git reset --hard origin/develop
```

**Isso descarta TODAS as alterações locais.** Se você tiver commits locais que quer preservar:
```bash
git stash
git fetch origin develop
git reset --hard origin/develop
git stash pop
# ou: crie uma branch com seus commits antes de resetar
```

### 🧠 Resumo do problema
- Alguém faz `git push --force` na `develop` → remote muda o histórico
- Você faz `git pull` → Git faz um **merge** dos históricos divergentes
- Isso cria um **merge commit** local que não existe no remote
- Seu local fica "ahead" do remote e o ciclo se repete

**Solução**: sempre usar `fetch + reset --hard` em vez de `pull`.

---

## Development Workflow

### Prerequisites
- Java 17+
- Docker (for MySQL)

### Start MySQL
```bash
docker compose up -d
```

### Run the application
```bash
./mvnw spring-boot:run
```

### Run tests
```bash
./mvnw test
```

### Build (without tests)
```bash
./mvnw clean package -DskipTests
```

### Build with Docker
```bash
docker build -t alexandria-backend .
```

### Test coverage report
```bash
./mvnw clean package
# Report at: target/site/jacoco/index.html
```

### API Documentation (when running)
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/api-docs
- Health: http://localhost:8080/actuator/health

### Environment Variables
| Variable | Default | Description |
|---|---|---|
| `DB_HOST` | `localhost` | MySQL host |
| `DB_PORT` | `3306` | MySQL port |
| `DB_NAME` | `alexandriadb` | Database name |
| `DB_USER` | `root` | Database user |
| `DB_PASSWORD` | `root` | Database password |
| `JWT_SECRET` | dev key | JWT signing secret |
| `JWT_EXPIRATION_MS` | `86400000` | Token expiry (24h) |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:3000` | CORS origins |

### Profiles
- **default**: Local MySQL with `spring.jpa.hibernate.ddl-auto=update`
- **rds**: AWS RDS with `ddl-auto=validate` and Flyway disabled (run via `--spring.profiles.active=rds`)

### Testing
- **Unit tests**: Domain entities, value objects, and use cases (plain JUnit)
- **Integration tests**: Controllers with `@SpringBootTest`, JPA repositories
- **In-memory H2** database for tests (configured in `src/test/resources/application.properties`)
- **Testcontainers** dependency available for MySQL container tests

### Notes
- Flyway is **disabled** by default (`spring.flyway.enabled=false`); schema is managed via Hibernate DDL auto-update
- The `scripts/` directory contains deployment scripts for EC2 — review before using (they contain hardcoded credentials)
