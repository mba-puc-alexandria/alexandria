# 📋 Plano: Job Agendado — Sincronização Semanal Gutendex via EventBridge

> Baseado na arquitetura do projeto (`markdown/ARCHITECTURE.md`) e instruções de apoio (`markdown/instrucoes_config_event_bridge.md`)

---

## 🔀 Preparação do Repositório

Antes de iniciar as alterações no código, atualizar a branch `develop` e criar a branch de trabalho.

```bash
# 1. Muda para a branch develop
git checkout develop

# 2. Atualiza develop com as últimas alterações do remoto
git pull origin develop

# 3. Cria a nova branch de feature a partir da develop
git checkout -b feature/event-bridge

# 4. Publica a branch no remoto
git push -u origin feature/event-bridge
```

Todas as alterações do plano serão feitas na branch `feature/event-bridge`. Quando finalizadas, será aberto um Pull Request para `develop`.

---

## 🧠 Decisões Arquiteturais

**Ponto crítico:** As instruções sugerem colocar `@Async` diretamente no `SyncAllGutendexBooksUseCase`. **Isso viola a Clean Architecture**, que determina:

> *"Use Case **não pode ter anotações Spring** (são registrados como `@Bean` em `BeanConfiguration`)"*

**Solução adotada:** Manter o Use Case **puro** (POJO sem `@Async`) na camada de `application/`, e criar um **adaptador de infraestrutura** (`adapter/in/job/`) com `@Service` + `@Async` que envolve o Use Case. Isso respeita a regra de que a camada de aplicação não depende de frameworks.

---

## 📁 Estrutura de Pacotes — Arquivos Novos/Modificados

```
com.pucsp.alexandria/
│
├── AlexandriaApplication.java                  ← 🛠️ MODIFICAR (+ @EnableAsync)
│
├── application/
│   └── book/
│       ├── SyncAllGutendexBooksUseCase.java    ← 🆕 NOVO (POJO puro, sem Spring)
│       └── CreateBookUseCase.java             ← 🛠️ MODIFICAR (+ try-catch por livro)
│
├── adapter/
│   ├── in/
│   │   ├── rest/
│   │   │   └── JobController.java             ← 🆕 NOVO (endpoint POST gatilho)
│   │   └── job/
│   │       └── SyncGutendexJobService.java    ← 🆕 NOVO (@Service com @Async)
│   └── out/
│       └── persistence/external/gutendex/
│           └── GutendexClient.java           ← 🛠️ MODIFICAR (remover filtro de idioma)
│
├── config/
│   ├── AsyncConfig.java                       ← 🆕 NOVO (TaskExecutor)
│   ├── BeanConfiguration.java                 ← 🛠️ MODIFICAR (+ bean do Use Case)
│   └── SecurityConfig.java                    ← 🛠️ MODIFICAR (+ rota pública)
│
└── resources/
    └── logback-spring.xml                     ← 🆕 NOVO (logging do job em arquivo separado)
```

**Nota sobre as camadas:**
- `JobController` em `adapter/in/rest/` — adapter de entrada por REST
- `SyncGutendexJobService` em `adapter/in/job/` — adapter de entrada por job assíncrono (separação clara de responsabilidades)

---

## 🔧 Fase 1: Alterações no Projeto Spring Boot

---

### 1️⃣ `AlexandriaApplication.java` — Habilitar Async

Adicionar `@EnableAsync` na classe principal.

```java
package com.pucsp.alexandria;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class AlexandriaApplication {

    public static void main(String[] args) {
        SpringApplication.run(AlexandriaApplication.class, args);
    }

}
```

---

### 2️⃣ `config/AsyncConfig.java` — Pool de Threads Dedicado 🆕

Cria um `TaskExecutor` com pool de 1 thread.
> O valor de `queueCapacity` é discutido na seção dedicada abaixo.

```java
package com.pucsp.alexandria.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import java.util.concurrent.Executor;

@Configuration
public class AsyncConfig {

    @Bean(name = "gutendexSyncExecutor")
    public Executor gutendexSyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.setQueueCapacity(0);           // ← ver análise na seção "Opções de Fila"
        executor.setThreadNamePrefix("gutendex-sync-");
        executor.initialize();
        return executor;
    }
}
```

---

### 3️⃣ `adapter/out/persistence/external/gutendex/GutendexClient.java` — Remover Filtro de Idioma 🛠️

**O que muda:** Remover o parâmetro `languages=pt` das consultas à API Gutendex. Agora indexa **todos os idiomas**.

