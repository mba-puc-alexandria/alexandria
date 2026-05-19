# 🏛️ Arquitetura do Alexandria Backend

## Tecnologias

| Tecnologia | Versão | Propósito |
|------------|--------|-----------|
| Java | 17 | Linguagem |
| Spring Boot | 4.0.3 | Framework principal |
| Maven | - | Gerenciamento de dependências |
| MySQL 8 | - | Banco de dados relacional |
| Spring Data JPA + Hibernate | - | ORM e acesso a dados |
| Flyway | - | Migrações de banco (habilitado no perfil `rds`) |
| RestTemplate | - | HTTP Client (Gutendex API) |
| Jackson | - | Serialização JSON |
| Spring Security | - | Autenticação e autorização (JWT) |
| JWT (jjwt) | 0.12.6 | Geração/validação de tokens JWT |
| SpringDoc OpenAPI | 2.7.0 | Documentação Swagger |
| AWS Parameter Store | 2.4.4 | Configuração externalizada (AWS) |
| H2 | - | Banco em memória para testes |
| Testcontainers | 1.19.0 | Testes de integração com MySQL |
| JaCoCo | 0.8.12 | Cobertura de testes |

---

## 🧱 Clean Architecture / Ports & Adapters

O projeto segue **Clean Architecture** com **Ports & Adapters** (Hexagonal Architecture). As dependências apontam **para dentro** — o Domínio nunca depende de Infraestrutura.

```
┌──────────────────────────────────────────────────────────────┐
│                     ADAPTERS DE ENTRADA                       │
│  (Controllers, DTOs de request/response, ExceptionHandler)   │
│                        ⬇️  chama                              │
├──────────────────────────────────────────────────────────────┤
│                    CAMADA DE APLICAÇÃO                        │
│           (Use Cases, DTOs de input/output)                  │
│                        ⬇️  usa                                │
├──────────────────────────────────────────────────────────────┤
│                    CAMADA DE DOMÍNIO                          │
│  (Entities, Value Objects, Enums, Ports, Exceptions)         │
│                        ⬆️  implementa                         │
├──────────────────────────────────────────────────────────────┤
│                    ADAPTERS DE SAÍDA                          │
│  (RepositoryImpl, ApiClientImpl, Mappers, JPA Entities)      │
└──────────────────────────────────────────────────────────────┘
```

---

## 📁 Estrutura de Pacotes

