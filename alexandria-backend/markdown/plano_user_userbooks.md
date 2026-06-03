# 📋 Plano de Implementação: UserBooks (Relacionamento Usuário-Livro)

Baseado no contrato de API e na arquitetura atual do projeto (`ARCHITECTURE.md`), seguindo os guidelines do `HOW_TO_ADD_NEW_DOMAIN.md`.

---

## 🧩 Premissas

- **UserBooks** opera **apenas no banco de dados MySQL** — nunca consulta a Gutendex.
- Se um livro não existir no banco ao tentar adicionar à biblioteca, lança exceção (o livro deve ser salvo primeiro por outro fluxo).
- O `userId` vem **do token JWT** (autenticado via Spring Security).
- O domínio **User** (`domain/user/User.java`) e **UserId** (`domain/user/UserId.java`) **já existem** — não precisam ser criados.
- O domínio **UserBooks** (`domain/userbook/UserBooks.java`) **já existe parcialmente** — será reformulado seguindo os padrões.

---

## 📊 Checklist por Camada

```
Novo Domínio: UserBooks (Relacionamento Usuário-Livro)
│
├── 🧬 DOMÍNIO (7 arquivos)
│   ├── domain/userbook/UserBooks.java           ← REFORMULAR (padrão HOW_TO + UserId)
│   ├── domain/userbook/UserBooksStatus.java     ← CRIAR (toread, reading, done)
│   ├── domain/userbook/UserBooksRepository.java ← CRIAR (porta de saída)
│   └── exception/
│       ├── InvalidUserBooksException.java       ← CRIAR (400)
│       ├── UserBooksNotFoundException.java      ← CRIAR (404)
│       └── DuplicateUserBooksException.java     ← CRIAR (409)
│   ├── domain/user/UserRepository.java          ← CRIAR (porta de saída)
│
├── ⚙️ APLICAÇÃO (11 arquivos)
│   ├── application/userbooks/
│   │   ├── AddUserBooksUseCase.java             ← CRIAR
│   │   ├── ListUserBooksUseCase.java
│   │   ├── UpdateUserBooksUseCase.java
│   │   ├── RemoveUserBooksUseCase.java
│   │   └── dto/
│   │       ├── UserBooksOutput.java             ← CRIAR (BookOutput aninhado)
│   │       ├── AddUserBooksInput.java
│   │       └── UpdateUserBooksInput.java
│   │
│   └── application/auth/                        ← NOVO (autenticação)
│       ├── RegisterUserUseCase.java
│       ├── AuthenticateUserUseCase.java
│       └── dto/
│           ├── RegisterInput.java
│           ├── RegisterOutput.java
│           ├── AuthInput.java
│           └── AuthOutput.java
│
├── 🏗️ INFRAESTRUTURA - Adapters (13 arquivos)
│   ├── adapter/in/rest/
│   │   ├── UserBooksController.java             ← CRIAR
│   │   └── dto/
│   │       ├── UserBooksResponse.java           ← CRIAR (BookSummaryResponse aninhado)
│   │       ├── BookSummaryResponse.java         ← CRIAR (versão resumida do BookResponse)
│   │       ├── AddUserBooksRequest.java
│   │       └── UpdateUserBooksRequest.java
│   │
│   ├── adapter/in/rest/auth/                    ← NOVO
│   │   ├── AuthController.java
│   │   └── dto/
│   │       ├── RegisterRequest.java
│   │       ├── RegisterResponse.java
│   │       ├── AuthRequest.java
│   │       └── AuthResponse.java
│   │
│   └── adapter/out/persistence/
│       ├── UserBooksRepositoryImpl.java         ← CRIAR
│       ├── entity/UserBooksEntity.java          ← REFORMULAR
│       ├── jpa/UserBooksJpaRepository.java      ← CRIAR
│       ├── mapper/UserBooksMapper.java          ← CRIAR
│       ├── UserRepositoryImpl.java              ← CRIAR
│       ├── jpa/UserJpaRepository.java           ← CRIAR
│       └── mapper/UserMapper.java               ← CRIAR
│
├── 🔐 SEGURANÇA / JWT (5 arquivos)
│   ├── config/SecurityConfig.java               ← MODIFICAR (adicionar JWT)
│   ├── config/jwt/
│   │   ├── JwtTokenProvider.java                ← CRIAR
│   │   ├── JwtAuthenticationFilter.java         ← CRIAR
│   │   └── JwtAuthenticationEntryPoint.java     ← CRIAR
│   └── config/UserDetailsServiceImpl.java       ← CRIAR
│
├── ⚙️ CONFIGURAÇÃO (1 modificação)
│   └── config/BeanConfiguration.java            ← MODIFICAR (adicionar @Bean)
│
├── 🛡️ EXCEÇÕES (1 modificação)
│   └── advice/GlobalExceptionHandler.java       ← MODIFICAR
│
├── 🌐 CORS (1 criação)
│   └── config/CorsConfig.java                   ← CRIAR
│
└── 📦 DEPENDÊNCIAS
    └── pom.xml                                  ← MODIFICAR (spring-security + jjwt)
```

**Total: ~33 arquivos criados + 4 modificados + 2 removidos**

---

## 🔨 Passo 1 — Dependency: Atualizar `pom.xml`

Adicionar dependências de **Spring Security** e **JWT**:

```xml
<!-- Spring Security -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<!-- JWT (jjwt) -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.6</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>
```

---

## 🔨 Passo 2 — Camada de Domínio

### 2.1 Value Object: `UserId.java` ✅ JÁ EXISTE

**Pacote:** `com.pucsp.alexandria.domain.user.UserId`

Já implementado em `domain/user/UserId.java`. Nenhuma alteração necessária.

### 2.2 Entity: `User.java` ✅ JÁ EXISTE

**Pacote:** `com.pucsp.alexandria.domain.user.User`

Já implementado em `domain/user/User.java`. **Nenhuma alteração necessária.**

### 2.3 Entity: `UserBooks.java` (REFORMULAR)

**Pacote:** `com.pucsp.alexandria.domain.userbook`

**Mudanças em relação ao existente:**
- Renomear `BookStatus` → `UserBooksStatus` (com valores TOREAD, READING, DONE)
- Usar `UserId` (Value Object) em vez de `Long userId`
- Adicionar regras de validação por status
- Seguir padrão `create()` / `restore()` do HOW_TO_ADD_NEW_DOMAIN.md

