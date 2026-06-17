# Plano: Tela de Configurações — Endpoints de Perfil

## Contexto

A tela de Configurações já existe no frontend (`configuracoes/page.tsx`) mas está **mocada** — exibe apenas o `username` vindo do contexto de autenticação. As seções "Alteração de senha e e-mail em breve.", "Notificações" e "Aparência" estão marcadas como "Em breve".

**Objetivo:** Criar os endpoints no backend para dar vida à seção **Conta**, permitindo que o usuário visualize e edite seus dados.

**Fora de escopo desta etapa:**
- Notificações
- Aparência
- Alteração de e-mail

---

## Visão Geral dos Endpoints

```
Frontend (configuracoes/page.tsx)
  │
  ├── GET  /profile/me       →  Buscar perfil completo
  ├── PUT  /profile/me       →  Atualizar dados (username, firstName, lastName)
  └── PUT  /profile/password →  Alterar senha
```

Todos os endpoints exigem **autenticação JWT** (diferentemente de `/auth/**` que é público).

---

## 1. Modificações na Entidade de Domínio

### `User.java`

Adicionar dois novos métodos para operações imutáveis:

```java
public User updateProfile(String username, String firstName, String lastName) {
    String finalUsername = username != null ? username : this.username;
    String finalFirstName = firstName != null ? firstName : this.firstName;
    String finalLastName = lastName != null ? lastName : this.lastName;
    validateUsername(finalUsername);
    validateFirstName(finalFirstName);
    validateLastName(finalLastName);
    return new User(this.id, finalUsername, finalFirstName, finalLastName,
                    this.email, this.password, this.createdAt);
}

public User updatePassword(String newPassword) {
    validatePassword(newPassword);
    return new User(this.id, this.username, this.firstName, this.lastName,
                    this.email, newPassword, this.createdAt);
}
```

- `updateProfile` aceita campos opcionais (null = manter atual) e não valida senha.
- `updatePassword` valida o novo formato (`validatePassword`) mas não verifica senha antiga (isso é responsabilidade do use case na camada de aplicação).

---

## 2. Exceções — Criar `UserNotFoundException`

Criar no pacote `domain/user/exception/`:

```java
package com.pucsp.alexandria.domain.user.exception;

import com.pucsp.alexandria.domain.shared.exception.DomainException;

public class UserNotFoundException extends DomainException {

    public UserNotFoundException(Long id) {
        super("User with id " + id + " not found");
    }
}
```

Isso elimina a violação semântica de usar `BookNotFoundException("User not found")`.

---

## 3. Handlers no `GlobalExceptionHandler`

Adicionar handlers para as exceções de domínio de `user` atualmente sem tratamento:

```java
@ExceptionHandler(UserNotFoundException.class)
public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex) {
    return ResponseEntity
        .status(HttpStatus.NOT_FOUND)
        .body(new ErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND.value()));
}

@ExceptionHandler(InvalidUserException.class)
public ResponseEntity<ErrorResponse> handleInvalidUser(InvalidUserException ex) {
    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(new ErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST.value()));
}
```

### Mapa completo de exceções e handlers

| Exceção | HTTP Status | Handler existe? |
|---|---|---|
| `UserNotFoundException` (nova) | 404 NOT_FOUND | ❌ Criar |
| `InvalidUserException` | 400 BAD_REQUEST | ❌ Criar |
| `InvalidCredentialsException` | 401 UNAUTHORIZED | ✅ Já existe |
| `DuplicateUserException` | 409 CONFLICT | ✅ Já existe |
| `BookNotFoundException` | 404 NOT_FOUND | ✅ Já existe |
| `UserBooksNotFoundException` | 404 NOT_FOUND | ✅ Já existe |
| `InvalidUserBooksException` | 400 BAD_REQUEST | ✅ Já existe |
| `DuplicateUserBooksException` | 409 CONFLICT | ✅ Já existe |
| `IllegalArgumentException` | 400 BAD_REQUEST | ✅ Já existe |

---

## 4. Camada de Aplicação (Use Cases)

### Estrutura de pacotes