```
com.pucsp.alexandria
├── AlexandriaApplication.java              # Entry point Spring Boot
│
├── domain/                                 # 🧬 DOMÍNIO (núcleo)
│   ├── package-info.java
│   ├── shared/                             # Compartilhado entre agregados
│   │   ├── Port.java                       # Interface marcadora para portas
│   │   ├── exception/
│   │   │   └── DomainException.java        # Exceção base do domínio
│   │   └── valueobject/
│   │       ├── Id.java                     # Value Object genérico de ID
│   │       └── Email.java                  # Value Object de email c/ validação
│   │
│   ├── book/                               # Agregado: Book
│   │   ├── Book.java                       # Aggregate Root
│   │   ├── BookId.java                     # Value Object: id do livro
│   │   ├── BookSource.java                 # enum: GUTENDEX | LOCAL
│   │   ├── BookRepository.java             # Porta de saída (interface)
│   │   ├── external/
│   │   │   ├── BookApiClient.java          # Porta de saída externa
│   │   │   ├── BookData.java               # Record DTO (dados da API externa)
│   │   │   ├── AuthorData.java             # Record DTO (dados do autor externo c/ formatação)
│   │   │   └── PersonData.java             # Record DTO (dados brutos de pessoa da Gutendex)
│   │   └── exception/
│   │       ├── BookNotFoundException.java
│   │       ├── DuplicateBookException.java
│   │       └── InvalidBookException.java
│   │
│   ├── author/                             # Agregado: Author
│   │   ├── Author.java                     # Aggregate Root
│   │   ├── AuthorId.java                   # Value Object: id do autor
│   │   ├── AuthorRepository.java           # Porta de saída (interface)
│   │   └── exception/
│   │       └── InvalidAuthorException.java
│   │
│   ├── user/                               # Agregado: User
│   │   ├── User.java                       # Aggregate Root
│   │   ├── UserId.java                     # Value Object: id do usuário
│   │   ├── UserRepository.java             # Porta de saída (interface)
│   │   └── exception/
│   │       ├── InvalidUserException.java
│   │       ├── DuplicateUserException.java
│   │       └── InvalidCredentialsException.java
│   │
│   └── userbook/                           # Agregado: UserBook (relação user-livro)
│       ├── UserBooks.java                  # Aggregate Root
│       ├── UserBooksStatus.java            # enum: TOREAD | READING | DONE
│       ├── UserBooksRepository.java        # Porta de saída (interface)
│       └── exception/
│           ├── InvalidUserBooksException.java
│           ├── DuplicateUserBooksException.java
│           └── UserBooksNotFoundException.java
│
├── application/                            # ⚙️ APLICAÇÃO (Use Cases)
│   ├── package-info.java
│   │
│   ├── book/                               # Casos de uso de Book
│   │   ├── CreateBookUseCase.java          # Importa página da Gutendex
│   │   ├── GetBookUseCase.java
│   │   ├── ListBooksUseCase.java
│   │   ├── UpdateBookUseCase.java
│   │   ├── DeleteBookUseCase.java
│   │   ├── SearchBookByTitleUseCase.java
│   │   └── dto/
│   │       ├── BookOutput.java             # DTO de saída (com AuthorInfo interno)
│   │       ├── CreateBookInput.java
│   │       ├── CreateBookOutput.java
│   │       ├── UpdateBookInput.java
│   │       └── SearchBookOutput.java
│   │
│   ├── auth/                               # Casos de uso de Autenticação
│   │   ├── RegisterUserUseCase.java
│   │   ├── AuthenticateUserUseCase.java
│   │   └── dto/
│   │       ├── AuthInput.java
│   │       ├── AuthOutput.java
│   │       ├── RegisterInput.java
│   │       └── RegisterOutput.java
│   │
│   └── userbooks/                          # Casos de uso de UserBooks
│       ├── AddUserBooksUseCase.java
│       ├── ListUserBooksUseCase.java
│       ├── UpdateUserBooksUseCase.java
│       ├── RemoveUserBooksUseCase.java
│       └── dto/
│           ├── UserBooksOutput.java
│           ├── AddUserBooksInput.java
│           └── UpdateUserBooksInput.java
│
├── adapter/                                # 🏗️ INFRAESTRUTURA (Adapters)
│   ├── in/                                 # 🔵 Adapters de Entrada
│   │   └── rest/
│   │       ├── BookController.java         # REST Controller (/books)
│   │       ├── UserBooksController.java    # REST Controller (/user-books)
│   │       ├── auth/
│   │       │   ├── AuthController.java     # REST Controller (/auth)
│   │       │   └── dto/
│   │       │       ├── AuthRequest.java
│   │       │       ├── AuthResponse.java
│   │       │       ├── RegisterRequest.java
│   │       │       └── RegisterResponse.java
│   │       └── dto/
│   │           ├── BookResponse.java
│   │           ├── BookSummaryResponse.java # Resumo do livro (p/ listas)
│   │           ├── CreateBookRequest.java
│   │           ├── UpdateBookRequest.java
│   │           ├── SearchBookResponse.java
│   │           ├── UserBooksResponse.java
│   │           ├── AddUserBooksRequest.java
│   │           └── UpdateUserBooksRequest.java
│   │
│   └── out/                                # 🟠 Adapters de Saída
│       └── persistence/
│           ├── BookRepositoryImpl.java     # Implementa BookRepository
│           ├── AuthorRepositoryImpl.java   # Implementa AuthorRepository
│           ├── UserRepositoryImpl.java     # Implementa UserRepository
│           ├── UserBooksRepositoryImpl.java# Implementa UserBooksRepository
│           ├── BookApiClientImpl.java      # Implementa BookApiClient
│           ├── entity/
│           │   ├── BookEntity.java         # JPA @Entity (tabela: books)
│           │   ├── AuthorEntity.java       # JPA @Entity (tabela: authors)
│           │   ├── UserEntity.java         # JPA @Entity (tabela: users)
│           │   └── UserBooksEntity.java    # JPA @Entity (tabela: user_books)
│           ├── jpa/
│           │   ├── BookJpaRepository.java
│           │   ├── AuthorJpaRepository.java
│           │   ├── UserJpaRepository.java
│           │   └── UserBooksJpaRepository.java
│           ├── mapper/
│           │   ├── BookMapper.java         # BookEntity ↔ Book
│           │   ├── AuthorMapper.java       # AuthorEntity ↔ Author
│           │   ├── UserMapper.java         # UserEntity ↔ User
│           │   └── UserBooksMapper.java    # UserBooksEntity ↔ UserBooks
│           └── external/
│               ├── gutendex/
│               │   ├── GutendexClient.java # HTTP Client p/ Gutendex API
│               │   └── dto/
│               │       ├── GutendexSearchResponse.java
│               │       ├── GutendexBookResponse.java
│               │       ├── GutendexAuthorResponse.java
│               │       └── GutendexFormatsResponse.java
│               └── mapper/
│                   └── GutendexMapper.java # GutendexDTO → BookData
│
├── config/                                 # ⚙️ Configurações Spring
│   ├── BeanConfiguration.java              # Beans: Use Cases + RestTemplate
│   ├── SecurityConfig.java                 # Security Filter Chain + CORS
│   ├── CorsConfig.java                     # Configuração CORS
│   ├── OpenApiConfig.java                  # Configuração Swagger/OpenAPI
│   ├── UserDetailsServiceImpl.java         # UserDetailsService p/ Spring Security
│   └── jwt/
│       ├── JwtTokenProvider.java           # Geração/validação de tokens JWT
│       ├── JwtAuthenticationFilter.java    # Filtro de autenticação JWT
│       └── JwtAuthenticationEntryPoint.java# Tratamento de 401
│
├── advice/                                 # 🛡️ Tratamento global de erros
│   ├── GlobalExceptionHandler.java         # @RestControllerAdvice
│   └── ErrorResponse.java
│
└── resources/
    ├── application.properties              # Config padrão (MySQL local/Flyway desligado)
    ├── application-rds.properties          # Config RDS (Flyway ligado, SSL)
    └── db/migration/
        ├── V001__Add_Gutendex_Fields_And_Remove_Genre.sql
        └── V002__Create_Authors_And_BookAuthors.sql
```