```java
package com.pucsp.alexandria.domain.userbook;

import com.pucsp.alexandria.domain.user.UserId;
import com.pucsp.alexandria.domain.userbook.exception.InvalidUserBooksException;
import java.time.LocalDateTime;
import java.util.Objects;

public class UserBooks {

    private final Long id;
    private final UserId userId;
    private final Long bookId;
    private final UserBooksStatus status;
    private final Integer progress;
    private final Integer rating;
    private final LocalDateTime createdAt;

    private UserBooks(Long id, UserId userId, Long bookId, UserBooksStatus status,
                      Integer progress, Integer rating, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.bookId = bookId;
        this.status = status;
        this.progress = progress;
        this.rating = rating;
        this.createdAt = createdAt;
    }

    // Factory method para criar novo vínculo
    public static UserBooks create(UserId userId, Long bookId, UserBooksStatus status) {
        Objects.requireNonNull(userId, "userId is required");
        Objects.requireNonNull(bookId, "bookId is required");
        UserBooksStatus finalStatus = status != null ? status : UserBooksStatus.TOREAD;
        validateByStatus(finalStatus, null, null);
        return new UserBooks(null, userId, bookId, finalStatus, null, null, LocalDateTime.now());
    }

    // Factory method para restaurar do BD
    public static UserBooks restore(Long id, Long userId, Long bookId, String status,
                                    Integer progress, Integer rating, LocalDateTime createdAt) {
        UserId userIdVO = UserId.from(userId);
        UserBooksStatus statusEnum = UserBooksStatus.fromString(status);
        validateByStatus(statusEnum, progress, rating);
        return new UserBooks(id, userIdVO, bookId, statusEnum, progress, rating, createdAt);
    }

    // Atualizar (cria nova instância com dados atualizados)
    public UserBooks updateWith(UserBooksStatus newStatus, Integer newProgress, Integer newRating) {
        UserBooksStatus finalStatus = newStatus != null ? newStatus : this.status;
        Integer finalProgress = newProgress != null ? newProgress : this.progress;
        Integer finalRating = newRating != null ? newRating : this.rating;
        validateByStatus(finalStatus, finalProgress, finalRating);
        return new UserBooks(this.id, this.userId, this.bookId, finalStatus, finalProgress, finalRating, this.createdAt);
    }

    // Regra de negócio: validações por status
    private static void validateByStatus(UserBooksStatus status, Integer progress, Integer rating) {
        switch (status) {
            case TOREAD:
                if (progress != null) throw new InvalidUserBooksException("Progress must be null for TOREAD status");
                if (rating != null) throw new InvalidUserBooksException("Rating must be null for TOREAD status");
                break;
            case READING:
                if (progress == null || progress < 0 || progress > 100)
                    throw new InvalidUserBooksException("Progress (0-100) is required for READING status");
                if (rating != null)
                    throw new InvalidUserBooksException("Rating must be null for READING status");
                break;
            case DONE:
                if (progress != null)
                    throw new InvalidUserBooksException("Progress must be null for DONE status");
                if (rating == null || rating < 0 || rating > 5)
                    throw new InvalidUserBooksException("Rating (0-5) is required for DONE status");
                break;
        }
    }

    // Getters
    public Long getId() { return id; }
    public UserId getUserId() { return userId; }
    public Long getBookId() { return bookId; }
    public UserBooksStatus getStatus() { return status; }
    public Integer getProgress() { return progress; }
    public Integer getRating() { return rating; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        UserBooks that = (UserBooks) obj;
        return id != null ? id.equals(that.id) : that.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
```

### 2.4 Enum: `UserBooksStatus.java`

**Pacote:** `com.pucsp.alexandria.domain.userbook`

```java
package com.pucsp.alexandria.domain.userbook;

public enum UserBooksStatus {
    TOREAD("toread"),
    READING("reading"),
    DONE("done");

    private final String value;

    UserBooksStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static UserBooksStatus fromString(String value) {
        for (UserBooksStatus status : UserBooksStatus.values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid UserBooksStatus: " + value);
    }
}
```

### 2.5 Porta de Saída: `UserBooksRepository.java`

**Pacote:** `com.pucsp.alexandria.domain.userbook`

```java
package com.pucsp.alexandria.domain.userbook;

import com.pucsp.alexandria.domain.user.UserId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

public interface UserBooksRepository {

    UserBooks save(UserBooks userBooks);

    Optional<UserBooks> findById(Long id);

    Page<UserBooks> findByUserId(UserId userId, Pageable pageable);

    Page<UserBooks> findByUserIdAndStatus(UserId userId, UserBooksStatus status, Pageable pageable);

    Optional<UserBooks> findByUserIdAndBookId(UserId userId, Long bookId);

    boolean existsByUserIdAndBookId(UserId userId, Long bookId);

    void delete(UserBooks userBooks);
}
```

### 2.6 Exceções

**Pacote:** `com.pucsp.alexandria.domain.userbook.exception`

```java
// InvalidUserBooksException.java — validações de negócio (status 400)
package com.pucsp.alexandria.domain.userbook.exception;

import com.pucsp.alexandria.domain.shared.exception.DomainException;

public class InvalidUserBooksException extends DomainException {

    public InvalidUserBooksException(String message) {
        super(message);
    }
}
```

```java
// UserBooksNotFoundException.java — recurso não encontrado (status 404)
package com.pucsp.alexandria.domain.userbook.exception;

import com.pucsp.alexandria.domain.shared.exception.DomainException;

public class UserBooksNotFoundException extends DomainException {

    public UserBooksNotFoundException(String message) {
        super(message);
    }
}
```

```java
// DuplicateUserBooksException.java — livro já na biblioteca (status 409)
package com.pucsp.alexandria.domain.userbook.exception;

import com.pucsp.alexandria.domain.shared.exception.DomainException;

public class DuplicateUserBooksException extends DomainException {

    public DuplicateUserBooksException(String message) {
        super(message);
    }
}
```

---

## 🔨 Passo 3 — Camada de Aplicação

### 3.1 DTOs de Entrada/Saída — UserBooks

```java
// UserBooksOutput.java
package com.pucsp.alexandria.application.userbooks.dto;

import com.pucsp.alexandria.application.book.dto.BookOutput;
import com.pucsp.alexandria.domain.userbook.UserBooks;

/**
 * DTO de saída.
 * O book é resolvido no Use Case e incluído aqui.
 * O adapter (controller) converte para UserBooksResponse.
 */
public record UserBooksOutput(
    Long id,
    BookOutput book,
    String status,
    Integer progress,
    Integer rating
) {
    public static UserBooksOutput from(UserBooks userBooks, BookOutput bookOutput) {
        return new UserBooksOutput(
            userBooks.getId(),
            bookOutput,
            userBooks.getStatus().getValue(),
            userBooks.getProgress(),
            userBooks.getRating()
        );
    }
}
```

```java
// AddUserBooksInput.java
package com.pucsp.alexandria.application.userbooks.dto;

public record AddUserBooksInput(
    Long bookId,
    String status
) {}
```