```
application/
└── profile/
    ├── GetProfileUseCase.java
    ├── UpdateProfileUseCase.java
    ├── UpdatePasswordUseCase.java
    └── dto/
        ├── ProfileOutput.java
        ├── UpdateProfileInput.java
        └── UpdatePasswordInput.java
```

### 4.1 `ProfileOutput.java`

```java
package com.pucsp.alexandria.application.profile.dto;

import java.time.LocalDateTime;

public record ProfileOutput(
    Long userId,
    String username,
    String firstName,
    String lastName,
    String email,
    LocalDateTime createdAt
) {}
```

### 4.2 `UpdateProfileInput.java`

```java
package com.pucsp.alexandria.application.profile.dto;

public record UpdateProfileInput(
    String username,
    String firstName,
    String lastName
) {}
```

### 4.3 `UpdatePasswordInput.java`

```java
package com.pucsp.alexandria.application.profile.dto;

public record UpdatePasswordInput(
    String currentPassword,
    String newPassword
) {}
```

### 4.4 `GetProfileUseCase.java`

```java
package com.pucsp.alexandria.application.profile;

import com.pucsp.alexandria.application.profile.dto.ProfileOutput;
import com.pucsp.alexandria.domain.user.User;
import com.pucsp.alexandria.domain.user.UserRepository;
import com.pucsp.alexandria.domain.user.exception.UserNotFoundException;

public class GetProfileUseCase {

    private final UserRepository userRepository;

    public GetProfileUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public ProfileOutput execute(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));

        return new ProfileOutput(
            user.getId().getValue(),
            user.getUsername(),
            user.getFirstName(),
            user.getLastName(),
            user.getEmail().getValue(),
            user.getCreatedAt()
        );
    }
}
```

### 4.5 `UpdateProfileUseCase.java`

```java
package com.pucsp.alexandria.application.profile;

import com.pucsp.alexandria.application.profile.dto.ProfileOutput;
import com.pucsp.alexandria.application.profile.dto.UpdateProfileInput;
import com.pucsp.alexandria.domain.user.User;
import com.pucsp.alexandria.domain.user.UserRepository;
import com.pucsp.alexandria.domain.user.exception.DuplicateUserException;
import com.pucsp.alexandria.domain.user.exception.UserNotFoundException;

public class UpdateProfileUseCase {

    private final UserRepository userRepository;

    public UpdateProfileUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public ProfileOutput execute(Long userId, UpdateProfileInput input) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));

        // Verifica se o username já está em uso por outro usuário
        if (input.username() != null
            && !input.username().equals(user.getUsername())
            && userRepository.existsByUsername(input.username())) {
            throw new DuplicateUserException("Este nome de usuário já está em uso");
        }

        User updated = user.updateProfile(input.username(), input.firstName(), input.lastName());
        User saved = userRepository.save(updated);

        return new ProfileOutput(
            saved.getId().getValue(),
            saved.getUsername(),
            saved.getFirstName(),
            saved.getLastName(),
            saved.getEmail().getValue(),
            saved.getCreatedAt()
        );
    }
}
```

### 4.6 `UpdatePasswordUseCase.java`

```java
package com.pucsp.alexandria.application.profile;

import com.pucsp.alexandria.application.profile.dto.UpdatePasswordInput;
import com.pucsp.alexandria.domain.user.User;
import com.pucsp.alexandria.domain.user.UserRepository;
import com.pucsp.alexandria.domain.user.exception.InvalidCredentialsException;
import com.pucsp.alexandria.domain.user.exception.UserNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

public class UpdatePasswordUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UpdatePasswordUseCase(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void execute(Long userId, UpdatePasswordInput input) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));

        if (!passwordEncoder.matches(input.currentPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Senha atual incorreta");
        }

        String encodedNewPassword = passwordEncoder.encode(input.newPassword());
        User updated = user.updatePassword(encodedNewPassword);
        userRepository.save(updated);
    }
}
```

---

## 5. Camada de Adaptador (Controller)

### Estrutura de pacotes