---

## 🧬 Camada de Domínio — Regras e Padrões

### Entity (Aggregate Root)
- **Construtor privado** — apenas factory methods criam instâncias
- **Atributos imutáveis** com `final` e sem setters públicos
- **Regras de negócio** validadas dentro do domínio, nunca nos adapters

```java
public class Book {
    private final BookId id;
    private final String title;
    private final Set<AuthorId> authorIds;
    private final Long gutendexId;
    // ...

    private Book(BookId id, ...) { /* construtor privado */ }

    // Factory methods — únicas formas de criar
    public static Book createLocal(String title, Set<AuthorId> authorIds, Long publisherId) { ... }
    public static Book createFromGutendex(Long gutendexId, String title, Set<AuthorId> authorIds, ...) { ... }
    public static Book restore(Long id, String title, Set<Long> authorIds, ...) { ... }
}
```

### Value Objects
- Imutáveis, sem identidade própria (comparados por valor)
- Herdam de `Id<T>` (Value Object genérico para IDs)

```java
public class BookId extends Id<Long> {
    private BookId(Long value) { super(value); }
    public static BookId from(Long id) {
        if (id == null || id <= 0) throw ...;
        return new BookId(id);
    }
}

public class Email {
    private final String value;
    public Email(String value) {
        // valida formato no construtor (regex)
    }
}
```

### Ports (Interfaces de Saída)
- Definidas na camada de domínio
- Implementadas pelos adapters de saída

```java
// BookRepository — Porta de persistência
public interface BookRepository {
    Book save(Book book);
    Optional<Book> findById(Long id);
    Optional<Book> findByGutendexId(Long gutendexId);
    Page<Book> findAll(Pageable pageable);
    Page<Book> searchBookByQuery(String query, Pageable pageable);
    void delete(Book book);
    boolean existsByGutendexId(Long gutendexId);
}

// BookApiClient — Porta de API externa
public interface BookApiClient {
    List<BookData> searchByTitle(String query);
    List<BookData> getPage(int page);
}
```

### Regras de Validação por Agregado