```java
// UpdateUserBooksInput.java
package com.pucsp.alexandria.application.userbooks.dto;

public record UpdateUserBooksInput(
    String status,
    Integer progress,
    Integer rating
) {}
```

### 3.2 Use Cases — UserBooks

#### AddUserBooksUseCase

```java
package com.pucsp.alexandria.application.userbooks;

import com.pucsp.alexandria.application.book.dto.BookOutput;
import com.pucsp.alexandria.application.userbooks.dto.AddUserBooksInput;
import com.pucsp.alexandria.application.userbooks.dto.UserBooksOutput;
import com.pucsp.alexandria.domain.book.Book;
import com.pucsp.alexandria.domain.book.BookRepository;
import com.pucsp.alexandria.domain.book.exception.BookNotFoundException;
import com.pucsp.alexandria.domain.user.UserId;
import com.pucsp.alexandria.domain.userbook.UserBooks;
import com.pucsp.alexandria.domain.userbook.UserBooksRepository;
import com.pucsp.alexandria.domain.userbook.UserBooksStatus;
import com.pucsp.alexandria.domain.userbook.exception.DuplicateUserBooksException;
import com.pucsp.alexandria.domain.userbook.exception.InvalidUserBooksException;

public class AddUserBooksUseCase {

    private final UserBooksRepository userBooksRepository;
    private final BookRepository bookRepository;

    public AddUserBooksUseCase(UserBooksRepository userBooksRepository, BookRepository bookRepository) {
        this.userBooksRepository = userBooksRepository;
        this.bookRepository = bookRepository;
    }

    public UserBooksOutput execute(Long userId, AddUserBooksInput input) {
        // 1. Valida bookId obrigatório
        if (input.bookId() == null) {
            throw new InvalidUserBooksException("bookId é obrigatório");
        }

        UserId userIdVO = UserId.from(userId);

        // 2. Verifica se o livro existe no MySQL
        var book = bookRepository.findById(input.bookId())
            .orElseThrow(() -> new BookNotFoundException(
                "Livro não encontrado com id: " + input.bookId()));

        Long bookId = book.getId().getValue();

        // 3. Verifica duplicidade (409 Conflict)
        if (userBooksRepository.existsByUserIdAndBookId(userIdVO, bookId)) {
            throw new DuplicateUserBooksException("Livro já está na biblioteca do usuário");
        }

        // 4. Cria o vínculo
        UserBooksStatus status = input.status() != null
            ? UserBooksStatus.fromString(input.status())
            : UserBooksStatus.TOREAD;

        UserBooks userBooks = UserBooks.create(userIdVO, bookId, status);
        UserBooks saved = userBooksRepository.save(userBooks);

        // 5. Retorna com dados do book embutidos
        BookOutput bookOutput = BookOutput.from(book);
        return UserBooksOutput.from(saved, bookOutput);
    }
}
```

#### ListUserBooksUseCase

```java
package com.pucsp.alexandria.application.userbooks;

import com.pucsp.alexandria.application.book.dto.BookOutput;
import com.pucsp.alexandria.application.userbooks.dto.UserBooksOutput;
import com.pucsp.alexandria.domain.book.Book;
import com.pucsp.alexandria.domain.book.BookRepository;
import com.pucsp.alexandria.domain.user.UserId;
import com.pucsp.alexandria.domain.userbook.UserBooks;
import com.pucsp.alexandria.domain.userbook.UserBooksRepository;
import com.pucsp.alexandria.domain.userbook.UserBooksStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public class ListUserBooksUseCase {

    private final UserBooksRepository userBooksRepository;
    private final BookRepository bookRepository;

    public ListUserBooksUseCase(UserBooksRepository userBooksRepository, BookRepository bookRepository) {
        this.userBooksRepository = userBooksRepository;
        this.bookRepository = bookRepository;
    }

    public Page<UserBooksOutput> execute(Long userId, String status, Pageable pageable) {
        UserId userIdVO = UserId.from(userId);

        Page<UserBooks> userBooksPage;
        if (status != null && !status.isBlank()) {
            UserBooksStatus statusEnum = UserBooksStatus.fromString(status);
            userBooksPage = userBooksRepository.findByUserIdAndStatus(userIdVO, statusEnum, pageable);
        } else {
            userBooksPage = userBooksRepository.findByUserId(userIdVO, pageable);
        }

        return userBooksPage.map(ub -> {
            Book book = bookRepository.findById(ub.getBookId())
                .orElseThrow(() -> new RuntimeException("Book not found for user book: " + ub.getId()));
            return UserBooksOutput.from(ub, BookOutput.from(book));
        });
    }
}
```

#### UpdateUserBooksUseCase

```java
package com.pucsp.alexandria.application.userbooks;

import com.pucsp.alexandria.application.book.dto.BookOutput;
import com.pucsp.alexandria.application.userbooks.dto.UpdateUserBooksInput;
import com.pucsp.alexandria.application.userbooks.dto.UserBooksOutput;
import com.pucsp.alexandria.domain.book.Book;
import com.pucsp.alexandria.domain.book.BookRepository;
import com.pucsp.alexandria.domain.book.exception.BookNotFoundException;
import com.pucsp.alexandria.domain.user.UserId;
import com.pucsp.alexandria.domain.userbook.UserBooks;
import com.pucsp.alexandria.domain.userbook.UserBooksRepository;
import com.pucsp.alexandria.domain.userbook.UserBooksStatus;
import com.pucsp.alexandria.domain.userbook.exception.UserBooksNotFoundException;

public class UpdateUserBooksUseCase {

    private final UserBooksRepository userBooksRepository;
    private final BookRepository bookRepository;

    public UpdateUserBooksUseCase(UserBooksRepository userBooksRepository, BookRepository bookRepository) {
        this.userBooksRepository = userBooksRepository;
        this.bookRepository = bookRepository;
    }

    public UserBooksOutput execute(Long userId, Long id, UpdateUserBooksInput input) {
        UserBooks userBooks = userBooksRepository.findById(id)
            .orElseThrow(() -> new UserBooksNotFoundException("UserBook não encontrado com id: " + id));

        // Valida que o UserBook pertence ao usuário autenticado
        if (!userBooks.getUserId().getValue().equals(userId)) {
            throw new UserBooksNotFoundException("UserBook não encontrado com id: " + id);
        }

        UserBooksStatus newStatus = input.status() != null
            ? UserBooksStatus.fromString(input.status()) : null;

        UserBooks updated = userBooks.updateWith(newStatus, input.progress(), input.rating());
        UserBooks saved = userBooksRepository.save(updated);

        Book book = bookRepository.findById(saved.getBookId())
            .orElseThrow(() -> new BookNotFoundException("Book not found with id: " + saved.getBookId()));

        return UserBooksOutput.from(saved, BookOutput.from(book));
    }
}
```