```java
package com.pucsp.alexandria.adapter.out.persistence.external.gutendex;

import com.pucsp.alexandria.adapter.out.persistence.external.gutendex.dto.GutendexSearchResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class GutendexClient {

    private static final String GUTENDEX_API_URL = "https://gutendex.com";
    private static final String SEARCH_BOOKS_ENDPOINT = "/books";

    private final RestTemplate restTemplate;

    public GutendexClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public GutendexSearchResponse searchByTitle(String query) {
        String url = UriComponentsBuilder
                .fromUriString(GUTENDEX_API_URL + SEARCH_BOOKS_ENDPOINT)
                .queryParam("search", query)
                .toUriString();

        return restTemplate.getForObject(url, GutendexSearchResponse.class);
    }

    public GutendexSearchResponse getPage(int page) {
        String url = UriComponentsBuilder
                .fromUriString(GUTENDEX_API_URL + SEARCH_BOOKS_ENDPOINT)
                .queryParam("page", page)
                .toUriString();

        return restTemplate.getForObject(url, GutendexSearchResponse.class);
    }
}
```

**Alterações:**
- ❌ Removeu `SEARCH_BOOKS_DEFAULT_LANGUAGE = "pt"`
- ❌ Removeu `.queryParam("languages", ...)` dos dois métodos
- ✅ `GutendexMapper` continua funcionando (já trata qualquer lista de idiomas)
- ✅ `BookData.languages` continua armazenando os idiomas (ex: `"pt,en,fr"`)
- ✅ Testes existentes (usam `anyString()`) continuam passando

**Impacto na API Gutendex:** Sem o filtro, cada página continua com ~32 livros, mas agora inclui todos os idiomas. O número total de páginas aumentará significativamente (~70k livros na Gutendex).

---

### 4️⃣ `application/book/CreateBookUseCase.java` — Resiliência por Livro 🛠️

**Problema atual:** O `@Transactional` no método `execute()` faz com que a falha de **um único livro** reverta a **página inteira** (~32 livros perdidos).

**Solução:** Adicionar try-catch por livro. Exceções de validação de domínio ou persistência são capturadas, logadas, e o próximo livro é processado.

```java
package com.pucsp.alexandria.application.book;

import com.pucsp.alexandria.application.book.dto.CreateBookInput;
import com.pucsp.alexandria.application.book.dto.CreateBookOutput;
import com.pucsp.alexandria.domain.author.Author;
import com.pucsp.alexandria.domain.author.AuthorId;
import com.pucsp.alexandria.domain.author.AuthorRepository;
import com.pucsp.alexandria.domain.book.Book;
import com.pucsp.alexandria.domain.book.BookRepository;
import com.pucsp.alexandria.domain.book.external.AuthorData;
import com.pucsp.alexandria.domain.book.external.BookApiClient;
import com.pucsp.alexandria.domain.book.external.BookData;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

public class CreateBookUseCase {

    private static final Logger log = LoggerFactory.getLogger(CreateBookUseCase.class);

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final BookApiClient bookApiClient;

    public CreateBookUseCase(BookRepository bookRepository, AuthorRepository authorRepository,
                             BookApiClient bookApiClient) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
        this.bookApiClient = bookApiClient;
    }

    @Transactional
    public CreateBookOutput execute(CreateBookInput input) {
        var bookDataList = bookApiClient.getPage(input.page());
        if (bookDataList.isEmpty()) {
            throw new RuntimeException("Page not found in Gutendex: " + input.page());
        }

        ArrayList<Long> createdIds = new ArrayList<>();
        for (BookData bookData : bookDataList) {
            try {
                processBook(bookData).ifPresent(createdIds::add);
            } catch (Exception e) {
                log.warn("Erro ao processar livro '{}' (gutendexId={}) na página {}: {}",
                        bookData.title(), bookData.gutendexId(), input.page(), e.getMessage());
                // Não interrompe — apenas loga e vai para o próximo livro
            }
        }

        return new CreateBookOutput(createdIds);
    }

    private Optional<Long> processBook(BookData bookData) {
        if (bookRepository.existsByGutendexId(bookData.gutendexId())) {
            return Optional.empty();
        }

        Set<AuthorId> authorIds = findOrCreateAuthors(bookData);
        if (authorIds.isEmpty()) {
            return Optional.empty();
        }

        Book newBook = Book.createFromGutendex(
                bookData.gutendexId(),
                bookData.title(),
                authorIds,
                bookData.downloadUrl(),
                bookData.coverUrl(),
                bookData.languages(),
                bookData.subjects(),
                bookData.downloadCount()
        );

        Book saved = bookRepository.save(newBook);
        return Optional.of(saved.getId().getValue());
    }

    private Set<AuthorId> findOrCreateAuthors(BookData bookData) {
        Set<AuthorId> authorIds = new HashSet<>();
        for (AuthorData authorData : bookData.authorDataList()) {
            String formattedName = authorData.getFormattedName();
            if (formattedName == null || formattedName.isBlank()) {
                continue;
            }

            Author author = authorRepository.findByName(formattedName)
                    .orElseGet(() -> authorRepository.save(Author.create(
                            formattedName,
                            authorData.birthYear(),
                            authorData.deathYear()
                    )));

            authorIds.add(author.getId());
        }
        return authorIds;
    }
}
```