| Agregado | Atributo | Regra |
|----------|----------|-------|
| **Book** | `title` | Obrigatório, max 500 chars |
| | `authorIds` | Pelo menos 1 autor |
| | `gutendexId` | Obrigatório p/ GUTENDEX, deve ser positivo |
| | `publisherId` | Obrigatório p/ LOCAL, deve ser positivo |
| **Author** | `name` | Obrigatório, max 255 chars |
| **User** | `username` | Obrigatório, max 255 chars |
| | `firstName` | Obrigatório, max 255 chars |
| | `lastName` | Obrigatório, max 255 chars |
| | `password` | Obrigatório, min 8 chars, max 255 chars |
| | `email` | Formato válido (regex) |
| **UserBooks** | `status` + `progress` + `rating` | Depende do status (ver abaixo) |

**Regras de UserBooks por status:**

| Status | progress | rating |
|--------|----------|--------|
| `TOREAD` | Deve ser `null` | Deve ser `null` |
| `READING` | Obrigatório (0–100) | Deve ser `null` |
| `DONE` | Deve ser `null` | Obrigatório (0–5) |

---

## ⚙️ Camada de Aplicação — Use Cases

Cada Use Case é uma classe **independente** com um único método público `execute(...)`.

### Regras:
1. Use Case **não pode ter anotações Spring** (são registrados como `@Bean` em `BeanConfiguration`)
2. Use Case recebe **Ports (interfaces)** no construtor
3. Use Case retorna **DTOs de saída** da aplicação (nunca objetos de domínio para o adapter)
4. Use Case **nunca depende de implementações concretas** (sempre das interfaces do domínio)
5. Use Cases que modificam dados são anotados com `@Transactional`

### Exemplos:

**CreateBookUseCase** — Importa uma página da Gutendex:
```java
public class CreateBookUseCase {
    public CreateBookOutput execute(CreateBookInput input) {
        // 1. Busca página na Gutendex API
        var bookDataList = bookApiClient.getPage(input.page());
        // 2. Para cada livro, ignora se já existe (por gutendexId)
        // 3. Cria autores se não existirem
        // 4. Salva livro
        return new CreateBookOutput(createdIds);
    }
}
```

**RegisterUserUseCase** + **AuthenticateUserUseCase** — Fluxo de auth:
```java
// Register: valida duplicidade de username/email, cria usuário
RegisterOutput execute(RegisterInput input);

// Login: valida credenciais, retorna dados do usuário
AuthOutput execute(AuthInput input);  // senha é validada no controller via PasswordEncoder
```

---

## 🏗️ Camada de Infraestrutura — Adapters

### 🔵 Adapter de Entrada (Controllers REST)

**Endpoints públicos (sem autenticação):**

| Método | Path | Descrição |
|--------|------|-----------|
| `POST` | `/auth/register` | Registrar novo usuário |
| `POST` | `/auth/login` | Login (retorna token JWT) |
| `GET` | `/books/search?query=` | Buscar livros por título/autor |
| `GET` | `/books` | Listar livros (paginado) |
| `GET` | `/books/{id}` | Buscar livro por ID |
| `POST` | `/books` | Importar página da Gutendex |
| `GET` | `/actuator/health` | Health check |
| `GET` | `/swagger-ui/**`, `/api-docs/**` | Documentação Swagger |

**Endpoints autenticados (requerem token JWT no header `Authorization: Bearer <token>`):**

| Método | Path | Descrição |
|--------|------|-----------|
| `GET` | `/user-books` | Listar livros do usuário (c/ filtro opcional por status) |
| `POST` | `/user-books` | Adicionar livro à coleção do usuário |
| `PUT` | `/user-books/{id}` | Atualizar status/progresso/rating de um livro |
| `DELETE` | `/user-books/{id}` | Remover livro da coleção do usuário |
| `PUT` | `/books/{id}` | Atualizar título de um livro |
| `DELETE` | `/books/{id}` | Deletar livro |

### Fluxo de DTOs entre camadas

```
HTTP Request
    ↓
Controller (adapter/in/rest/dto/*Request.java) ← tipos primitivos (Long, String)
    ↓
UseCase Input (application/*/dto/*Input.java) ← pode usar VO do domínio
    ↓
UseCase.execute()
    ↓
UseCase Output (application/*/dto/*Output.java) ← pode usar VO do domínio
    ↓
Controller
    ↓ (converte VO → tipos primitivos via .getValue() ou .from())
Response (adapter/in/rest/dto/*Response.java)
    ↓
HTTP Response (JSON)
```