#### RemoveUserBooksUseCase

```java
package com.pucsp.alexandria.application.userbooks;

import com.pucsp.alexandria.domain.userbook.UserBooks;
import com.pucsp.alexandria.domain.userbook.UserBooksRepository;
import com.pucsp.alexandria.domain.userbook.exception.UserBooksNotFoundException;

public class RemoveUserBooksUseCase {

    private final UserBooksRepository userBooksRepository;

    public RemoveUserBooksUseCase(UserBooksRepository userBooksRepository) {
        this.userBooksRepository = userBooksRepository;
    }

    public void execute(Long userId, Long id) {
        UserBooks userBooks = userBooksRepository.findById(id)
            .orElseThrow(() -> new UserBooksNotFoundException("UserBook não encontrado com id: " + id));

        // Valida que o UserBook pertence ao usuário autenticado
        if (!userBooks.getUserId().getValue().equals(userId)) {
            throw new UserBooksNotFoundException("UserBook não encontrado com id: " + id);
        }

        userBooksRepository.delete(userBooks);
    }
}
```

### 3.3 DTOs e Use Cases — Autenticação (Auth)

```java
// RegisterInput.java
package com.pucsp.alexandria.application.auth.dto;

public record RegisterInput(
    String username,
    String firstName,
    String lastName,
    String email,
    String password
) {}
```

```java
// RegisterOutput.java
package com.pucsp.alexandria.application.auth.dto;

public record RegisterOutput(
    Long id,
    String username,
    String email
) {}
```

```java
// AuthInput.java
package com.pucsp.alexandria.application.auth.dto;

public record AuthInput(
    String username,
    String password
) {}
```

```java
// AuthOutput.java
package com.pucsp.alexandria.application.auth.dto;

public record AuthOutput(
    String token,
    String type,
    Long userId,
    String username
) {
    public static AuthOutput of(String token, Long userId, String username) {
        return new AuthOutput(token, "Bearer", userId, username);
    }
}
```

```java
// RegisterUserUseCase.java
package com.pucsp.alexandria.application.auth;

import com.pucsp.alexandria.application.auth.dto.RegisterInput;
import com.pucsp.alexandria.application.auth.dto.RegisterOutput;
import com.pucsp.alexandria.domain.user.User;
import com.pucsp.alexandria.domain.user.UserRepository;

public class RegisterUserUseCase {

    private final UserRepository userRepository;

    public RegisterUserUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public RegisterOutput execute(RegisterInput input) {
        User user = User.create(
            input.username(),
            input.firstName(),
            input.lastName(),
            input.email(),
            input.password()
        );
        User saved = userRepository.save(user);
        return new RegisterOutput(
            saved.getId().getValue(),
            saved.getUsername(),
            saved.getEmail().getValue()
        );
    }
}
```

```java
// AuthenticateUserUseCase.java
package com.pucsp.alexandria.application.auth;

import com.pucsp.alexandria.application.auth.dto.AuthInput;
import com.pucsp.alexandria.application.auth.dto.AuthOutput;
import com.pucsp.alexandria.domain.user.User;
import com.pucsp.alexandria.domain.user.UserRepository;

public class AuthenticateUserUseCase {

    private final UserRepository userRepository;

    public AuthenticateUserUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public AuthOutput execute(AuthInput input) {
        User user = userRepository.findByUsername(input.username())
            .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));

        // A validação da senha já foi feita pelo Spring Security (AuthenticationManager)
        // Este use case apenas busca os dados do usuário para gerar o token
        return AuthOutput.of(null, user.getId().getValue(), user.getUsername());
    }
}
```

---

## 🔨 Passo 4 — Segurança: Spring Security com JWT

### 4.1 `JwtTokenProvider.java`

**Pacote:** `com.pucsp.alexandria.config.jwt`

Responsabilidades:
- Gerar token JWT com claims: `userId`, `username`, `issuedAt`, `expiration`
- Validar token JWT (assinatura, expiração)
- Extrair `userId` e `username` do token
- Usar chave secreta configurada em `application.properties`

```java
package com.pucsp.alexandria.config.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long expirationMs;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-ms}") long expirationMs) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    public String generateToken(Long userId, String username) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
            .subject(username)
            .claim("userId", userId)
            .issuedAt(now)
            .expiration(expiry)
            .signWith(secretKey)
            .compact();
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();
        return claims.get("userId", Long.class);
    }

    public String getUsernameFromToken(String token) {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .getPayload()
            .getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
```

### 4.2 `JwtAuthenticationFilter.java`

**Pacote:** `com.pucsp.alexandria.config.jwt`

Filtro que intercepta requisições HTTP, extrai o token JWT do header `Authorization: Bearer <token>`, valida e configura o `SecurityContextHolder`.

```java
package com.pucsp.alexandria.config.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            if (jwtTokenProvider.validateToken(token)) {
                String username = jwtTokenProvider.getUsernameFromToken(token);
                Long userId = jwtTokenProvider.getUserIdFromToken(token);

                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                        userId, null, Collections.emptyList());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }
}
```

### 4.3 `JwtAuthenticationEntryPoint.java`

**Pacote:** `com.pucsp.alexandria.config.jwt`

```java
package com.pucsp.alexandria.config.jwt;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized: Invalid or expired token");
    }
}
```

### 4.4 `UserDetailsServiceImpl.java`

**Pacote:** `com.pucsp.alexandria.config`

Implementa `UserDetailsService` do Spring Security para carregar usuários do banco.

```java
package com.pucsp.alexandria.config;

import com.pucsp.alexandria.domain.user.User;
import com.pucsp.alexandria.domain.user.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return new org.springframework.security.core.userdetails.User(
            user.getUsername(),
            user.getPassword(),
            Collections.emptyList()
        );
    }
}
```

### 4.5 `SecurityConfig.java` (MODIFICAR)

**Pacote:** `com.pucsp.alexandria.config`

```java
package com.pucsp.alexandria.config;

import com.pucsp.alexandria.config.jwt.JwtAuthenticationEntryPoint;
import com.pucsp.alexandria.config.jwt.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .exceptionHandling(ex -> ex.authenticationEntryPoint(jwtAuthenticationEntryPoint))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/**").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/error").permitAll()
                .requestMatchers("/books/search").permitAll()
                .requestMatchers("/books").permitAll()
                .requestMatchers("/books/{id}").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
```

**Observação sobre rotas públicas:** Ajustar os `requestMatchers` conforme necessário. Por enquanto, os endpoints de **books** permanecem públicos e os de **user-books** e **auth** exigem autenticação (exceto `/auth/register` e `/auth/login`).

---

## 🔨 Passo 5 — Adapter de Entrada (Controllers REST)