**Mudanças em relação ao original:**
- `execute()` — cada iteração do `for` envolta em try-catch
- Lógica do livro extraída para `processBook()` → retorna `Optional<Long>`
- `processBook()` mantém `continue` lógico (via `Optional.empty()`) para duplicatas e autores vazios
- Log `log.warn` para cada livro que falhar

**Comportamento resultante:**

```
Página 5 — 32 livros
├── Livro 1 ✅ → salvo (id=101)
├── Livro 2 ✅ → salvo (id=102)
├── Livro 3 ❌ → "Erro ao processar livro 'Título Inválido' (gutendexId=9999): Book title is required"
├── Livro 4 ✅ → salvo (id=103)
├── ...
└── Livro 32 ✅ → salvo (id=132)

Resultado: 31 livros salvos (apenas o inválido foi pulado)
```

**Impacto no endpoint `POST /books` (controlador existente):**
- Antes: falha de 1 livro → HTTP 500 + rollback total
- Agora: retorna `204 No Content` com IDs dos livros criados (pode vir com menos IDs que o esperado)
- **Refinamento positivo** — endpoint mais resiliente

---

### 5️⃣ `application/book/SyncAllGutendexBooksUseCase.java` — Use Case Puro 🆕

**Camada de Aplicação** — POJO puro, sem anotações Spring.

```java
package com.pucsp.alexandria.application.book;

import com.pucsp.alexandria.application.book.dto.CreateBookInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SyncAllGutendexBooksUseCase {

    private static final Logger log = LoggerFactory.getLogger(SyncAllGutendexBooksUseCase.class);
    private static final long PAGE_DELAY_MS = 500L;

    private final CreateBookUseCase createBookUseCase;

    public SyncAllGutendexBooksUseCase(CreateBookUseCase createBookUseCase) {
        this.createBookUseCase = createBookUseCase;
    }

    public void execute() {
        int currentPage = 1;

        while (true) {
            try {
                CreateBookInput input = new CreateBookInput(currentPage);
                var output = createBookUseCase.execute(input);
                log.info("Página {} sincronizada. {} livros criados.",
                        currentPage, output.ids().size());
                currentPage++;
                Thread.sleep(PAGE_DELAY_MS);

            } catch (RuntimeException e) {
                String msg = e.getMessage();

                // Fim normal da Gutendex — não há mais páginas
                if (msg != null && msg.contains("Page not found in Gutendex")) {
                    log.info("Fim da sincronização: {}", msg);
                    break;
                }

                // Erro transiente (timeout, banco, etc.) — loga e pula para a próxima
                log.error("Erro na página {}, pulando para a próxima: {}", currentPage, msg);
                currentPage++;
                continue;

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Sincronização interrompida externamente na página {}.", currentPage);
                break;
            }
        }

        log.info("Sincronização completa. Última página processada: {}.", currentPage - 1);
    }
}
```

---

### 6️⃣ `adapter/in/job/SyncGutendexJobService.java` — Wrapper `@Async` 🆕

**Camada de Infraestrutura (Adapter).** Aqui mora o `@Async`, não no Use Case.

```java
package com.pucsp.alexandria.adapter.in.job;

import com.pucsp.alexandria.application.book.SyncAllGutendexBooksUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class SyncGutendexJobService {

    private static final Logger log = LoggerFactory.getLogger(SyncGutendexJobService.class);

    private final SyncAllGutendexBooksUseCase syncAllGutendexBooksUseCase;

    public SyncGutendexJobService(SyncAllGutendexBooksUseCase syncAllGutendexBooksUseCase) {
        this.syncAllGutendexBooksUseCase = syncAllGutendexBooksUseCase;
    }

    @Async("gutendexSyncExecutor")
    public void triggerSync() {
        log.info("Job de sincronização iniciado em thread separada.");
        syncAllGutendexBooksUseCase.execute();
        log.info("Job de sincronização finalizado.");
    }
}
```