**Regra importante:** DTOs dos adapters de entrada (`*Request.java`, `*Response.java`) usam **tipos primitivos** (`Long`, `String`), nunca Value Objects do domínio. A conversão `ValueObject → primitivo` é feita nos métodos `from()` dos DTOs de resposta.

---

### 🟠 Adapters de Saída

#### Persistência (MySQL)

**Tabelas após migrações Flyway:**

```sql
-- Tabela principal de livros
books
├── id              BIGINT PK AUTO_INCREMENT
├── title           VARCHAR(500) NOT NULL
├── gutendex_id     BIGINT UNIQUE NULL         ← ID externo da Gutendex
├── download_url    LONGTEXT NULL               ← URL de download (ePub)
├── cover_url       LONGTEXT NULL               ← URL da capa
├── languages       LONGTEXT NULL               ← Ex: "pt,en"
├── subjects        LONGTEXT NULL               ← Ex: "Romance;Ficção"
├── download_count  INT NULL                    ← Contagem de downloads
├── publisher_id    BIGINT NULL                 ← FK para editora (não implementado)
├── source          VARCHAR(255) NOT NULL       ← "GUTENDEX" ou "LOCAL"

-- Autores (separados por migração V002)
authors
├── id              BIGINT PK AUTO_INCREMENT
├── name            VARCHAR(255) NOT NULL
├── birth_year      INT NULL
├── death_year      INT NULL

-- Relacionamento N:N livros ↔ autores
book_authors
├── book_id         BIGINT NOT NULL FK → books.id
├── author_id       BIGINT NOT NULL FK → authors.id
├── PRIMARY KEY (book_id, author_id)

-- Usuários
users
├── id              BIGINT PK AUTO_INCREMENT
├── username        VARCHAR(255) UNIQUE NOT NULL
├── first_name      VARCHAR(255) NOT NULL
├── last_name       VARCHAR(255) NOT NULL
├── email           VARCHAR(255) UNIQUE NOT NULL
├── password        VARCHAR(255) NOT NULL        ← BCrypt hash
├── created_at      DATETIME

-- Coleção de livros do usuário
user_books
├── id              BIGINT PK AUTO_INCREMENT
├── user_id         BIGINT NOT NULL FK → users.id
├── book_id         BIGINT NOT NULL FK → books.id
├── status          VARCHAR(20) NOT NULL DEFAULT 'toread'  ← toread|reading|done
├── progress        INT NULL                              ← 0–100 (para READING)
├── rating          INT NULL                              ← 0–5 (para DONE)
├── created_at      DATETIME
├── UNIQUE (user_id, book_id)
```

#### API Externa (Gutendex)

```
BookApiClientImpl
    → GutendexClient (RestTemplate)
        → GET https://gutendex.com/books?languages=pt&page={n}
        → GET https://gutendex.com/books?search={query}&languages=pt
    → GutendexMapper (converte GutendexBookResponse → BookData)
    → AuthorData.getFormattedName() (formata "Sobrenome, Nome" → "Nome Sobrenome")
```

O `CreateBookUseCase` chama `bookApiClient.getPage(page)` para importar uma página de livros (32 livros por página, apenas em português). Cada livro é verificado por `gutendexId` duplicado antes de salvar.

---

## 🔐 Autenticação e Segurança

### Fluxo de autenticação:

```
1. POST /auth/register  →  Cria usuário (senha com BCrypt)
2. POST /auth/login     →  Valida credenciais, retorna token JWT
3. Headers nas requests autenticadas:
   Authorization: Bearer <token>
```

### Componentes de segurança:

| Componente | Função |
|------------|--------|
| `SecurityConfig` | Configura SecurityFilterChain, CORS, session stateless, rotas públicas vs autenticadas |
| `JwtTokenProvider` | Gera tokens JWT com claims (userId, username, issuedAt, expiration) e valida |
| `JwtAuthenticationFilter` | Filtro OncePerRequestFilter: extrai token do header, valida, seta SecurityContext |
| `JwtAuthenticationEntryPoint` | Retorna 401 para requests não autenticadas |
| `UserDetailsServiceImpl` | Carrega UserDetails do banco para o Spring Security |
| `CorsConfig` | Permite origens configuradas via `cors.allowed-origins` (padrão: localhost:3000) |

### Propriedades JWT:

| Propriedade | Descrição | Padrão |
|-------------|-----------|--------|
| `jwt.secret` | Chave secreta HMAC-SHA (min 256 bits) | `dev-secret-key-not-for-production-min-256bits` |
| `jwt.expiration-ms` | Tempo de expiração do token | `86400000` (24h) |

---

## 📜 Migrações Flyway

**Nota:** Flyway está **desabilitado** no perfil padrão (`spring.flyway.enabled=false`) e **habilitado** apenas no perfil `rds` (produção).

| Migração | Descrição |
|----------|-----------|
| `V001__Add_Gutendex_Fields_And_Remove_Genre.sql` | Adiciona colunas Gutendex (`gutendex_id`, `download_url`, `cover_url`, `languages`, `subjects`, `download_count`), remove coluna `genre`, modifica `publisher_id` para nullable |
| `V002__Create_Authors_And_BookAuthors.sql` | Cria tabelas `authors` e `book_authors`, migra dados da coluna `author` (antiga) para a nova estrutura, remove coluna `author` |

> **Importante:** As tabelas iniciais (`books`, `users`, `user_books`) são criadas automaticamente pelo Hibernate com `spring.jpa.hibernate.ddl-auto=update`. As migrações Flyway apenas fazem alterações incrementais.

---

## 🔧 Configuração (BeanConfiguration)

Todos os Use Cases e dependências são declarados como `@Bean` em `BeanConfiguration.java`:

```java
@Configuration
public class BeanConfiguration {

  @Bean
  public RestTemplate restTemplate() { return new RestTemplate(); }

  @Bean
  public CreateBookUseCase createBookUseCase(
      BookRepository bookRepository,
      AuthorRepository authorRepository,
      BookApiClient bookApiClient) {
    return new CreateBookUseCase(bookRepository, authorRepository, bookApiClient);
  }

  @Bean
  public RegisterUserUseCase registerUserUseCase(UserRepository userRepository) {
    return new RegisterUserUseCase(userRepository);
  }

  @Bean
  public AuthenticateUserUseCase authenticateUserUseCase(
      UserRepository userRepository, PasswordEncoder passwordEncoder) {
    return new AuthenticateUserUseCase(userRepository, passwordEncoder);
  }
  // ... demais Use Cases
}
```

Isso mantém a camada de aplicação **pura** (sem anotações Spring) e facilita testes unitários.

---

## 🌐 Perfis de Configuração

### Profile padrão (desenvolvimento local)
**Arquivo:** `application.properties`

| Propriedade | Valor |
|-------------|-------|
| Banco | `jdbc:mysql://localhost:3306/alexandriadb` |
| Usuário | `root` (ou `DB_USER` env) |
| Senha | `root` (ou `DB_PASSWORD` env) |
| JPA DDL | `update` |
| Flyway | `false` |
| JWT Secret | `dev-secret-key-not-for-production-min-256bits` |

### Profile `rds` (produção — usado no Dockerfile)
**Arquivo:** `application-rds.properties`

| Propriedade | Valor |
|-------------|-------|
| Banco | MySQL via SSL (variáveis de ambiente) |
| JPA DDL | `update` |
| Flyway | `true` |
| JWT Secret | Via variável de ambiente `JWT_SECRET` |
| Monitoramento | Actuator endpoints (health, info) |
| CORS | Configurável via `CORS_ALLOWED_ORIGINS` |
| Swagger | `/swagger-ui.html`, `/api-docs` |

**Dockerfile** inicia com `--spring.profiles.active=rds`.

---

## 🛡️ Tratamento de Erros

`GlobalExceptionHandler` (anotado com `@RestControllerAdvice`) captura exceções e retorna respostas padronizadas:

```json
{
  "message": "Book not found with id: 999",
  "status": 404
}
```

| Exceção | HTTP Status | Quando ocorre |
|---------|-------------|---------------|
| `BookNotFoundException` | 404 | Livro não encontrado |
| `UserBooksNotFoundException` | 404 | Relação user-book não encontrada |
| `InvalidUserBooksException` | 400 | Violação de regra de UserBooks (ex: progress + TOREAD) |
| `IllegalArgumentException` | 400 | Argumento inválido |
| `DuplicateUserBooksException` | 409 | Livro já adicionado à coleção |
| `DuplicateUserException` | 409 | Username ou email já cadastrado |
| `InvalidCredentialsException` | 401 | Credenciais inválidas (login) |
| `Exception` (genérica) | 500 | Erro interno não tratado |