### 5.1 `AuthController.java`

**Pacote:** `com.pucsp.alexandria.adapter.in.rest.auth`

```java
package com.pucsp.alexandria.adapter.in.rest.auth;

import com.pucsp.alexandria.adapter.in.rest.auth.dto.AuthRequest;
import com.pucsp.alexandria.adapter.in.rest.auth.dto.AuthResponse;
import com.pucsp.alexandria.adapter.in.rest.auth.dto.RegisterRequest;
import com.pucsp.alexandria.adapter.in.rest.auth.dto.RegisterResponse;
import com.pucsp.alexandria.application.auth.AuthenticateUserUseCase;
import com.pucsp.alexandria.application.auth.RegisterUserUseCase;
import com.pucsp.alexandria.application.auth.dto.AuthInput;
import com.pucsp.alexandria.application.auth.dto.RegisterInput;
import com.pucsp.alexandria.config.jwt.JwtTokenProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final RegisterUserUseCase registerUserUseCase;
    private final AuthenticateUserUseCase authenticateUserUseCase;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthController(RegisterUserUseCase registerUserUseCase,
                          AuthenticateUserUseCase authenticateUserUseCase,
                          AuthenticationManager authenticationManager,
                          PasswordEncoder passwordEncoder,
                          JwtTokenProvider jwtTokenProvider) {
        this.registerUserUseCase = registerUserUseCase;
        this.authenticateUserUseCase = authenticateUserUseCase;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@RequestBody RegisterRequest request) {
        var input = new RegisterInput(
            request.username(), request.firstName(), request.lastName(),
            request.email(), passwordEncoder.encode(request.password())
        );
        var output = registerUserUseCase.execute(input);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(RegisterResponse.from(output));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        var input = new AuthInput(request.username(), request.password());
        var userOutput = authenticateUserUseCase.execute(input);

        String token = jwtTokenProvider.generateToken(userOutput.userId(), userOutput.username());

        return ResponseEntity.ok(AuthResponse.from(
            new AuthOutput(token, "Bearer", userOutput.userId(), userOutput.username())
        ));
    }
}
```

### 5.2 DTOs do AuthController

```java
// RegisterRequest.java
package com.pucsp.alexandria.adapter.in.rest.auth.dto;

public record RegisterRequest(
    String username,
    String firstName,
    String lastName,
    String email,
    String password
) {}
```

```java
// RegisterResponse.java
package com.pucsp.alexandria.adapter.in.rest.auth.dto;

import com.pucsp.alexandria.application.auth.dto.RegisterOutput;

public record RegisterResponse(
    Long id,
    String username,
    String email
) {
    public static RegisterResponse from(RegisterOutput output) {
        return new RegisterResponse(output.id(), output.username(), output.email());
    }
}
```

```java
// AuthRequest.java
package com.pucsp.alexandria.adapter.in.rest.auth.dto;

public record AuthRequest(
    String username,
    String password
) {}
```

```java
// AuthResponse.java
package com.pucsp.alexandria.adapter.in.rest.auth.dto;

import com.pucsp.alexandria.application.auth.dto.AuthOutput;

public record AuthResponse(
    String token,
    String type,
    Long userId,
    String username
) {
    public static AuthResponse from(AuthOutput output) {
        return new AuthResponse(output.token(), output.type(), output.userId(), output.username());
    }
}
```

### 5.3 `UserBooksController.java`

**Pacote:** `com.pucsp.alexandria.adapter.in.rest`

```java
package com.pucsp.alexandria.adapter.in.rest;

import com.pucsp.alexandria.adapter.in.rest.dto.AddUserBooksRequest;
import com.pucsp.alexandria.adapter.in.rest.dto.UpdateUserBooksRequest;
import com.pucsp.alexandria.adapter.in.rest.dto.UserBooksResponse;
import com.pucsp.alexandria.application.userbooks.AddUserBooksUseCase;
import com.pucsp.alexandria.application.userbooks.ListUserBooksUseCase;
import com.pucsp.alexandria.application.userbooks.RemoveUserBooksUseCase;
import com.pucsp.alexandria.application.userbooks.UpdateUserBooksUseCase;
import com.pucsp.alexandria.application.userbooks.dto.AddUserBooksInput;
import com.pucsp.alexandria.application.userbooks.dto.UpdateUserBooksInput;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user-books")
public class UserBooksController {

    private final AddUserBooksUseCase addUserBooksUseCase;
    private final ListUserBooksUseCase listUserBooksUseCase;
    private final UpdateUserBooksUseCase updateUserBooksUseCase;
    private final RemoveUserBooksUseCase removeUserBooksUseCase;

    public UserBooksController(
            AddUserBooksUseCase addUserBooksUseCase,
            ListUserBooksUseCase listUserBooksUseCase,
            UpdateUserBooksUseCase updateUserBooksUseCase,
            RemoveUserBooksUseCase removeUserBooksUseCase) {
        this.addUserBooksUseCase = addUserBooksUseCase;
        this.listUserBooksUseCase = listUserBooksUseCase;
        this.updateUserBooksUseCase = updateUserBooksUseCase;
        this.removeUserBooksUseCase = removeUserBooksUseCase;
    }

    @GetMapping
    public ResponseEntity<Page<UserBooksResponse>> list(
            Authentication authentication,
            @RequestParam(required = false) String status,
            Pageable pageable) {
        Long userId = (Long) authentication.getPrincipal();
        var page = listUserBooksUseCase.execute(userId, status, pageable);
        return ResponseEntity.ok(page.map(UserBooksResponse::from));
    }

    @PostMapping
    public ResponseEntity<UserBooksResponse> add(
            Authentication authentication,
            @RequestBody AddUserBooksRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        var input = new AddUserBooksInput(request.bookId(), request.status());
        var output = addUserBooksUseCase.execute(userId, input);
        return ResponseEntity.status(HttpStatus.CREATED).body(UserBooksResponse.from(output));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserBooksResponse> update(
            Authentication authentication,
            @PathVariable Long id,
            @RequestBody UpdateUserBooksRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        var input = new UpdateUserBooksInput(request.status(), request.progress(), request.rating());
        var output = updateUserBooksUseCase.execute(userId, id, input);
        return ResponseEntity.ok(UserBooksResponse.from(output));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remove(
            Authentication authentication,
            @PathVariable Long id) {
        Long userId = (Long) authentication.getPrincipal();
        removeUserBooksUseCase.execute(userId, id);
        return ResponseEntity.noContent().build();
    }
}
```

### 5.4 DTOs do UserBooksController

```java
// AddUserBooksRequest.java
package com.pucsp.alexandria.adapter.in.rest.dto;

public record AddUserBooksRequest(
    Long bookId,
    String status
) {}
```

