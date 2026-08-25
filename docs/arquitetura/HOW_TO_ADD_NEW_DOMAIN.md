# 📋 Passo a Passo: Como Implementar um Novo Domínio

Este guia descreve todos os arquivos que precisam ser criados/modificados para adicionar um novo domínio seguindo a Clean Architecture do Alexandria.

> ⚠️ **Nota:** Este guia usa "Author" como exemplo, mas Author **já está implementado** no código. Use este guia como referência para criar **outros** domínios (Publisher, Category, Loan, etc.).

---

## 📊 Checklist por Camada

```
Novo Domínio: Xxx (ex: Publisher)
├── 🧬 DOMÍNIO (5 arquivos)
│   ├── Xxx.java
│   ├── XxxId.java
│   ├── XxxRepository.java  (porta de saída)
│   └── exception/
│       └── InvalidXxxException.java
│
├── ⚙️ APLICAÇÃO (7 arquivos)
│   ├── CreateXxxUseCase.java
│   ├── GetXxxUseCase.java
│   ├── ListXxxUseCase.java
│   ├── UpdateXxxUseCase.java
│   ├── DeleteXxxUseCase.java
│   └── dto/
│       ├── XxxOutput.java
│       ├── CreateXxxInput.java
│       └── UpdateXxxInput.java
│
├── 🏗️ INFRAESTRUTURA - Adapters (7 arquivos)
│   ├── adapter/in/rest/
│   │   ├── XxxController.java
│   │   └── dto/
│   │       ├── XxxResponse.java
│   │       ├── CreateXxxRequest.java
│   │       └── UpdateXxxRequest.java
│   │
│   └── adapter/out/persistence/
│       ├── XxxRepositoryImpl.java
│       ├── entity/XxxEntity.java
│       ├── jpa/XxxJpaRepository.java
│       └── mapper/XxxMapper.java
│
├── ⚙️ CONFIGURAÇÃO (1 modificação)
│   └── config/BeanConfiguration.java (adicionar @Bean)
│
├── 🔐 SEGURANÇA
│   └── config/SecurityConfig.java (se o endpoint precisar ser autenticado)
│
└── 🛡️ EXCEÇÕES (opcional)
    └── advice/GlobalExceptionHandler.java (se necessário adicionar handler)
```

**Total: ~19 arquivos novos + 1~2 modificações**

---

## 🔨 Passo 1 — Camada de Domínio

### 1.1 Value Object: `XxxId.java`

```java
package com.pucsp.alexandria.domain.xxx;

import com.pucsp.alexandria.domain.shared.valueobject.Id;

public class XxxId extends Id<Long> {

    private XxxId(Long value) {
        super(value);
    }

    public static XxxId from(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("XxxId must be positive");
        }
        return new XxxId(id);
    }
}
```

### 1.2 Entity: `Xxx.java`

```java
package com.pucsp.alexandria.domain.xxx;

import com.pucsp.alexandria.domain.xxx.exception.InvalidXxxException;

public class Xxx {

    private final XxxId id;
    private final String name;
    // ... outros atributos conforme regra de negócio

    // Construtor privado — apenas factory methods
    private Xxx(XxxId id, String name) {
        this.id = id;
        this.name = name;
    }

    // Factory method para criar NOVO (id = null)
    public static Xxx create(String name) {
        validateName(name);
        return new Xxx(null, name);
    }

    // Factory method para RESTAURAR do BD (id preenchido)
    public static Xxx restore(Long id, String name) {
        validateName(name);
        XxxId xxxId = XxxId.from(id);
        return new Xxx(xxxId, name);
    }

    // Factory method para ATUALIZAR com novos valores
    public Xxx updateWith(String newName) {
        String finalName = newName != null ? newName : this.name;
        validateName(finalName);
        return new Xxx(this.id, finalName);
    }

    // Regra de negócio validada dentro do domínio
    private static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidXxxException("Xxx name is required");
        }
        if (name.length() > 255) {
            throw new InvalidXxxException("Xxx name must not exceed 255 characters");
        }
    }

    // Getters públicos (sem setters!)
    public XxxId getId() { return id; }
    public String getName() { return name; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Xxx xxx = (Xxx) obj;
        return id != null ? id.equals(xxx.id) : xxx.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
```