---

### 7️⃣ `adapter/in/rest/JobController.java` — Endpoint Gatilho 🆕

Retorna `202 Accepted` ou `429 Too Many Requests`.

```java
package com.pucsp.alexandria.adapter.in.rest;

import com.pucsp.alexandria.adapter.in.job.SyncGutendexJobService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.core.task.TaskRejectedException;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private static final Logger log = LoggerFactory.getLogger(JobController.class);

    private final SyncGutendexJobService syncGutendexJobService;

    public JobController(SyncGutendexJobService syncGutendexJobService) {
        this.syncGutendexJobService = syncGutendexJobService;
    }

    @PostMapping("/sync-gutendex")
    public ResponseEntity<Void> triggerSync() {
        try {
            syncGutendexJobService.triggerSync();
            return ResponseEntity.accepted().build();
        } catch (TaskRejectedException e) {
            log.warn("Job de sincronização já está em execução. Requisição rejeitada.");
            return ResponseEntity.status(429).build(); // Too Many Requests
        }
    }
}
```

---

### 8️⃣ `config/BeanConfiguration.java` — Registrar Use Case 🛠️

Adicionar ao final da classe:

```java
@Bean
public SyncAllGutendexBooksUseCase syncAllGutendexBooksUseCase(
        CreateBookUseCase createBookUseCase) {
    return new SyncAllGutendexBooksUseCase(createBookUseCase);
}
```

---

### 9️⃣ `config/SecurityConfig.java` — Liberar Rota 🛠️

Adicionar `"/api/jobs/**"` às rotas públicas:

```java
.requestMatchers("/auth/**", "/api/jobs/**").permitAll()
```

---

### 🔟 `resources/logback-spring.xml` — Logging do Job 🆕

**Problema:** Sem configuração, os logs do job se misturam com os demais no stdout. Em Docker/EC2, fica difícil isolar.

**Solução:** Arquivo de log separado para o job, com rolagem diária e 30 dias de retenção.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>

    <!-- Propriedades -->
    <property name="LOG_PATH" value="${LOG_PATH:-logs}" />
    <property name="JOB_LOG_FILE" value="${LOG_PATH}/gutendex-sync" />

    <!-- Appender do Console (comportamento padrão) -->
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <!-- Appender de Arquivo para o Job de Sincronização -->
    <appender name="JOB_FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>${JOB_LOG_FILE}.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>${JOB_LOG_FILE}.%d{yyyy-MM-dd}.log</fileNamePattern>
            <maxHistory>30</maxHistory>
        </rollingPolicy>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <!-- Logger específico para o job (arquivo + console) -->
    <logger name="com.pucsp.alexandria.adapter.in.job" level="INFO" additivity="false">
        <appender-ref ref="JOB_FILE" />
        <appender-ref ref="CONSOLE" />
    </logger>

    <logger name="com.pucsp.alexandria.application.book.SyncAllGutendexBooksUseCase" level="INFO" additivity="false">
        <appender-ref ref="JOB_FILE" />
        <appender-ref ref="CONSOLE" />
    </logger>

    <logger name="com.pucsp.alexandria.application.book.CreateBookUseCase" level="INFO" additivity="false">
        <appender-ref ref="JOB_FILE" />
        <appender-ref ref="CONSOLE" />
    </logger>

    <!-- Root: console apenas -->
    <root level="INFO">
        <appender-ref ref="CONSOLE" />
    </root>