```java
// UpdateUserBooksRequest.java
package com.pucsp.alexandria.adapter.in.rest.dto;

public record UpdateUserBooksRequest(
    String status,
    Integer progress,
    Integer rating
) {}
```

```java
// UserBooksResponse.java
package com.pucsp.alexandria.adapter.in.rest.dto;

import com.pucsp.alexandria.application.userbooks.dto.UserBooksOutput;

/**
 * Resposta da API conforme contrato:
 * {
 *   "id": 1,
 *   "book": { "id": 1, "gutenbergId": 1787, "title": "...", "author": "...", "cover": "...", "downloadUrl": "..." },
 *   "status": "reading",
 *   "progress": 45
 * }
 */
public record UserBooksResponse(
    Long id,
    BookSummaryResponse book,
    String status,
    Integer progress,
    Integer rating
) {
    public static UserBooksResponse from(UserBooksOutput output) {
        return new UserBooksResponse(
            output.id(),
            BookSummaryResponse.from(output.book()),
            output.status(),
            output.progress(),
            output.rating()
        );
    }
}

/**
 * Versão resumida do Book, apenas com os campos do contrato:
 * id, gutenbergId, title, author, cover, downloadUrl
 */
record BookSummaryResponse(
    Long id,
    Long gutenbergId,
    String title,
    String author,
    String cover,
    String downloadUrl
) {
    public static BookSummaryResponse from(com.pucsp.alexandria.application.book.dto.BookOutput book) {
        return new BookSummaryResponse(
            book.id().getValue(),
            book.gutendexId(),
            book.title(),
            book.author(),
            book.coverUrl(),
            book.downloadUrl()
        );
    }
}
```

---

## 🔨 Passo 6 — Adapter de Saída (Persistência)

### 6.1 `UserBooksEntity.java` (REFORMULAR)

**Pacote:** `com.pucsp.alexandria.adapter.out.persistence.entity`

Substituir o `UserBooksEntity` existente (que usa `BookStatus` do pacote `domain.book`) por um que use `UserBooksStatus` do pacote `domain.userbook`. O campo `status` deixa de ser `BookStatus` (com `toread, reading, done, borrowed`) e passa a ser `UserBooksStatus` (com `TOREAD, READING, DONE`), armazenado como String no banco.

```java
package com.pucsp.alexandria.adapter.out.persistence.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_books", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "book_id"})
})
public class UserBooksEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private BookEntity book;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "toread";

    private Integer progress;

    private Integer rating;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public UserBooksEntity() {}

    public UserBooksEntity(Long id, UserEntity user, BookEntity book, String status,
                           Integer progress, Integer rating, LocalDateTime createdAt) {
        this.id = id;
        this.user = user;
        this.book = book;
        this.status = status;
        this.progress = progress;
        this.rating = rating;
        this.createdAt = createdAt;
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public UserEntity getUser() { return user; }
    public void setUser(UserEntity user) { this.user = user; }

    public BookEntity getBook() { return book; }
    public void setBook(BookEntity book) { this.book = book; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getProgress() { return progress; }
    public void setProgress(Integer progress) { this.progress = progress; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
```

### 6.2 `UserBooksJpaRepository.java`

**Pacote:** `com.pucsp.alexandria.adapter.out.persistence.jpa`

```java
package com.pucsp.alexandria.adapter.out.persistence.jpa;

import com.pucsp.alexandria.adapter.out.persistence.entity.UserBooksEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserBooksJpaRepository extends JpaRepository<UserBooksEntity, Long> {

    Page<UserBooksEntity> findByUserId(Long userId, Pageable pageable);

    Page<UserBooksEntity> findByUserIdAndStatus(Long userId, String status, Pageable pageable);

    Optional<UserBooksEntity> findByUserIdAndBookId(Long userId, Long bookId);

    boolean existsByUserIdAndBookId(Long userId, Long bookId);
}
```

### 6.3 `UserBooksMapper.java`

**Pacote:** `com.pucsp.alexandria.adapter.out.persistence.mapper`

```java
package com.pucsp.alexandria.adapter.out.persistence.mapper;

import com.pucsp.alexandria.adapter.out.persistence.entity.BookEntity;
import com.pucsp.alexandria.adapter.out.persistence.entity.UserBooksEntity;
import com.pucsp.alexandria.adapter.out.persistence.entity.UserEntity;
import com.pucsp.alexandria.domain.book.Book;
import com.pucsp.alexandria.domain.user.UserId;
import com.pucsp.alexandria.domain.userbook.UserBooks;
import org.springframework.stereotype.Component;

@Component
public class UserBooksMapper {

    public UserBooks toDomain(UserBooksEntity entity) {
        if (entity == null) return null;
        return UserBooks.restore(
            entity.getId(),
            entity.getUser().getId(),
            entity.getBook().getId(),
            entity.getStatus(),
            entity.getProgress(),
            entity.getRating(),
            entity.getCreatedAt()
        );
    }

    public UserBooksEntity toPersistence(UserBooks userBooks, UserEntity userEntity, BookEntity bookEntity) {
        if (userBooks == null) return null;
        return new UserBooksEntity(
            userBooks.getId(),
            userEntity,
            bookEntity,
            userBooks.getStatus().getValue(),
            userBooks.getProgress(),
            userBooks.getRating(),
            userBooks.getCreatedAt()
        );
    }
}
```

### 6.4 `UserBooksRepositoryImpl.java`

**Pacote:** `com.pucsp.alexandria.adapter.out.persistence`