### 1.3 Porta de Saída: `XxxRepository.java`

```java
package com.pucsp.alexandria.domain.xxx;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface XxxRepository {

    Xxx save(Xxx xxx);

    Optional<Xxx> findById(Long id);

    Optional<Xxx> findByName(String name);

    List<Xxx> findAllById(Set<XxxId> ids);

    boolean existsByName(String name);

    void delete(Xxx xxx);
}
```

> **Dica:** Inclua apenas os métodos que seu domínio realmente precisa. Consulte `AuthorRepository` e `BookRepository` como referência.

### 1.4 Exceção: `InvalidXxxException.java`

```java
package com.pucsp.alexandria.domain.xxx.exception;

import com.pucsp.alexandria.domain.shared.exception.DomainException;

public class InvalidXxxException extends DomainException {

    public InvalidXxxException(String message) {
        super(message);
    }
}
```

---

## 🔨 Passo 2 — Camada de Aplicação

### 2.1 DTOs de Entrada/Saída

```java
// XxxOutput.java — DTO de saída (usa XxxId do domínio)
package com.pucsp.alexandria.application.xxx.dto;

import com.pucsp.alexandria.domain.xxx.Xxx;

public record XxxOutput(
    Long id,
    String name
) {
    public static XxxOutput from(Xxx xxx) {
        return new XxxOutput(
            xxx.getId().getValue(),
            xxx.getName()
        );
    }
}
```

> ⚠️ **Importante:** No `XxxOutput` da **aplicação**, você pode usar tipos primitivos (`Long`, `String`) em vez de Value Objects, para simplificar. Compare: `BookOutput` usa `List<AuthorInfo>` (tipos primitivos), enquanto `UserBooksOutput` usa `BookOutput`. O importante é a consistência interna.

```java
// CreateXxxInput.java
package com.pucsp.alexandria.application.xxx.dto;

public record CreateXxxInput(
    String name
) {}
```

```java
// UpdateXxxInput.java
package com.pucsp.alexandria.application.xxx.dto;

public record UpdateXxxInput(
    String name
) {}
```

### 2.2 Use Cases

```java
// CreateXxxUseCase.java
package com.pucsp.alexandria.application.xxx;

import com.pucsp.alexandria.application.xxx.dto.CreateXxxInput;
import com.pucsp.alexandria.application.xxx.dto.XxxOutput;
import com.pucsp.alexandria.domain.xxx.Xxx;
import com.pucsp.alexandria.domain.xxx.XxxRepository;

public class CreateXxxUseCase {

    private final XxxRepository xxxRepository;

    public CreateXxxUseCase(XxxRepository xxxRepository) {
        this.xxxRepository = xxxRepository;
    }

    public XxxOutput execute(CreateXxxInput input) {
        Xxx xxx = Xxx.create(input.name());
        Xxx saved = xxxRepository.save(xxx);
        return XxxOutput.from(saved);
    }
}
```

```java
// GetXxxUseCase.java
package com.pucsp.alexandria.application.xxx;

import com.pucsp.alexandria.application.xxx.dto.XxxOutput;
import com.pucsp.alexandria.domain.xxx.Xxx;
import com.pucsp.alexandria.domain.xxx.XxxRepository;
import com.pucsp.alexandria.domain.book.exception.BookNotFoundException;

public class GetXxxUseCase {

    private final XxxRepository xxxRepository;

    public GetXxxUseCase(XxxRepository xxxRepository) {
        this.xxxRepository = xxxRepository;
    }

    public XxxOutput execute(Long id) {
        Xxx xxx = xxxRepository.findById(id)
            .orElseThrow(() -> new BookNotFoundException("Xxx not found with id: " + id));
        return XxxOutput.from(xxx);
    }
}
```

```java
// ListXxxUseCase.java (se fizer sentido paginar)
package com.pucsp.alexandria.application.xxx;

import com.pucsp.alexandria.application.xxx.dto.XxxOutput;
import com.pucsp.alexandria.domain.xxx.XxxRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public class ListXxxUseCase {

    private final XxxRepository xxxRepository;

    public ListXxxUseCase(XxxRepository xxxRepository) {
        this.xxxRepository = xxxRepository;
    }

    public Page<XxxOutput> execute(Pageable pageable) {
        // Se o repositório suportar Page, use: return xxxRepository.findAll(pageable).map(XxxOutput::from);
        // Caso contrário, adapte
        throw new UnsupportedOperationException("Not implemented");
    }
}
```