```
adapter/in/rest/
└── profile/
    ├── ProfileController.java
    └── dto/
        ├── ProfileResponse.java
        ├── UpdateProfileRequest.java
        └── UpdatePasswordRequest.java
```

### 5.1 DTOs de Request/Response

**ProfileResponse.java:**
```java
package com.pucsp.alexandria.adapter.in.rest.profile.dto;

import com.pucsp.alexandria.application.profile.dto.ProfileOutput;
import java.time.LocalDateTime;

public record ProfileResponse(
    Long userId,
    String username,
    String firstName,
    String lastName,
    String email,
    LocalDateTime createdAt
) {
    public static ProfileResponse from(ProfileOutput output) {
        return new ProfileResponse(
            output.userId(), output.username(),
            output.firstName(), output.lastName(),
            output.email(), output.createdAt()
        );
    }
}
```

**UpdateProfileRequest.java:**
```java
package com.pucsp.alexandria.adapter.in.rest.profile.dto;

public record UpdateProfileRequest(
    String username,
    String firstName,
    String lastName
) {}
```

**UpdatePasswordRequest.java:**
```java
package com.pucsp.alexandria.adapter.in.rest.profile.dto;

public record UpdatePasswordRequest(
    String currentPassword,
    String newPassword
) {}
```

### 5.2 `ProfileController.java`

```java
package com.pucsp.alexandria.adapter.in.rest.profile;

import com.pucsp.alexandria.adapter.in.rest.profile.dto.ProfileResponse;
import com.pucsp.alexandria.adapter.in.rest.profile.dto.UpdateProfileRequest;
import com.pucsp.alexandria.adapter.in.rest.profile.dto.UpdatePasswordRequest;
import com.pucsp.alexandria.application.profile.GetProfileUseCase;
import com.pucsp.alexandria.application.profile.UpdateProfileUseCase;
import com.pucsp.alexandria.application.profile.UpdatePasswordUseCase;
import com.pucsp.alexandria.application.profile.dto.UpdateProfileInput;
import com.pucsp.alexandria.application.profile.dto.UpdatePasswordInput;
import com.pucsp.alexandria.domain.shared.valueobject.AuthenticatedUser;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/profile")
@SecurityRequirement(name = "bearerAuth")
public class ProfileController {

    private final GetProfileUseCase getProfileUseCase;
    private final UpdateProfileUseCase updateProfileUseCase;
    private final UpdatePasswordUseCase updatePasswordUseCase;

    public ProfileController(
        GetProfileUseCase getProfileUseCase,
        UpdateProfileUseCase updateProfileUseCase,
        UpdatePasswordUseCase updatePasswordUseCase
    ) {
        this.getProfileUseCase = getProfileUseCase;
        this.updateProfileUseCase = updateProfileUseCase;
        this.updatePasswordUseCase = updatePasswordUseCase;
    }

    @GetMapping("/me")
    public ResponseEntity<ProfileResponse> getProfile(Authentication authentication) {
        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
        var output = getProfileUseCase.execute(user.id());
        return ResponseEntity.ok(ProfileResponse.from(output));
    }

    @PutMapping("/me")
    public ResponseEntity<ProfileResponse> updateProfile(
        Authentication authentication,
        @RequestBody UpdateProfileRequest request
    ) {
        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
        var input = new UpdateProfileInput(request.username(), request.firstName(), request.lastName());
        var output = updateProfileUseCase.execute(user.id(), input);
        return ResponseEntity.ok(ProfileResponse.from(output));
    }

    @PutMapping("/password")
    public ResponseEntity<Void> updatePassword(
        Authentication authentication,
        @RequestBody UpdatePasswordRequest request
    ) {
        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
        var input = new UpdatePasswordInput(request.currentPassword(), request.newPassword());
        updatePasswordUseCase.execute(user.id(), input);
        return ResponseEntity.noContent().build();
    }
}
```

---

## 6. Modificações em Arquivos Existentes

### 6.1 `SecurityConfig.java` — ⚠️ CRÍTICO

O `SecurityConfig` atual tem `.requestMatchers("/auth/**").permitAll()` que tornaria `/profile/**` público por herança de rota. Precisamos restringir para explicitar apenas os endpoints públicos de autenticação:

```java
// ANTES
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/auth/**", "/api/jobs/**").permitAll()
    ...
)

// DEPOIS
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/auth/register", "/auth/login", "/auth/google").permitAll()
    .requestMatchers("/api/jobs/**").permitAll()
    ...
)
```

Dessa forma, `/profile/**` cai no `.anyRequest().authenticated()` e exige JWT.

**Arquivo completo com a alteração:**

```java
package com.pucsp.alexandria.config;

import com.pucsp.alexandria.config.jwt.JwtAuthenticationEntryPoint;
import com.pucsp.alexandria.config.jwt.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.Customizer;
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
        .cors(Customizer.withDefaults())
        .csrf(csrf -> csrf.disable())
        .exceptionHandling(ex -> ex.authenticationEntryPoint(jwtAuthenticationEntryPoint))
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/auth/register", "/auth/login", "/auth/google").permitAll()
            .requestMatchers("/api/jobs/**").permitAll()
            .requestMatchers("/actuator/health").permitAll()
            .requestMatchers("/error").permitAll()
            .requestMatchers("/swagger-ui/**", "/api-docs/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
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

### 6.2 `BeanConfiguration.java`

Adicionar os beans dos 3 novos use cases ao final da classe:

```java
@Bean
public GetProfileUseCase getProfileUseCase(UserRepository userRepository) {
    return new GetProfileUseCase(userRepository);
}

@Bean
public UpdateProfileUseCase updateProfileUseCase(UserRepository userRepository) {
    return new UpdateProfileUseCase(userRepository);
}

@Bean
public UpdatePasswordUseCase updatePasswordUseCase(
    UserRepository userRepository,
    PasswordEncoder passwordEncoder
) {
    return new UpdatePasswordUseCase(userRepository, passwordEncoder);
}
```

### 6.3 `GlobalExceptionHandler.java`

Adicionar os handlers para `UserNotFoundException` e `InvalidUserException`:

```java
@ExceptionHandler(UserNotFoundException.class)
public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex) {
    return ResponseEntity
        .status(HttpStatus.NOT_FOUND)
        .body(new ErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND.value()));
}

@ExceptionHandler(InvalidUserException.class)
public ResponseEntity<ErrorResponse> handleInvalidUser(InvalidUserException ex) {
    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(new ErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST.value()));
}
```

---

## 7. Exceções Utilizadas

| Exceção | Uso | HTTP Status | Já existe? |
|---|---|---|---|
| `UserNotFoundException` (nova) | Usuário não encontrado por ID | 404 NOT_FOUND | ❌ Criar |
| `InvalidUserException` | Dados inválidos (ex: nome vazio) | 400 BAD_REQUEST | ✅ Sim (sem handler) |
| `DuplicateUserException` | Username já em uso | 409 CONFLICT | ✅ Sim |
| `InvalidCredentialsException` | Senha atual incorreta | 401 UNAUTHORIZED | ✅ Sim |

---

## 8. Testes

### 8.1 Testes Unitários (3 arquivos)

```
src/test/java/com/pucsp/alexandria/application/profile/
├── GetProfileUseCaseTest.java
├── UpdateProfileUseCaseTest.java
└── UpdatePasswordUseCaseTest.java
```

Cada teste deve seguir o padrão dos testes existentes (ex: `RegisterUserUseCaseTest.java`):
- `@ExtendWith(MockitoExtension.class)`
- Mockar `UserRepository` (e `PasswordEncoder` onde necessário)
- Testar fluxo feliz (happy path)
- Testar exceções (usuário não encontrado, username duplicado, senha incorreta)

### 8.2 Teste de Integração (1 arquivo)

```
src/test/java/com/pucsp/alexandria/adapter/in/rest/
└── ProfileControllerIntegrationTest.java
```

Seguir o padrão dos testes existentes (`AuthControllerIntegrationTest.java`):
- `@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)` / `@AutoConfigureMockMvc`
- `@MockitoBean` para os use cases
- Criar usuário via `/auth/register` para obter token JWT
- Testar GET /profile/me com token válido
- Testar GET /profile/me sem token (401)
- Testar PUT /profile/me (alterar nome e username)
- Testar PUT /profile/me com username duplicado (409)
- Testar PUT /profile/me com firstName vazio (400)
- Testar PUT /profile/password (alterar senha) — sucesso
- Testar PUT /profile/password com senha incorreta (401)

---

## 9. Fluxos de Requisição (Exemplos)

### GET /profile/me (autenticado)

```
Client
  │
  └─▶ GET /profile/me
      Header: Authorization: Bearer eyJhbGci...
      │
      ├─▶ JwtAuthenticationFilter
      │     Extrai token, valida, cria AuthenticatedUser
      │     Seta no SecurityContext
      │
      ├─▶ ProfileController.getProfile()
      │     Extrai AuthenticatedUser do Authentication
      │     userId = 1
      │
      ├─▶ GetProfileUseCase.execute(1)
      │     userRepository.findById(1) → User
      │     Retorna ProfileOutput
      │
      └─▶ Response 200
          {
            "userId": 1,
            "username": "talita",
            "firstName": "Talita",
            "lastName": "Alves",
            "email": "talita@email.com",
            "createdAt": "2026-01-15T10:30:00"
          }