```java
package com.pucsp.alexandria.adapter.out.persistence;

import com.pucsp.alexandria.adapter.out.persistence.entity.BookEntity;
import com.pucsp.alexandria.adapter.out.persistence.entity.UserBooksEntity;
import com.pucsp.alexandria.adapter.out.persistence.entity.UserEntity;
import com.pucsp.alexandria.adapter.out.persistence.jpa.BookJpaRepository;
import com.pucsp.alexandria.adapter.out.persistence.jpa.UserBooksJpaRepository;
import com.pucsp.alexandria.adapter.out.persistence.jpa.UserJpaRepository;
import com.pucsp.alexandria.adapter.out.persistence.mapper.UserBooksMapper;
import com.pucsp.alexandria.domain.user.UserId;
import com.pucsp.alexandria.domain.userbook.UserBooks;
import com.pucsp.alexandria.domain.userbook.UserBooksRepository;
import com.pucsp.alexandria.domain.userbook.UserBooksStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserBooksRepositoryImpl implements UserBooksRepository {

    private final UserBooksJpaRepository jpaRepository;
    private final UserJpaRepository userJpaRepository;
    private final BookJpaRepository bookJpaRepository;
    private final UserBooksMapper mapper;

    public UserBooksRepositoryImpl(
            UserBooksJpaRepository jpaRepository,
            UserJpaRepository userJpaRepository,
            BookJpaRepository bookJpaRepository,
            UserBooksMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.userJpaRepository = userJpaRepository;
        this.bookJpaRepository = bookJpaRepository;
        this.mapper = mapper;
    }

    @Override
    public UserBooks save(UserBooks userBooks) {
        UserEntity userEntity = userJpaRepository.getReferenceById(userBooks.getUserId().getValue());
        BookEntity bookEntity = bookJpaRepository.getReferenceById(userBooks.getBookId());
        UserBooksEntity entity = mapper.toPersistence(userBooks, userEntity, bookEntity);

        if (userBooks.getId() != null) {
            entity.setId(userBooks.getId());
        }

        UserBooksEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<UserBooks> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Page<UserBooks> findByUserId(UserId userId, Pageable pageable) {
        return jpaRepository.findByUserId(userId.getValue(), pageable)
            .map(mapper::toDomain);
    }

    @Override
    public Page<UserBooks> findByUserIdAndStatus(UserId userId, UserBooksStatus status, Pageable pageable) {
        return jpaRepository.findByUserIdAndStatus(userId.getValue(), status.getValue(), pageable)
            .map(mapper::toDomain);
    }

    @Override
    public Optional<UserBooks> findByUserIdAndBookId(UserId userId, Long bookId) {
        return jpaRepository.findByUserIdAndBookId(userId.getValue(), bookId)
            .map(mapper::toDomain);
    }

    @Override
    public boolean existsByUserIdAndBookId(UserId userId, Long bookId) {
        return jpaRepository.existsByUserIdAndBookId(userId.getValue(), bookId);
    }

    @Override
    public void delete(UserBooks userBooks) {
        jpaRepository.deleteById(userBooks.getId());
    }
}
```

> **Nota:** `UserJpaRepository` precisa ser criado ou já existir. Verificar se há um repositório JPA para `UserEntity`.

---

## 🔨 Passo 7 — Configuração (BeanConfiguration)

Adicionar no final da classe `BeanConfiguration.java`:

```java
  // ===== UserBooks Use Cases =====

  @Bean
  public AddUserBooksUseCase addUserBooksUseCase(
      UserBooksRepository userBooksRepository,
      BookRepository bookRepository) {
    return new AddUserBooksUseCase(userBooksRepository, bookRepository);
  }

  @Bean
  public ListUserBooksUseCase listUserBooksUseCase(
      UserBooksRepository userBooksRepository,
      BookRepository bookRepository) {
    return new ListUserBooksUseCase(userBooksRepository, bookRepository);
  }

  @Bean
  public UpdateUserBooksUseCase updateUserBooksUseCase(
      UserBooksRepository userBooksRepository,
      BookRepository bookRepository) {
    return new UpdateUserBooksUseCase(userBooksRepository, bookRepository);
  }

  @Bean
  public RemoveUserBooksUseCase removeUserBooksUseCase(
      UserBooksRepository userBooksRepository) {
    return new RemoveUserBooksUseCase(userBooksRepository);
  }

  // ===== Auth Use Cases =====

  @Bean
  public RegisterUserUseCase registerUserUseCase(UserRepository userRepository) {
    return new RegisterUserUseCase(userRepository);
  }

  @Bean
  public AuthenticateUserUseCase authenticateUserUseCase(UserRepository userRepository) {
    return new AuthenticateUserUseCase(userRepository);
  }
```

> **Nota:** `UserRepository` (interface de domínio) ainda não existe e precisará ser criada.

---

## 🔨 Passo 8 — Criação do Domínio UserRepository e UserJpaRepository

Como o `UserRepository` (porta de saída para User) não existe, precisamos criá-la, junto com sua implementação:

### 8.1 `UserRepository.java` (Porta de Saída)

**Pacote:** `com.pucsp.alexandria.domain.user`

```java
package com.pucsp.alexandria.domain.user;

import java.util.Optional;

public interface UserRepository {

    User save(User user);

    Optional<User> findById(Long id);

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    void delete(User user);
}
```

### 8.2 `UserJpaRepository.java`

**Pacote:** `com.pucsp.alexandria.adapter.out.persistence.jpa`

```java
package com.pucsp.alexandria.adapter.out.persistence.jpa;

import com.pucsp.alexandria.adapter.out.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserJpaRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
```

### 8.3 `UserMapper.java`

**Pacote:** `com.pucsp.alexandria.adapter.out.persistence.mapper`

```java
package com.pucsp.alexandria.adapter.out.persistence.mapper;

import com.pucsp.alexandria.adapter.out.persistence.entity.UserEntity;
import com.pucsp.alexandria.domain.user.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toDomain(UserEntity entity) {
        if (entity == null) return null;
        return User.restore(
            entity.getId(),
            entity.getUsername(),
            entity.getFirstName(),
            entity.getLastName(),
            entity.getEmail(),
            entity.getPassword(),
            entity.getCreatedAt()
        );
    }

    public UserEntity toPersistence(User user) {
        if (user == null) return null;
        return new UserEntity(
            user.getId() != null ? user.getId().getValue() : null,
            user.getUsername(),
            user.getFirstName(),
            user.getLastName(),
            user.getEmail().getValue(),
            user.getPassword(),
            user.getCreatedAt()
        );
    }
}
```

### 8.4 `UserRepositoryImpl.java`

**Pacote:** `com.pucsp.alexandria.adapter.out.persistence`

```java
package com.pucsp.alexandria.adapter.out.persistence;

import com.pucsp.alexandria.adapter.out.persistence.jpa.UserJpaRepository;
import com.pucsp.alexandria.adapter.out.persistence.mapper.UserMapper;
import com.pucsp.alexandria.domain.user.User;
import com.pucsp.alexandria.domain.user.UserRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserRepositoryImpl implements UserRepository {

    private final UserJpaRepository jpaRepository;
    private final UserMapper mapper;

    public UserRepositoryImpl(UserJpaRepository jpaRepository, UserMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public User save(User user) {
        var entity = mapper.toPersistence(user);
        var saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<User> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return jpaRepository.findByUsername(username).map(mapper::toDomain);
    }

    @Override
    public boolean existsByUsername(String username) {
        return jpaRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }

    @Override
    public void delete(User user) {
        jpaRepository.deleteById(user.getId().getValue());
    }
}
```

---

## 🔨 Passo 9 — CORS Configuration

Criar `CorsConfig.java` para permitir requisições do frontend (localhost:3000):

**Pacote:** `com.pucsp.alexandria.config`

```java
package com.pucsp.alexandria.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                    .allowedOrigins("http://localhost:3000")
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                    .allowedHeaders("*");
            }
        };
    }
}
```

---

## 🔨 Passo 10 — GlobalExceptionHandler (MODIFICAR)