---

## 🧪 Testes

### Estrutura de testes:

```
src/test/java/
└── com/pucsp/alexandria/
    ├── AlexandriaApplicationTests.java
    ├── domain/            # Testes unitários de entidades e VOs
    │   ├── book/          # BookTest, BookIdTest, BookSourceTest
    │   ├── author/        # AuthorTest, AuthorIdTest
    │   ├── user/          # UserTest, UserIdTest
    │   ├── userbook/      # UserBooksTest, UserBooksStatusTest
    │   └── shared/valueobject/ # EmailTest
    ├── application/       # Testes de Use Cases (Mockito)
    │   ├── book/          # CreateBookUseCaseTest, GetBookUseCaseTest, ...
    │   ├── auth/          # RegisterUserUseCaseTest, AuthenticateUserUseCaseTest
    │   └── userbooks/     # AddUserBooksUseCaseTest, ListUserBooksUseCaseTest, ...
    ├── adapter/
    │   └── out/persistence/
    │       ├── jpa/       # Testes de JPA Repository (Testcontainers)
    │       ├── entity/    # Testes de entidades JPA
    │       ├── mapper/    # Testes de mappers
    │       └── external/  # Testes de clientes externos (Gutendex)
    ├── adapter/in/rest/   # Testes de integração dos controllers
    └── config/jwt/        # Testes de JWT
```

### Ferramentas:
- **JUnit 5 + Mockito** — Testes unitários
- **H2** — Banco em memória para testes de repositório
- **Testcontainers (MySQL)** — Testes de integração com banco real
- **JaCoCo** — Relatório de cobertura (executado no `prepare-package`)

---

## 🚀 Deploy e Execução

### Docker
```bash
# Construir imagem
docker build -t alexandria-api .

# Executar com Docker Compose (MySQL local)
docker-compose up -d
```

### Manual
```bash
# Pré-requisitos: MySQL rodando na porta 3306, schema alexandriadb
# Credenciais: root/root (ou variáveis de ambiente DB_USER/DB_PASSWORD)

./mvnw spring-boot:run

# Com profile RDS:
./mvnw spring-boot:run -Dspring-boot.run.profiles=rds
```

### Variáveis de Ambiente

| Variável | Obrigatória | Padrão | Descrição |
|----------|-------------|--------|-----------|
| `DB_HOST` | Não | `localhost` | Host do MySQL |
| `DB_PORT` | Não | `3306` | Porta do MySQL |
| `DB_NAME` | Não | `alexandriadb` | Nome do database |
| `DB_USER` | Não (sim p/ RDS) | `root` | Usuário do MySQL |
| `DB_PASSWORD` | Não (sim p/ RDS) | `root` | Senha do MySQL |
| `JWT_SECRET` | Não (sim p/ RDS) | `dev-secret-key-...` | Chave secreta JWT (min 256 bits) |
| `JWT_EXPIRATION_MS` | Não | `86400000` | Expiração do token (24h) |
| `CORS_ALLOWED_ORIGINS` | Não | `http://localhost:3000` | Origens permitidas CORS |

---

## 🧠 Decisões Técnicas

### Por que não usar `@Service` nos Use Cases?
Para manter a camada de aplicação **pura** (POJO), sem dependência de frameworks. Os Use Cases são registrados como `@Bean` em `BeanConfiguration`, facilitando testes unitários.

### Por que usar `ddl-auto=update` junto com Flyway?
O `ddl-auto=update` do Hibernate garante que as tabelas existam (inclusive durante desenvolvimento). As migrações Flyway são usadas apenas para mudanças estruturais controladas em produção, com validação de integridade.

### Por que o UserId é passado como `Long` no Authentication?
O `JwtAuthenticationFilter` seta o `userId` como principal (`authentication.setPrincipal(userId)`), e os controllers fazem cast para `Long` para obter o ID do usuário autenticado.

### Por que a Gutendex busca apenas livros em português?
O parâmetro `languages=pt` é fixo no `GutendexClient`, definido como constante `SEARCH_BOOKS_DEFAULT_LANGUAGE = "pt"`, pois o foco da aplicação é o público brasileiro.

---

## 📚 Documentação da API

Com a aplicação rodando, acesse:
- **Swagger UI:** `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON:** `http://localhost:8080/api-docs`
- **Health Check:** `http://localhost:8080/actuator/health`