```

### PUT /profile/me (autenticado)

```
Client
  │
  └─▶ PUT /profile/me
      Header: Authorization: Bearer eyJhbGci...
      Body: { "username": "talita_novo", "firstName": "Talita", "lastName": "Alves" }
      │
      ├─▶ JwtAuthenticationFilter
      │
      ├─▶ ProfileController.updateProfile()
      │     userId = 1
      │
      ├─▶ UpdateProfileUseCase.execute(1, input)
      │     1. userRepository.findById(1) → User
      │     2. Verifica se novo username já existe (se diferente do atual)
      │     3. user.updateProfile("talita_novo", "Talita", "Alves") → User updated
      │     4. userRepository.save(updated) → persist
      │
      └─▶ Response 200
          {
            "userId": 1,
            "username": "talita_novo",
            "firstName": "Talita",
            "lastName": "Alves",
            "email": "talita@email.com",
            "createdAt": "2026-01-15T10:30:00"
          }
```

### PUT /profile/password (autenticado)

```
Client
  │
  └─▶ PUT /profile/password
      Header: Authorization: Bearer eyJhbGci...
      Body: { "currentPassword": "senhaAntiga123", "newPassword": "senhaNova456" }
      │
      ├─▶ JwtAuthenticationFilter
      │
      ├─▶ ProfileController.updatePassword()
      │     userId = 1
      │
      ├─▶ UpdatePasswordUseCase.execute(1, input)
      │     1. userRepository.findById(1) → User
      │     2. passwordEncoder.matches("senhaAntiga123", user.getPassword()) → true
      │     3. passwordEncoder.encode("senhaNova456") → "$2a$10$..."
      │     4. user.updatePassword("$2a$10$...") → User updated
      │     5. userRepository.save(updated) → persist
      │
      └─▶ Response 204 No Content
```

---

## 10. Cronograma Sugerido

| Etapa | O que inclui | Arquivos |
|---|---|---|
| 1. Exceção | `UserNotFoundException.java` | 1 criado |
| 2. Domínio | `updateProfile()` e `updatePassword()` em `User.java` | 1 modificado |
| 3. Handlers | `GlobalExceptionHandler` (UserNotFound + InvalidUser) | 1 modificado |
| 4. Use cases | 3 classes + 3 DTOs em `application/profile/` | 6 criados |
| 5. Controller | `ProfileController` + 3 DTOs | 4 criados |
| 6. Infra | `SecurityConfig` + `BeanConfiguration` | 2 modificados |
| 7. Testes unitários | 3 testes em `application/profile/` | 3 criados |
| 8. Teste integração | 1 controller test | 1 criado |
| 9. Validação | `./mvnw test` + verificar cobertura | — |

**Total: 15 arquivos criados + 4 modificados = 19 arquivos**