Substituir o conteúdo do `GlobalExceptionHandler.java` para incluir os novos handlers:

```java
package com.pucsp.alexandria.advice;

import com.pucsp.alexandria.domain.book.exception.BookNotFoundException;
import com.pucsp.alexandria.domain.userbook.exception.DuplicateUserBooksException;
import com.pucsp.alexandria.domain.userbook.exception.InvalidUserBooksException;
import com.pucsp.alexandria.domain.userbook.exception.UserBooksNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  // 404 — Book não encontrado
  @ExceptionHandler(BookNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleBookNotFound(BookNotFoundException ex) {
    return ResponseEntity
        .status(HttpStatus.NOT_FOUND)
        .body(new ErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND.value()));
  }

  // 404 — UserBook não encontrado
  @ExceptionHandler(UserBooksNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleUserBooksNotFound(UserBooksNotFoundException ex) {
    return ResponseEntity
        .status(HttpStatus.NOT_FOUND)
        .body(new ErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND.value()));
  }

  // 400 — Validação de negócio
  @ExceptionHandler(InvalidUserBooksException.class)
  public ResponseEntity<ErrorResponse> handleInvalidUserBooks(InvalidUserBooksException ex) {
    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(new ErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST.value()));
  }

  // 400 — Argumento inválido (ex: status inválido)
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(new ErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST.value()));
  }

  // 409 — Conflito (livro já na biblioteca)
  @ExceptionHandler(DuplicateUserBooksException.class)
  public ResponseEntity<ErrorResponse> handleDuplicateUserBooks(DuplicateUserBooksException ex) {
    return ResponseEntity
        .status(HttpStatus.CONFLICT)
        .body(new ErrorResponse(ex.getMessage(), HttpStatus.CONFLICT.value()));
  }

  // 401 — Credenciais inválidas (login)
  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex) {
    return ResponseEntity
        .status(HttpStatus.UNAUTHORIZED)
        .body(new ErrorResponse("Usuário ou senha inválidos", HttpStatus.UNAUTHORIZED.value()));
  }

  // 500 — Erro genérico
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleException(Exception ex) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(new ErrorResponse(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
  }
}
```

---

## 🔨 Passo 11 — application.properties

Adicionar configurações de JWT no `application.properties`:

```properties
# JWT Configuration
jwt.secret=alexandria-secret-key-mudar-em-producao-deve-ter-256bits
jwt.expiration-ms=86400000
```

---

## 📋 Resumo Final: Lista de Arquivos

### Criar (24 arquivos):

| # | Arquivo | Pacote |
|---|---------|--------|
| 1 | `UserBooksStatus.java` | `domain.userbook` |
| 2 | `UserBooks.java` (reformular) | `domain.userbook` |
| 3 | `UserBooksRepository.java` | `domain.userbook` |
| 4 | `InvalidUserBooksException.java` | `domain.userbook.exception` |
| 5 | `UserBooksNotFoundException.java` | `domain.userbook.exception` |
| 6 | `DuplicateUserBooksException.java` | `domain.userbook.exception` |
| 7 | `UserRepository.java` | `domain.user` |
| 6 | `UserBooksOutput.java` | `application.userbooks.dto` |
| 7 | `AddUserBooksInput.java` | `application.userbooks.dto` |
| 8 | `UpdateUserBooksInput.java` | `application.userbooks.dto` |
| 9 | `AddUserBooksUseCase.java` | `application.userbooks` |
| 10 | `ListUserBooksUseCase.java` | `application.userbooks` |
| 11 | `UpdateUserBooksUseCase.java` | `application.userbooks` |
| 12 | `RemoveUserBooksUseCase.java` | `application.userbooks` |
| 13 | `RegisterInput.java` | `application.auth.dto` |
| 14 | `RegisterOutput.java` | `application.auth.dto` |
| 15 | `AuthInput.java` | `application.auth.dto` |
| 16 | `AuthOutput.java` | `application.auth.dto` |
| 17 | `RegisterUserUseCase.java` | `application.auth` |
| 18 | `AuthenticateUserUseCase.java` | `application.auth` |
| 19 | `UserBooksController.java` | `adapter.in.rest` |
| 20 | `AddUserBooksRequest.java` | `adapter.in.rest.dto` |
| 21 | `UpdateUserBooksRequest.java` | `adapter.in.rest.dto` |
| 22 | `UserBooksResponse.java` | `adapter.in.rest.dto` |
| 23 | `BookSummaryResponse.java` | `adapter.in.rest.dto` |
| 24 | `AuthController.java` | `adapter.in.rest.auth` |
| 25 | `RegisterRequest.java` | `adapter.in.rest.auth.dto` |
| 26 | `RegisterResponse.java` | `adapter.in.rest.auth.dto` |
| 27 | `AuthRequest.java` | `adapter.in.rest.auth.dto` |
| 28 | `AuthResponse.java` | `adapter.in.rest.auth.dto` |
| 29 | `UserBooksEntity.java` (reformular) | `adapter.out.persistence.entity` |
| 30 | `UserBooksJpaRepository.java` | `adapter.out.persistence.jpa` |
| 31 | `UserBooksMapper.java` | `adapter.out.persistence.mapper` |
| 32 | `UserBooksRepositoryImpl.java` | `adapter.out.persistence` |
| 33 | `UserJpaRepository.java` | `adapter.out.persistence.jpa` |
| 34 | `UserMapper.java` | `adapter.out.persistence.mapper` |
| 35 | `UserRepositoryImpl.java` | `adapter.out.persistence` |
| 36 | `JwtTokenProvider.java` | `config.jwt` |
| 37 | `JwtAuthenticationFilter.java` | `config.jwt` |
| 38 | `JwtAuthenticationEntryPoint.java` | `config.jwt` |
| 39 | `UserDetailsServiceImpl.java` | `config` |
| 40 | `CorsConfig.java` | `config` |

### Modificar (4 arquivos):

| # | Arquivo | Mudança |
|---|---------|---------|
| 1 | `pom.xml` | Adicionar spring-security, jjwt-api, jjwt-impl, jjwt-jackson |
| 2 | `SecurityConfig.java` | Substituir classe vazia por config completa com JWT |
| 3 | `BeanConfiguration.java` | Adicionar @Bean dos novos Use Cases |
| 4 | `GlobalExceptionHandler.java` | Adicionar handler para InvalidUserBooksException e IllegalArgumentException |

### Remover (2 arquivos):

| # | Arquivo | Motivo |
|---|---------|--------|
| 1 | `domain/userbook/BookStatus.java` | Substituído por `UserBooksStatus.java` |
| 2 | `domain/userbook/Progress.java` | Lógica movida para UserBooks (create/restore/updateWith) |