</configuration>
```

**Resultado:**

```
logs/
├── gutendex-sync.log              ← arquivo atual
├── gutendex-sync.2025-03-21.log   ← logs de 21/03
├── gutendex-sync.2025-03-14.log   ← logs de 14/03
└── ... (30 dias de histórico)
```

**Como acessar:**

| Ambiente | Comando |
|----------|---------|
| Docker | `docker exec <container> cat logs/gutendex-sync.log` |
| Docker Compose | `docker-compose exec alexandria-api cat logs/gutendex-sync.log` |
| Volume Docker | Mapear `./logs:/app/logs` no `docker-compose.yml` |

---

## 📊 Separação de Responsabilidades

| Camada | Classe | Responsabilidade | Spring? |
|--------|--------|------------------|---------|
| **Application** | `SyncAllGutendexBooksUseCase` | Iteração, delay, condição de parada | ❌ POJO |
| **Application** | `CreateBookUseCase` (modificado) | Processamento de página c/ try-catch por livro | 🛠️ `@Transactional` |
| **Adapter (out)** | `GutendexClient` (modificado) | Consulta Gutendex sem filtro de idioma | ✅ `@Component` |
| **Adapter (in/job)** | `SyncGutendexJobService` | Wrapper `@Async` | ✅ `@Service` + `@Async` |
| **Adapter (in/rest)** | `JobController` | Endpoint `POST /api/jobs/sync-gutendex` | ✅ `@RestController` |
| **Config** | `AsyncConfig` | Pool de threads | ✅ `@Configuration` |
| **Config** | `BeanConfiguration` | Registro do Use Case | ✅ `@Configuration` |
| **Config** | `SecurityConfig` | Permissão da rota | ✅ `@Configuration` |
| **Config** | `logback-spring.xml` | Logging do job | ✅ Infraestrutura |

---

## 🔄 Fluxo Completo de Execução

```
EventBridge Scheduler
    │  cron(0 3 ? * SUN *) — domingo 03:00 BRT
    │
    ▼  POST /api/jobs/sync-gutendex
JobController
    │
    ├── Se thread livre       → 202 Accepted
    ├── Se thread ocupada     → 429 Too Many Requests
    │
    ▼  (chama)
SyncGutendexJobService  (@Async)
    │  log: "Job iniciado" → gutendex-sync.log
    │
    ▼  (delega, thread separada)
SyncAllGutendexBooksUseCase  (POJO puro)
    │
    ├── createBookUseCase.execute(page=1)
    │   ├── Livro 1 ✅
    │   ├── Livro 2 ❌ → warn → continua
    │   ├── Livro 3 ✅
    │   └── log: "Página 1. 31 livros criados."
    │   Thread.sleep(500ms)
    │
    ├── createBookUseCase.execute(page=2)
    │   └── log: "Página 2. 32 livros criados."
    │   Thread.sleep(500ms)
    │
    ├── ... 
    │
    └── createBookUseCase.execute(page=N)
        └── "Page not found" → break
        └── log: "Sincronização completa. Última página: N"

SyncGutendexJobService
    └── log: "Job finalizado." → gutendex-sync.log
```

---

## 🧵 Análise das Opções de Fila (Queue Capacity)

### Contexto

O pool tem **1 thread**. O parâmetro `queueCapacity` controla quantas tarefas podem esperar na fila quando a thread já está ocupada.

```
                    ┌──────────────────┐
  Requisição 1 ───→ │  Thread ocupada   │ ← processando página 5
                    └──────────────────┘
                            ↑
  Requisição 2 ───→ ┌──────┴───────┐
                    │    Fila       │ ← tarefas esperando
                    └──────────────┘
