# 📋 Resumo da Implementação — Job Agendado Gutendex via EventBridge

> **Data:** Maio 2026
> **Branch:** `feature/event-bridge`
> **Objetivo:** Sincronizar todos os livros da Gutendex (todos os idiomas) automaticamente todo domingo às 03:00 BRT

---

## 🧠 Decisão Arquitetural Importante

**Problema:** Instruções iniciais sugeriam `@Async` no `SyncAllGutendexBooksUseCase`, mas isso viola a Clean Architecture (Use Case não pode ter anotações Spring).

**Solução adotada:**
| Camada | Classe | Anotações Spring? |
|--------|--------|-------------------|
| **Application** | `SyncAllGutendexBooksUseCase` | ❌ POJO puro |
| **Adapter (in/job)** | `SyncGutendexJobService` | ✅ `@Service` + `@Async` |

O `@Async` fica no **Adapter** (infraestrutura), não no Use Case.

---

## 📁 Arquivos — 5 Criados + 5 Modificados

### 🆕 Arquivos Novos

| # | Arquivo | Caminho | Função |
|---|---------|---------|--------|
| 1 | `SyncAllGutendexBooksUseCase.java` | `application/book/` | POJO puro: itera páginas com delay 500ms, trata fim da Gutendex e erros transientes |
| 2 | `SyncGutendexJobService.java` | `adapter/in/job/` | Wrapper `@Service` + `@Async("gutendexSyncExecutor")` que chama o Use Case |
| 3 | `JobController.java` | `adapter/in/rest/` | Endpoint `POST /api/jobs/sync-gutendex` — retorna `202 Accepted` ou `429 Too Many Requests` |
| 4 | `AsyncConfig.java` | `config/` | Pool de 1 thread com `queueCapacity=0` (rejeita execução concorrente) |
| 5 | `logback-spring.xml` | `resources/` | Log do job em arquivo separado (`logs/gutendex-sync.log`) com rolagem diária e 30 dias de retenção |

### 🛠️ Arquivos Modificados

| # | Arquivo | O que mudou |
|---|---------|-------------|
| 1 | `AlexandriaApplication.java` | Adicionado `@EnableAsync` |
| 2 | `BeanConfiguration.java` | Registrado `SyncAllGutendexBooksUseCase` como `@Bean` |
| 3 | `SecurityConfig.java` | Rota `/api/jobs/**` adicionada como pública (`permitAll()`) |
| 4 | `CreateBookUseCase.java` | Adicionado **try-catch por livro** — falha de 1 livro não quebra a página inteira |
| 5 | `GutendexClient.java` | Removido filtro `languages=pt` — agora indexa **todos os idiomas** |

### 🧪 Testes Criados

| Arquivo | Caminho | O que testa |
|---------|---------|-------------|
| `SyncAllGutendexBooksUseCaseTest.java` | `test/.../application/book/` | Iteração até fim, erro transiente, skip de página |
| `JobControllerIntegrationTest.java` | `test/.../adapter/in/rest/` | Retorno `202 Accepted` no endpoint |

---

## 🔄 Fluxo Completo de Execução

```
EventBridge Scheduler (cron: 0 3 ? * SUN *)
    │  POST https://api.bibliotecaalexandria.com.br/api/jobs/sync-gutendex
    ▼
JobController
    │
    ├── Thread livre   → 202 Accepted (job disparado)
    ├── Thread ocupada → 429 Too Many Requests
    ▼
SyncGutendexJobService (@Async, pool dedicada)
    │  log: "Job de sincronização iniciado"
    ▼
SyncAllGutendexBooksUseCase (POJO puro)
    │
    ├── Página 1 → CreateBookUseCase → 32 livros (try-catch por livro)
    │   Thread.sleep(500ms)
    ├── Página 2 → CreateBookUseCase → 32 livros
    │   Thread.sleep(500ms)
    ├── ...
    │
    └── "Page not found" → break
    │  log: "Sincronização completa. Última página: N"
    ▼
SyncGutendexJobService
    │  log: "Job de sincronização finalizado."
```

---

## ☁️ Configuração AWS EventBridge