```java
// UpdateXxxUseCase.java
package com.pucsp.alexandria.application.xxx;

import com.pucsp.alexandria.application.xxx.dto.UpdateXxxInput;
import com.pucsp.alexandria.application.xxx.dto.XxxOutput;
import com.pucsp.alexandria.domain.xxx.Xxx;
import com.pucsp.alexandria.domain.xxx.XxxRepository;
import com.pucsp.alexandria.domain.book.exception.BookNotFoundException;

public class UpdateXxxUseCase {

    private final XxxRepository xxxRepository;

    public UpdateXxxUseCase(XxxRepository xxxRepository) {
        this.xxxRepository = xxxRepository;
    }

    public XxxOutput execute(Long id, UpdateXxxInput input) {
        Xxx xxx = xxxRepository.findById(id)
            .orElseThrow(() -> new BookNotFoundException("Xxx not found with id: " + id));

        Xxx updated = xxx.updateWith(input.name());
        Xxx saved = xxxRepository.save(updated);
        return XxxOutput.from(saved);
    }
}
```

```java
// DeleteXxxUseCase.java
package com.pucsp.alexandria.application.xxx;

import com.pucsp.alexandria.domain.xxx.Xxx;
import com.pucsp.alexandria.domain.xxx.XxxRepository;
import com.pucsp.alexandria.domain.book.exception.BookNotFoundException;

public class DeleteXxxUseCase {

    private final XxxRepository xxxRepository;

    public DeleteXxxUseCase(XxxRepository xxxRepository) {
        this.xxxRepository = xxxRepository;
    }

    public void execute(Long id) {
        Xxx xxx = xxxRepository.findById(id)
            .orElseThrow(() -> new BookNotFoundException("Xxx not found with id: " + id));
        xxxRepository.delete(xxx);
    }
}
```

---

## 🔨 Passo 3 — Camada de Infraestrutura

### 3.1 Adapter de Saída — Persistência

```java
// XxxEntity.java
package com.pucsp.alexandria.adapter.out.persistence.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "xxx")
public class XxxEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    // Construtor padrão obrigatório para JPA
    public XxxEntity() {}

    public XxxEntity(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
```

```java
// XxxJpaRepository.java
package com.pucsp.alexandria.adapter.out.persistence.jpa;

import com.pucsp.alexandria.adapter.out.persistence.entity.XxxEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface XxxJpaRepository extends JpaRepository<XxxEntity, Long> {

    // Custom queries — veja AuthorJpaRepository como exemplo
    Optional<XxxEntity> findByName(String name);
    boolean existsByName(String name);
}
```

```java
// XxxMapper.java
package com.pucsp.alexandria.adapter.out.persistence.mapper;

import com.pucsp.alexandria.adapter.out.persistence.entity.XxxEntity;
import com.pucsp.alexandria.domain.xxx.Xxx;
import org.springframework.stereotype.Component;

@Component
public class XxxMapper {

    public Xxx toDomain(XxxEntity entity) {
        if (entity == null) return null;
        return Xxx.restore(
            entity.getId(),
            entity.getName()
        );
    }

    public XxxEntity toPersistence(Xxx xxx) {
        if (xxx == null) return null;
        return new XxxEntity(
            xxx.getId() != null ? xxx.getId().getValue() : null,
            xxx.getName()
        );
    }
}
```