```

---

### Opção A: `queueCapacity = 0` ← RECOMENDADA

```java
executor.setQueueCapacity(0);
```

| Situação | Comportamento |
|----------|--------------|
| Thread livre | Executa imediatamente ✅ |
| Thread ocupada | **Rejeita** com `TaskRejectedException` |

**Prós:**
- Zero acúmulo — se o job já roda, nova requisição é rejeitada na hora
- Sem risco de execuções atrasadas e obsoletas
- Mínimo consumo de memória

**Contras:**
- EventBridge receberia 500... **mas o controller trata** e retorna **429** em vez de 500

**Mitigação já implementada no `JobController`:** O catch de `TaskRejectedException` retorna `429 Too Many Requests`, que é semanticamente correto e evita que o EventBridge interprete como falha do sistema.

---

### Opção B: `queueCapacity = 1`

```java
executor.setQueueCapacity(1);
```

| Situação | Comportamento |
|----------|--------------|
| Thread livre | Executa ✅ |
| Thread ocupada, fila vazia | **Enfileira** ⏳ (1 tarefa na fila) |
| Thread ocupada, fila cheia | **Rejeita** ❌ |

**Prós:** Requisição pode "esperar a vez" em vez de ser rejeitada.

**Contras:**
- Tarefa enfileirada executa com atraso — se o job demorar 30 min, executa 30+ min depois
- Se EventBridge retentar em 1 min, a fila enche e a segunda retentativa é rejeitada do mesmo jeito
- Consumo de memória extra irrelevante (1 tarefa)

**Tradeoff:** Pouco benefício prático. Schedule semanal tem janela enorme entre triggers.

---

### Opção C: `queueCapacity = Integer.MAX_VALUE` (fila ilimitada)

```java
executor.setQueueCapacity(Integer.MAX_VALUE);
```

| Situação | Comportamento |
|----------|--------------|
| Thread livre | Executa ✅ |
| Thread ocupada | **Sempre enfileira** (nunca rejeita) |

**Prós:** Nenhuma requisição rejeitada.

**Contras:**
- Se EventBridge retentar N vezes, fila acumula N tarefas que executarão **sequencialmente** após o job atual
- Execução de tarefas obsoletas processando dados já desatualizados
- Consumo de memória proporcional ao número de tarefas acumuladas

---

### Recomendação Final

**`queueCapacity = 0`** com tratamento de `429 Too Many Requests`.

| Critério | Nota |
|----------|------|
| **Simplicidade** | 🟢 Máxima — sem estado de fila para gerenciar |
| **Correção semântica** | 🟢 429 é mais correto que 500 ou executar job obsoleto |
| **Performance** | 🟢 Zero overhead de memória para fila |
| **Schedule semanal** | 🟢 Risco de concorrência é insignificante |

---

## ☁️ Fase 2: Configurações AWS (EventBridge Scheduler)

### 2.1 Criar API Destination

| Campo | Valor |
|-------|-------|
| **Name** | `GutendexSyncEndpoint` |
| **Endpoint** | `https://<seu-dominio>/api/jobs/sync-gutendex` |
| **HTTP Method** | `POST` |
| **Connection** | Criar nova (`ConexaoGutendexSync`) |
| **Authorization** | `API Key` (X-Dummy: 123) ou `Basic Auth` se necessário |

### 2.2 Criar Schedule

| Campo | Valor |
|-------|-------|
| **Name** | `SincronizacaoSemanalGutendex` |
| **Pattern** | `Recurring schedule` → `Cron-based` |
| **Cron** | `cron(0 3 ? * SUN *)` |
| **Timezone** | `America/Sao_Paulo` |
| **Target** | API Destination `GutendexSyncEndpoint` |
| **Payload** | Vazio |
| **Permissions** | `Create new role` |

---

## 🧪 Testes

### Testes para `SyncAllGutendexBooksUseCase`

Seguindo o padrão dos testes existentes (`@ExtendWith(MockitoExtension.class)` + `@Mock` + `@InjectMocks`):

```java
@ExtendWith(MockitoExtension.class)
class SyncAllGutendexBooksUseCaseTest {

    @Mock
    private CreateBookUseCase createBookUseCase;

    @InjectMocks
    private SyncAllGutendexBooksUseCase syncUseCase;

    @Test
    void shouldIterateUntilPageNotFound() {
        when(createBookUseCase.execute(any(CreateBookInput.class)))
            .thenReturn(new CreateBookOutput(List.of(1L, 2L)))   // página 1
            .thenReturn(new CreateBookOutput(List.of(3L)))       // página 2
            .thenThrow(new RuntimeException("Page not found in Gutendex: 3"));

        syncUseCase.execute();

        verify(createBookUseCase, times(3)).execute(any(CreateBookInput.class));
    }

    @Test
    void shouldSkipPageOnTransientError() {
        when(createBookUseCase.execute(any(CreateBookInput.class)))
            .thenReturn(new CreateBookOutput(List.of(1L)))       // página 1
            .thenThrow(new RuntimeException("Read timed out"))   // página 2 - erro transiente
            .thenReturn(new CreateBookOutput(List.of(2L)))       // página 3
            .thenThrow(new RuntimeException("Page not found in Gutendex: 4"));

        syncUseCase.execute();

        verify(createBookUseCase, times(4)).execute(any(CreateBookInput.class));
    }
}
```

### Testes para `CreateBookUseCase` (nova resiliência)