### Etapa 1: Connection
| Campo | Valor |
|-------|-------|
| **Nome** | `ConexaoGutendexSync` |
| **Tipo** | `API Key` |
| **API Key Name** | `X-API-Key` |
| **API Key Value** | `dummy-key-123` |

### Etapa 2: API Destination
| Campo | Valor |
|-------|-------|
| **Nome** | `GutendexSyncEndpoint` |
| **Endpoint** | `https://api.bibliotecaalexandria.com.br/api/jobs/sync-gutendex` |
| **Método HTTP** | `POST` |
| **Connection** | `ConexaoGutendexSync` |

### Etapa 3: Schedule
| Campo | Valor |
|-------|-------|
| **Nome** | `SincronizacaoSemanalGutendex` |
| **Tipo** | Recurring → Cron-based |
| **Cron** | `cron(0 3 ? * SUN *)` |
| **Timezone** | `America/Sao_Paulo` |
| **Target** | `GutendexSyncEndpoint` (API Destination) |
| **Payload** | Vazio |
| **Retry** | 3 tentativas, 60 min |
| **Região** | `us-east-2` |
| **Status** | ✅ Enabled |

---

## ⚙️ Comportamento da Concorrência

| Situação | Comportamento |
|----------|---------------|
| Thread livre | Job executa imediatamente ✅ |
| Thread ocupada (job já rodando) | Requisição **rejeitada** com `429 Too Many Requests` ❌ |
| Fila | `queueCapacity=0` — zero acúmulo |

**Justificativa:** Schedule semanal (domingo 03:00). Risco de concorrência é insignificante. Melhor rejeitar do que acumular tarefas obsoletas.

---

## 📝 Logs

| Local | Arquivo |
|-------|---------|
| Console | stdout (comportamento padrão) |
| Arquivo separado | `logs/gutendex-sync.log` |

Rolagem diária com 30 dias de retenção:

```
logs/
├── gutendex-sync.log              ← arquivo atual
├── gutendex-sync.2026-05-20.log
├── gutendex-sync.2026-05-13.log
└── ...
```

```bash
# Ver logs
docker exec alexandria-backend cat logs/gutendex-sync.log
```

---

## 🌐 Mudança no Escopo: Todos os Idiomas

| Aspecto | Antes (`languages=pt`) | Depois (sem filtro) |
|---------|------------------------|---------------------|
| Idiomas indexados | Apenas português | Todos (pt, en, fr, de, es, etc.) |
| Total estimado | ~10k livros | ~70k livros |
| Tempo estimado | ~2,5h | ~1h+ (delay 500ms é ~25% do tempo real por página) |

---

## ✅ Validação Realizada

| Teste | Resultado |
|-------|-----------|
| `curl -X POST https://api.bibliotecaalexandria.com.br/api/jobs/sync-gutendex` | ✅ **202 Accepted** |
| Compilação (`./mvnw compile`) | ✅ Verde |
| Testes unitários | ✅ Todos passando |
| Aplicação startup | ✅ Log: `Initializing ExecutorService 'gutendexSyncExecutor'` |
| Teste de concorrência (2º curl simultâneo) | ✅ **429 Too Many Requests** |

---

## 📊 Responsabilidades por Camada

| Camada | Classe | Responsabilidade |
|--------|--------|------------------|
| **Application** | `SyncAllGutendexBooksUseCase` | Iteração, delay, condição de parada |
| **Application** | `CreateBookUseCase` (modificado) | Processamento de página com try-catch por livro |
| **Adapter (out)** | `GutendexClient` (modificado) | Consulta Gutendex sem filtro de idioma |
| **Adapter (in/job)** | `SyncGutendexJobService` | Wrapper `@Async` |
| **Adapter (in/rest)** | `JobController` | Endpoint `POST /api/jobs/sync-gutendex` |
| **Config** | `AsyncConfig` | Pool de threads dedicado |
| **Config** | `BeanConfiguration` | Registro do Use Case |
| **Config** | `SecurityConfig` | Permissão da rota |
| **Config** | `logback-spring.xml` | Logging do job em arquivo |

---

**Total: 5 arquivos novos + 5 modificados + 2 testes novos + 1 schedule EventBridge = Job semanal rodando! 🚀**