```java
// XxxRepositoryImpl.java
package com.pucsp.alexandria.adapter.out.persistence;

import com.pucsp.alexandria.adapter.out.persistence.jpa.XxxJpaRepository;
import com.pucsp.alexandria.adapter.out.persistence.mapper.XxxMapper;
import com.pucsp.alexandria.domain.xxx.Xxx;
import com.pucsp.alexandria.domain.xxx.XxxId;
import com.pucsp.alexandria.domain.xxx.XxxRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public class XxxRepositoryImpl implements XxxRepository {

    private final XxxJpaRepository jpaRepository;
    private final XxxMapper mapper;

    public XxxRepositoryImpl(XxxJpaRepository jpaRepository, XxxMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Xxx save(Xxx xxx) {
        XxxEntity entity = mapper.toPersistence(xxx);
        XxxEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Xxx> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Xxx> findByName(String name) {
        return jpaRepository.findByName(name).map(mapper::toDomain);
    }

    @Override
    public List<Xxx> findAllById(Set<XxxId> ids) {
        List<Long> longIds = ids.stream()
            .map(XxxId::getValue)
            .toList();
        return jpaRepository.findAllById(longIds).stream()
            .map(mapper::toDomain)
            .toList();
    }

    @Override
    public boolean existsByName(String name) {
        return jpaRepository.existsByName(name);
    }

    @Override
    public void delete(Xxx xxx) {
        XxxEntity entity = mapper.toPersistence(xxx);
        jpaRepository.delete(entity);
    }
}
```

### 3.2 Adapter de Entrada — Controller REST

> ⚠️ **Sobre a localização dos DTOs:** Se o domínio for pequeno (ex: apenas 2 DTOs), pode colocá-los direto em `adapter/in/rest/dto/`. Se tiver muitos DTOs, crie um subpacote específico (ex: `adapter/in/rest/xxx/dto/`) — veja o `auth/` como exemplo.

```java
// XxxResponse.java — DTO de resposta (usa Long, NÃO XxxId)
package com.pucsp.alexandria.adapter.in.rest.dto;

import com.pucsp.alexandria.application.xxx.dto.XxxOutput;

public record XxxResponse(
    Long id,
    String name
) {
    public static XxxResponse from(XxxOutput output) {
        return new XxxResponse(
            output.id(),  // ← já é Long (veja nota abaixo)
            output.name()
        );
    }
}
```

> 💡 **Nota:** Se o `XxxOutput` da aplicação retornar `Long` diretamente (como no exemplo acima), não precisa de `.getValue()`. Se retornar `XxxId`, use `.getValue()`. Veja `BookOutput` vs `AuthorOutput` no código real como referência.

```java
// CreateXxxRequest.java
package com.pucsp.alexandria.adapter.in.rest.dto;

public record CreateXxxRequest(
    String name
) {}
```

```java
// UpdateXxxRequest.java
package com.pucsp.alexandria.adapter.in.rest.dto;

public record UpdateXxxRequest(
    String name
) {}
```

```java
// XxxController.java
package com.pucsp.alexandria.adapter.in.rest;

import com.pucsp.alexandria.adapter.in.rest.dto.XxxResponse;
import com.pucsp.alexandria.adapter.in.rest.dto.CreateXxxRequest;
import com.pucsp.alexandria.adapter.in.rest.dto.UpdateXxxRequest;
import com.pucsp.alexandria.application.xxx.*;
import com.pucsp.alexandria.application.xxx.dto.CreateXxxInput;
import com.pucsp.alexandria.application.xxx.dto.UpdateXxxInput;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/xxx")
public class XxxController {

    private final CreateXxxUseCase createXxxUseCase;
    private final GetXxxUseCase getXxxUseCase;
    private final ListXxxUseCase listXxxUseCase;
    private final UpdateXxxUseCase updateXxxUseCase;
    private final DeleteXxxUseCase deleteXxxUseCase;

    public XxxController(
        CreateXxxUseCase createXxxUseCase,
        GetXxxUseCase getXxxUseCase,
        ListXxxUseCase listXxxUseCase,
        UpdateXxxUseCase updateXxxUseCase,
        DeleteXxxUseCase deleteXxxUseCase
    ) {
        this.createXxxUseCase = createXxxUseCase;
        this.getXxxUseCase = getXxxUseCase;
        this.listXxxUseCase = listXxxUseCase;
        this.updateXxxUseCase = updateXxxUseCase;
        this.deleteXxxUseCase = deleteXxxUseCase;
    }

    @PostMapping
    public ResponseEntity<XxxResponse> create(@RequestBody CreateXxxRequest request) {
        CreateXxxInput input = new CreateXxxInput(request.name());
        var output = createXxxUseCase.execute(input);
        return ResponseEntity.ok(XxxResponse.from(output));
    }

    @GetMapping("/{id}")
    public ResponseEntity<XxxResponse> getById(@PathVariable Long id) {
        var output = getXxxUseCase.execute(id);
        return ResponseEntity.ok(XxxResponse.from(output));
    }

    @GetMapping
    public ResponseEntity<Page<XxxResponse>> getAll(Pageable pageable) {
        var page = listXxxUseCase.execute(pageable);
        return ResponseEntity.ok(page.map(XxxResponse::from));
    }

    @PutMapping("/{id}")
    public ResponseEntity<XxxResponse> update(
            @PathVariable Long id,
            @RequestBody UpdateXxxRequest request) {
        UpdateXxxInput input = new UpdateXxxInput(request.name());
        var output = updateXxxUseCase.execute(id, input);
        return ResponseEntity.ok(XxxResponse.from(output));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deleteXxxUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }
}
```