```java
@Test
void shouldContinueWhenSingleBookFails() {
    BookData validBook = new BookData(-1L, 100L, "Dom Casmurro",
        "Machado de Assis",
        List.of(new AuthorData("Machado de Assis", 1839, 1908)),
        "url", "url", "pt", "Fiction", 5000);

    BookData invalidBook = new BookData(-1L, 200L, "",
        "Unknown", List.of(), null, null, null, null, null);

    when(bookApiClient.getPage(1)).thenReturn(List.of(validBook, invalidBook));
    when(bookRepository.existsByGutendexId(100L)).thenReturn(false);
    when(bookRepository.existsByGutendexId(200L)).thenReturn(false);

    Author author = Author.restore(1L, "Machado de Assis", 1839, 1908);
    when(authorRepository.findByName("Machado de Assis")).thenReturn(Optional.of(author));

    Book saved = Book.restore(1L, "Dom Casmurro", Set.of(1L), 100L,
        "url", "url", "pt", "Fiction", 5000, null, BookSource.GUTENDEX);
    when(bookRepository.save(any(Book.class))).thenReturn(saved);

    CreateBookOutput output = createBookUseCase.execute(new CreateBookInput(1));

    assertEquals(1, output.ids().size());  // só o válido foi criado
}
```

### Testes para `JobController`

Seguindo o padrão dos testes de integração existentes (`@SpringBootTest` + `@AutoConfigureMockMvc` + `@MockitoBean`):

```java
@SpringBootTest(properties = {
    "jwt.secret=test-secret-key-for-tests-min-256-bits",
    "jwt.expiration-ms=86400000"
})
@AutoConfigureMockMvc
class JobControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SyncGutendexJobService syncGutendexJobService;

    @Test
    void shouldReturn202WhenTriggered() throws Exception {
        // O service é void e executa assíncrono — só verificamos o status
        mockMvc.perform(post("/api/jobs/sync-gutendex"))
                .andExpect(status().isAccepted());
    }
}
```

---

## 📝 Linguagem: Antes vs Depois

| Aspecto | Antes (`languages=pt`) | Depois (sem filtro) |
|---------|----------------------|-------------------|
| **Idiomas indexados** | Apenas português | Todos (pt, en, fr, de, es, etc.) |
| **Vol. por página** | ~32 livros | ~32 livros (mesmo) |
| **Total de páginas** | ~300 (est.) | **Milhares** (~70k livros na Gutendex) |
| **Tempo estimado** | ~2,5h | **Muito maior** (horas ou dias) |
| **Coluna `languages`** | Sempre `"pt"` | Variável: `"pt"`, `"en"`, `"pt,en"`, etc. |

---

## ✅ Resumo Final

| Funcionalidade | Tipo | Arquivos |
|---------------|------|----------|
| Job agendado EventBridge | 🆕 | `JobController`, `SyncGutendexJobService`, `SyncAllGutendexBooksUseCase` |
| Async + pool de threads | 🆕 | `AsyncConfig`, `AlexandriaApplication` |
| Concorrência: `queueCapacity=0` + `429` | 🆕 | `AsyncConfig`, `JobController` |
| Resiliência por livro (try-catch) | 🛠️ | `CreateBookUseCase` |
| Indexar **todos os idiomas** | 🛠️ | `GutendexClient` |
| Logging do job em arquivo | 🆕 | `logback-spring.xml` |
| Liberar rota `/api/jobs/**` | 🛠️ | `SecurityConfig` |
| Registrar Use Case | 🛠️ | `BeanConfiguration` |

**Total: 5 arquivos novos + 5 modificados = 10 arquivos**

---

## 🧪 Validação (Rodar Aplicação e Testes)

Após implementar todas as alterações, seguir esta ordem de validação:

---

### Passo 1: Rodar os testes existentes (antes de qualquer alteração)

Garantir que a suíte atual está verde antes de começar.

```bash
# Na branch develop, antes de criar a feature/event-bridge
./mvnw test
```

**Resultado esperado:** Todos os testes existentes passando (verde).

---

### Passo 2: Rodar os testes após as alterações

Após implementar todas as classes novas e modificações, executar a suíte completa:

```bash
# Na branch feature/event-bridge
./mvnw test
```

**O que verificar:**
- ✅ Testes existentes continuam passando (nada quebrou)
- ✅ `GutendexClientTest` — continua verde (usa `anyString()`, não depende do parâmetro `languages`)
- ✅ `CreateBookUseCaseTest` — existente continua passando (o comportamento do `execute()` não mudou na interface pública, só ficou mais resiliente internamente)
- ✅ `BookApiClientImplTest` — continua verde

---

### Passo 3: Atualizar testes existentes (se necessário)

Avaliar se o `CreateBookUseCaseTest` precisa de ajustes:

| Teste existente | Impacto da mudança | Ação |
|----------------|-------------------|------|
| `shouldCreateBooksFromGutendexPage` | Nenhum — fluxo feliz inalterado | ✅ Mantido |
| `shouldSkipExistingGutendexBooks` | Nenhum — continue para duplicatas mantido | ✅ Mantido |
| `shouldReuseExistingAuthor` | Nenhum | ✅ Mantido |
| `shouldThrowExceptionWhenPageNotFound` | Nenhum — exceção ainda é lançada | ✅ Mantido |
| `shouldHandleEmptyResultFromApi` | Nenhum | ✅ Mantido |

**Conclusão:** Nenhum teste existente precisa ser alterado. As mudanças foram puramente aditivas (try-catch interno não altera o contrato público).

---

### Passo 4: Criar os novos testes

Criar os arquivos de teste conforme especificado na seção de Testes acima:

```bash
# Localização dos novos arquivos de teste
src/test/java/com/pucsp/alexandria/application/book/SyncAllGutendexBooksUseCaseTest.java
src/test/java/com/pucsp/alexandria/adapter/in/rest/JobControllerIntegrationTest.java
```

E adicionar o novo método de teste no `CreateBookUseCaseTest`:
- `shouldContinueWhenSingleBookFails()`

Rodar novamente:

```bash
./mvnw test
```

**Resultado esperado:** Todos os testes passando, incluindo os novos.

---

### Passo 5: Compilar o projeto

Verificar se o projeto compila sem erros:

```bash
./mvnw compile
```

---

### Passo 6: Rodar a aplicação localmente

Iniciar a aplicação para validar que sobe sem erros:

```bash
# Terminal 1: Iniciar a aplicação (precisa do MySQL rodando)
./mvnw spring-boot:run
```

**O que verificar nos logs de startup:**
```
2025-... INFO  o.s.s.c.ThreadPoolTaskExecutor - Initializing ExecutorService 'gutendexSyncExecutor'
2025-... INFO  c.p.a.AlexandriaApplication - Started AlexandriaApplication in X.XXX seconds
```

---

### Passo 7: Testar o endpoint manualmente

Com a aplicação rodando, testar o endpoint gatilho:

```bash
# Requisição sem autenticação — deve retornar 202 Accepted
curl -X POST http://localhost:8080/api/jobs/sync-gutendex -v
```

**Resultado esperado:**
```
< HTTP/1.1 202 Accepted
```

```bash
# Segunda requisição imediata (job ainda rodando) — deve retornar 429
curl -X POST http://localhost:8080/api/jobs/sync-gutendex -v
```

**Resultado esperado:**
```
< HTTP/1.1 429 Too Many Requests
```

---

### Passo 8: Verificar os logs do job

Com o job rodando, verificar o arquivo de log:

```bash
cat logs/gutendex-sync.log
```

**Exemplo de saída esperada:**
```
2025-... [gutendex-sync-1] INFO  c.p.a.a.b.SyncAllGutendexBooksUseCase - Página 1 sincronizada. 32 livros criados.
2025-... [gutendex-sync-1] INFO  c.p.a.a.b.SyncAllGutendexBooksUseCase - Página 2 sincronizada. 32 livros criados.
```

---

### Passo 9: Commit e Push

Após todas as validações:

```bash
git add .
git commit -m "feat: add weekly Gutendex sync job via EventBridge

- Add SyncAllGutendexBooksUseCase with page iteration, delay and error handling
- Add SyncGutendexJobService as @Async adapter wrapper
- Add JobController endpoint POST /api/jobs/sync-gutendex
- Add AsyncConfig with dedicated thread pool (queueCapacity=0)
- Add @EnableAsync to AlexandriaApplication
- Remove languages=pt filter from GutendexClient (index all languages)
- Add per-book error resilience to CreateBookUseCase
- Add logback-spring.xml with separate log file for the job
- Allow /api/jobs/** as public route in SecurityConfig
- Register SyncAllGutendexBooksUseCase in BeanConfiguration"

git push origin feature/event-bridge
```

---

## 📝 Observações Finais

- **Volume estimado:** Sem filtro de idioma, a Gutendex tem ~70k livros → ~2.200 páginas. No cenário realista (~2s por página entre HTTP + DB + delay), ~1h13min de execução. O delay de 500ms é apenas ~25% do tempo total por página
- **Logs do job:** `logs/gutendex-sync.log` com rolagem diária (30 dias)
- **Concorrência:** Pool de 1 thread, `queueCapacity=0`, retorno `429` se ocupado
- **Delay entre páginas:** 500ms (constante `PAGE_DELAY_MS`) — evita rate limit da Gutendex
- **Variável de ambiente `LOG_PATH`:** permite customizar diretório de logs (padrão: `logs/`)