---

## 🔨 Passo 4 — Configuração (BeanConfiguration)

Adicione no final da classe `config/BeanConfiguration.java`:

```java
@Bean
public CreateXxxUseCase createXxxUseCase(XxxRepository xxxRepository) {
    return new CreateXxxUseCase(xxxRepository);
}

@Bean
public GetXxxUseCase getXxxUseCase(XxxRepository xxxRepository) {
    return new GetXxxUseCase(xxxRepository);
}

@Bean
public ListXxxUseCase listXxxUseCase(XxxRepository xxxRepository) {
    return new ListXxxUseCase(xxxRepository);
}

@Bean
public UpdateXxxUseCase updateXxxUseCase(XxxRepository xxxRepository) {
    return new UpdateXxxUseCase(xxxRepository);
}

@Bean
public DeleteXxxUseCase deleteXxxUseCase(XxxRepository xxxRepository) {
    return new DeleteXxxUseCase(xxxRepository);
}
```

---

## 🔨 Passo 5 (Opcional) — Segurança

Se seu novo endpoint **não** deve ser público, configure a rota em `config/SecurityConfig.java`:

```java
.authorizeHttpRequests(auth -> auth
    // Rotas públicas
    .requestMatchers("/auth/**").permitAll()
    .requestMatchers("/books/search").permitAll()
    .requestMatchers("/xxx/**").permitAll()  // Se for público (GET /xxx, GET /xxx/{id})
    // Rotas autenticadas
    .requestMatchers(HttpMethod.POST, "/xxx/**").authenticated()
    .requestMatchers(HttpMethod.PUT, "/xxx/**").authenticated()
    .requestMatchers(HttpMethod.DELETE, "/xxx/**").authenticated()
    .anyRequest().authenticated()
)
```

Se o controller precisar do **usuário autenticado**, adicione o parâmetro `Authentication authentication` nos métodos:

```java
@PostMapping
public ResponseEntity<XxxResponse> create(
        Authentication authentication,
        @RequestBody CreateXxxRequest request) {
    Long userId = (Long) authentication.getPrincipal();
    // ... usa userId na lógica
}
```

---

## 🔨 Passo 6 (Opcional) — Exception Handler

Se você criou novas exceções, adicione handlers em `advice/GlobalExceptionHandler.java`:

```java
@ExceptionHandler(InvalidXxxException.class)
public ResponseEntity<ErrorResponse> handleInvalidXxx(InvalidXxxException ex) {
    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(new ErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST.value()));
}
```

---

## 🧪 Bônus — Testes

Crie testes para cada camada seguindo o padrão do projeto:

```
src/test/java/com/pucsp/alexandria/
└── domain/xxx/           # Testes unitários (sem Spring)
│   └── XxxTest.java
│   └── XxxIdTest.java
├── application/xxx/      # Testes de Use Cases (Mockito)
│   ├── CreateXxxUseCaseTest.java
│   ├── GetXxxUseCaseTest.java
│   └── ...
├── adapter/out/persistence/
│   ├── jpa/XxxJpaRepositoryTest.java     # Testcontainers
│   ├── entity/XxxEntityTest.java
│   └── mapper/XxxMapperTest.java
└── adapter/in/rest/
    └── XxxControllerIntegrationTest.java  # Testcontainers
```

---

## 📐 Padrões para Copiar e Colar

### Estrutura de pastas a criar:
```
domain/xxx/
application/xxx/
adapter/in/rest/          ← XxxController.java e DTOs vão AQUI (ou subpacote)
adapter/out/persistence/  ← *Impl, Entity, JpaRepository, Mapper vão AQUI
```

### Nomenclatura consistente:

| Camada | Sufixo | Exemplo |
|--------|--------|---------|
| Domínio (Entity) | (nome do domínio) | `Publisher`, `Book`, `User` |
| Domínio (ID) | `Id` | `PublisherId`, `BookId`, `UserId` |
| Domínio (Repository) | `Repository` | `PublisherRepository` |
| Domínio (Exception) | `Exception` | `InvalidPublisherException` |
| Aplicação (Use Case) | `UseCase` | `CreatePublisherUseCase` |
| Aplicação (Input) | `Input` | `CreatePublisherInput` |
| Aplicação (Output) | `Output` | `PublisherOutput` |
| Infra (Controller) | `Controller` | `PublisherController` |
| Infra (Request) | `Request` | `CreatePublisherRequest` |
| Infra (Response) | `Response` | `PublisherResponse` |
| Infra (Entity JPA) | `Entity` | `PublisherEntity` |
| Infra (JPA Repo) | `JpaRepository` | `PublisherJpaRepository` |
| Infra (Mapper) | `Mapper` | `PublisherMapper` |
| Infra (Impl) | `Impl` | `PublisherRepositoryImpl` |

---

## 🚨 Pontos de Atenção

### ⚠️ Não fazer:
- ❌ Colocar anotações Spring (`@Service`, `@Component`) nos Use Cases
- ❌ Colocar anotações Spring nas Entities de domínio
- ❌ Use Case depender de implementação concreta (sempre da interface/porta)
- ❌ `*Response` no adapter usar Value Objects do domínio (ex: `BookId`, `UserId`) — use `Long`
- ❌ Getters/setters nas entities de domínio (use apenas getters se necessário)
- ❌ Colocar regras de validação de negócio nos adapters ou controllers
- ❌ Esquecer de adicionar os `@Bean` no `BeanConfiguration`

### ✅ Fazer:
- ✅ Value Objects imutáveis com `final` nos atributos e construtor privado
- ✅ Factory methods estáticos (`create`, `restore`) em vez de construtores públicos
- ✅ Método `updateWith()` na entity para retornar nova instância com valores atualizados
- ✅ Validação de negócio **dentro do domínio** (nos factory methods)
- ✅ DTOs como `record` do Java (imutáveis)
- ✅ `from()` estático nos DTOs para conversão
- ✅ `*Response.id` como `Long` (convertendo via `.getValue()` se necessário)
- ✅ Consultar os domínios existentes (`Author`, `Book`, `User`, `UserBooks`) como referência real
- ✅ Adicionar a Flyway migration se houver mudança estrutural no banco

---

## 📋 Quick Reference: Arquivos por Camada

```
📁 NOVO DOMÍNIO: Xxx (ex: Publisher)
├── domain/xxx/
│   ├── Xxx.java
│   ├── XxxId.java
│   ├── XxxRepository.java
│   └── exception/
│       └── InvalidXxxException.java
│
├── application/xxx/
│   ├── CreateXxxUseCase.java
│   ├── GetXxxUseCase.java
│   ├── ListXxxUseCase.java      (opcional)
│   ├── UpdateXxxUseCase.java
│   ├── DeleteXxxUseCase.java
│   └── dto/
│       ├── XxxOutput.java
│       ├── CreateXxxInput.java
│       └── UpdateXxxInput.java
│
├── adapter/in/rest/
│   ├── XxxController.java
│   └── dto/
│       ├── XxxResponse.java
│       ├── CreateXxxRequest.java
│       └── UpdateXxxRequest.java
│
├── adapter/out/persistence/
│   ├── XxxRepositoryImpl.java
│   ├── entity/XxxEntity.java
│   ├── jpa/XxxJpaRepository.java
│   └── mapper/XxxMapper.java
│
├── config/BeanConfiguration.java         (MODIFICAR - adicionar @Bean)
├── config/SecurityConfig.java            (MODIFICAR - se precisar de rotas)
└── advice/GlobalExceptionHandler.java    (MODIFICAR - se criar novas exceções)
```

> 💡 **Dica final:** Sempre olhe o código real dos domínios já implementados (`Author`, `Book`, `User`, `UserBooks`) como referência mais confiável. Este guia é um template genérico.
