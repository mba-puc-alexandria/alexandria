# 🧵 Análise Detalhada das Opções de Controle de Concorrência

> Complemento ao plano em `markdown/plano_event_bridge_sync_gutendex.md`

---

## Contexto do Problema

O `AsyncConfig` define um `ThreadPoolTaskExecutor` com **1 thread** para o job de sincronização Gutendex. O parâmetro `queueCapacity` determina o que acontece quando uma segunda requisição chega enquanto a thread única já está ocupada processando.

```
                    ┌─────────────────────────────┐
  Requisição 1 ───→ │  Thread "gutendex-sync-1"    │ ← ocupada (~2,5h)
                    └─────────────────────────────┘
                              ↑
  Requisição 2 ───→ ┌─────────┴──────────┐
                    │   queueCapacity=?   │ ← o que fazer?
                    └────────────────────┘
```

**Cenário real:** Schedule semanal (`cron(0 3 ? * SUN *)`) → às 03:00 de domingo o job inicia e roda por ~2,5h. A chance de outra requisição chegar durante esse período é **baixíssima**, mas pode ocorrer se:
- Alguém disparar manualmente o endpoint para testar
- O EventBridge retentar (se configurado com retry)
- Um monitoramento externo fizer health check do endpoint

---

## Opção A: `queueCapacity = 0` (Rejeição Imediata)

```java
executor.setCorePoolSize(1);
executor.setMaxPoolSize(1);
executor.setQueueCapacity(0);
```

### Como funciona internamente

O `ThreadPoolTaskExecutor` delega para um `ThreadPoolExecutor` do JDK. Com `queueCapacity=0`, o `ThreadPoolExecutor` usa um `SynchronousQueue` — uma fila sem capacidade de armazenamento. Cada tarefa precisa de uma thread disponível **no momento da submissão**.

```
Tempo:  t0          t1           t2           t3
        │           │            │            │
Req 1:  │─submit──→ │─inicia────→│─processa──→│─termina
        │           │            │            │
Req 2:  │           │─submit────→│ REJECTED!  │
        │           │            │            │
                   ↑ thread ocupada processando página 1
```

### Fluxo da exceção no Spring

```
JobController.triggerSync()
  └── syncGutendexJobService.triggerSync()     ← método @Async
        └── (Spring AOP intercepta)
              └── executor.submit(callable)     ← tenta submeter ao pool
                    └── SynchronousQueue.offer()
                          └── false (thread ocupada)
                                └── new TaskRejectedException("Executor [gutendexSyncExecutor] did not accept task")
                                      └── propaga para o controller
                                            └── catch → return 429
```

**⚠️ Detalhe importante:** O `@Async` lança a `TaskRejectedException**de forma síncrona** — ou seja, ela ocorre no momento da chamada, dentro da thread do controller, ANTES de qualquer processamento assíncrono. É por isso que o `try-catch` no controller funciona.

### O que o EventBridge faz com 429?

| Comportamento do EventBridge | 429 | 500 |
|------------------------------|-----|-----|
| **Retry automático** | Depende da config. Por padrão, EventBridge **não retenta** para 4xx | Por padrão, EventBridge **pode retentar** para 5xx |
| **Interpretação** | "Serviço ocupado, não retentar" | "Serviço falhou, talvez tente de novo" |
| **Log no CloudWatch** | Conta como falha | Conta como falha |

**Conclusão:** 429 é **mais seguro** que 500 porque evita que o EventBridge enfileire retentativas desnecessárias durante a execução do job.

### Prós detalhados

| Pró | Detalhamento |
|-----|-------------|
| 🚫 **Zero acúmulo** | Nenhuma tareha fica na fila. O job nunca fica "para trás" |
| 🧠 **Previsível** | Comportamento determinístico: ou roda agora, ou é rejeitado |
| 💾 **Memória mínima** | `SynchronousQueue` não aloca nós internos (diferente de `LinkedBlockingQueue`) |
| ⏰ **Sem execução obsoleta** | Nunca vai rodar um job disparado horas atrás com dados desatualizados |

### Contras detalhados

| Contra | Detalhamento | Peso |
|--------|-------------|------|
| ❌ **Perda do trigger** | A requisição que gerou o 429 é perdida. Se era a única do schedule semanal, o job daquela semana não roda | 🔴 Crítico |
| ❌ **Falso positivo** | Se o job terminar 1ms depois da rejeição, o trigger foi perdido por muito pouco | 🟡 Moderado |

### Mitigação para o contra crítico

O schedule semanal roda às 03:00. Se por acaso:
1. O job semanal está rodando (03:00-05:30)
2. Alguém faz uma requisição manual durante esse período
3. → 429

**O schedule semanal NÃO é perdido** porque ele já está rodando. O 429 afeta apenas a **requisição concorrente**, não o job em execução.

O risco real seria se o job **não tivesse iniciado** e uma requisição concorrente bloqueasse a única thread. Mas com `queueCapacity=0`, se a thread está livre, a requisição **executa imediatamente**.

---

## Opção B: `queueCapacity = 1` (Fila Pequena)

```java
executor.setCorePoolSize(1);
executor.setMaxPoolSize(1);
executor.setQueueCapacity(1);
```

### Como funciona internamente

O `ThreadPoolExecutor` usa um `LinkedBlockingQueue` com capacidade 1. Uma tarefa pode aguardar na fila.

```
Tempo:  t0          t1           t2           t3           t4
        │           │            │            │            │
Req 1:  │─submit──→ │─inicia────→│─processa──→│─processa──→│─termina
        │           │            │            │            │
Req 2:  │           │─submit────→│ (fila)     │ (fila)     │─INICIA────→
        │           │            │            │            │
Req 3:  │           │            │─submit────→│ REJECTED!  │
        │           │            │            │            │
                   ↑ thread ocupada          ↑ fila ocupada
                                              pela Req 2
```

### Cenários com `queueCapacity = 1`

**Cenário 1: Job rápido (improvável)**
- Job demora 5 minutos
- Req 2 chega 1 minuto após o início → fica na fila 4 minutos → executa
- Resultado: duas execuções do job em sequência. A segunda processa páginas já sincronizadas (pula duplicatas)

**Cenário 2: Job lento (realista)**
- Job demora 2,5 horas
- Req 2 chega 1 minuto após o início → fica na fila ~2h29min → executa
- Req 3 chega durante a execução da Req 2 → rejeitada (fila ocupada)

**Cenário 3: EventBridge com retry**
- EventBridge configurado com retry em 5 minutos
- Job iniciou às 03:00, Req 2 (retry) chega às 03:05 → enfileira
- Job termina às 05:30, Req 2 inicia às 05:30
- Req 2 processa dados de 2,5h atrás → pode encontrar páginas já sincronizadas

### Prós detalhados

| Pró | Detalhamento |
|-----|-------------|
| ✅ **Não perde o trigger** | A requisição aguarda na fila em vez de ser rejeitada |
| ✅ **Janela curta** | Máximo 1 tarefa enfileirada — não há crescimento indefinido |

### Contras detalhados

| Contra | Detalhamento | Peso |
|--------|-------------|------|
| ❌ **Execução obsoleta** | Tarefa enfileirada roda com dados potencialmente desatualizados | 🟡 Moderado |
| ❌ **Atraso imprevisível** | Se o job demorar 2,5h, a tarefa enfileirada executa 2,5h depois | 🟡 Moderado |
| ❌ **Complexidade mental** | Comportamento não é binário (roda/não roda) — é "roda agora", "roda depois" ou "rejeita" | 🟢 Baixo |
| ❗️ **Retry do EventBridge** | Se o EventBridge retentar em 5min e a fila ainda estiver ocupada, a retentativa também é rejeitada — mesmo com fila de 1 | 🔴 Crítico |

### O paradoxo da fila de 1

A fila de 1 **não resolve o problema de retentativas do EventBridge**:

```
03:00 → Job inicia (thread ocupada)
03:05 → EventBridge retenta (fila vazia → enfileira) ← fila agora ocupada
03:10 → EventBridge retenta de novo (fila cheia → REJECT)
03:15 → EventBridge retenta de novo (fila cheia → REJECT)
...
```

A segunda retentativa já encontra a fila ocupada e é rejeitada. O ganho em relação a `queueCapacity=0` é de apenas **1 requisição** — a primeira retentativa.

---

## Opção C: `queueCapacity = Integer.MAX_VALUE` (Fila Ilimitada)

```java
executor.setCorePoolSize(1);
executor.setMaxPoolSize(1);
executor.setQueueCapacity(Integer.MAX_VALUE);
```

### Como funciona internamente

O `ThreadPoolExecutor` usa um `LinkedBlockingQueue` sem limite. Todas as tarefas são aceitas e enfileiradas.

```
Tempo:  t0    t1    t2    t3    t4    t5    t6    t7    t8
        │     │     │     │     │     │     │     │     │
Req 1:  │─────│─────│─────────────────────────────────────│
Req 2:  │     │─────│     │     │     │───────────────────│
Req 3:  │     │     │─────│     │     │     │─────────────│
Req 4:  │     │     │     │─────│     │     │     │───────│
...     │     │     │     │     │     │     │     │     │
        ↑                          ↑
     Enfileirando               Thread livre, processa fila
     todas as reqs               sequencialmente
```

### O que acontece com a memória

Cada tarefa enfileirada ocupa memória:
- 1 objeto `Callable` (criado pelo proxy `@Async`) ≈ ~200 bytes
- 1 entrada na `LinkedBlockingQueue` (nó da lista encadeada) ≈ ~40 bytes
- Captura de variáveis do escopo (se houver) ≈ variável

**Para N tarefas enfileiradas:**

| N | Memória aproximada | Impacto |
|---|-------------------|---------|
| 10 | ~2,5 KB | Irrelevante |
| 100 | ~25 KB | Irrelevante |
| 1.000 | ~250 KB | Mínimo |
| 10.000 | ~2,5 MB | Baixo (mas 10k requisições em 2,5h = 1 req/segundo) |
| 1.000.000 | ~250 MB | ⚠️ Crítico — pode causar OOM |

### Cenário catastrófico

```
03:00 → Job inicia (2,5h de execução)
03:01 → Bug no EventBridge dispara 10 requisições/segundo
       → Fila cresce: 10 × 150 × 60 = 90.000 tarefas enfileiradas
       → Consumo de memória: ~20 MB
05:30 → Job termina
05:30 → 90.000 tarefas começam a executar SEQUENCIALMENTE (1 thread)
       → A 500ms por página, cada job processa ~300 páginas
       → 90.000 × 300 páginas × 500ms = 13.500.000 segundos ≈ 156 DIAS
       → Aplicação ocupada 24/7 por 5 meses processando tarefas obsoletas
       → Banco de dados sobrecarregado com milhares de consultas duplicadas
       → `existsByGutendexId()` retorna true para tudo → 0 inserts → desperdício
```

**Isso é um cenário extremo, mas ilustra o risco de fila ilimitada.**

### Prós detalhados

| Pró | Detalhamento |
|-----|-------------|
| ✅ **Nunca rejeita** | Toda requisição é aceita |
| ✅ **Simplicidade** | Sem tratamento de exceção no controller |

### Contras detalhados

| Contra | Detalhamento | Peso |
|--------|-------------|------|
| ❌ **Risco de OOM** | Milhares de tarefas enfileiradas podem estourar a memória | 🔴 Crítico |
| ❌ **Job fantasma** | Tarefas obsoletas executam muito tempo depois, processando dados antigos | 🔴 Crítico |
| ❌ **Sobrecarga do banco** | Milhares de consultas `existsByGutendexId()` desnecessárias | 🟡 Moderado |
| ❌ **Degradação gradual** | Aplicação fica mais lenta conforme a fila cresce (mais threads em espera, mais GC) | 🟡 Moderado |
| ❌ **Sem feedback** | Quem chamou o endpoint recebe 202, mas o job pode nunca executar de fato (se a aplicação reiniciar, a fila é perdida) | 🟡 Moderado |

---

## Opção D: Flag em Memória (Alternativa)

> Opção não baseada em `queueCapacity`, mas em um controle explícito de concorrência.

```java
@Component
public class SyncJobGuard {

    private final AtomicBoolean running = new AtomicBoolean(false);

    public boolean tryAcquire() {
        return running.compareAndSet(false, true);
    }

    public void release() {
        running.set(false);
    }
}
```

Uso no `SyncGutendexJobService`:

```java
@Async("gutendexSyncExecutor")
public void triggerSync() {
    if (!syncJobGuard.tryAcquire()) {
        log.warn("Job já em execução. Requisição ignorada.");
        return;  // simplesmente ignora
    }
    try {
        log.info("Job de sincronização iniciado.");
        syncAllGutendexBooksUseCase.execute();
    } finally {
        syncJobGuard.release();
    }
}
```

### Como funciona

| Situação | Comportamento |
|----------|--------------|
| Thread livre, flag false | `tryAcquire()` → true → executa |
| Thread ocupada, flag true | `tryAcquire()` → false → log + return (ignora) |
| Thread terminou, flag liberada | `release()` → flag false → próxima executa |

### Prós e Contras

| Critério | Avaliação |
|----------|-----------|
| **Clareza** | 🟢 Explícito — qualquer pessoa lê o código e entende |
| **Sem exceções** | 🟢 O controller sempre retorna 202 (não precisa tratar `TaskRejectedException`) |
| **Perde o trigger?** | 🟡 Sim, mas de forma controlada e logada |
| **Escala** | 🟢 Funciona para 1 instância. ❌ Não funciona se houver múltiplas instâncias (EC2 auto-scaling) |

---

## Opção E: Flag no Banco de Dados (Alternativa para Múltiplas Instâncias)

```sql
CREATE TABLE job_locks (
    job_name VARCHAR(100) PRIMARY KEY,
    locked_at TIMESTAMP,
    instance_id VARCHAR(100)
);
```

```java
@Component
public class DatabaseSyncJobGuard {

    @Transactional
    public boolean tryAcquire(String instanceId) {
        // INSERT ... ON DUPLICATE KEY UPDATE ... WHERE locked_at < NOW() - INTERVAL 6 HOUR
        // Se afetou 1 linha → lock adquirido
        // Se afetou 0 linhas → lock já ocupado
    }

    @Transactional
    public void release(String instanceId) {
        // DELETE FROM job_locks WHERE job_name = 'gutendex-sync' AND instance_id = ?
    }
}
```

### Prós e Contras

| Critério | Avaliação |
|----------|-----------|
| **Escala horizontal** | 🟢 Funciona com múltiplas instâncias |
| **Persistência** | 🟢 Se a instância cair, o lock expira por timeout |
| **Complexidade** | 🔴 Maior — requer tabela, transações, tratamento de timeout |
| **Overhead** | 🟡 Cada requisição faz uma consulta ao banco |

---

## Tabela Comparativa Completa

| Critério | A: queue=0 | B: queue=1 | C: queue=MAX | D: Flag memória | E: Flag DB |
|----------|:----------:|:----------:|:-------------:|:---------------:|:----------:|
| **Perde trigger?** | Sim | Não (1 salvo) | Não | Sim | Sim |
| **Risco de OOM** | Nenhum | Mínimo | 🔴 **Alto** | Nenhum | Nenhum |
| **Job obsoleto?** | Nunca | ⚠️ Atrasado | 🔴 **Sempre** | Nunca | Nunca |
| **Funciona em 1 instância** | ✅ | ✅ | ✅ | ✅ | ✅ |
| **Funciona em N instâncias** | ✅ | ✅ | ✅ | ❌ | ✅ |
| **Complexidade** | 🟢 0 | 🟢 0 | 🟢 0 | 🟢 Baixa | 🟡 Média |
| **Tratamento de exceção** | Sim (429) | Sim (429) | Não | Não | Não |
| **Resposta ao cliente** | 202 ou 429 | 202 ou 429 | 202 sempre | 202 sempre | 202 ou 409 |
| **Feedback no log** | "Job rejeitado" | "Job rejeitado" | (silencioso) | "Job já em execução" | "Lock já adquirido" |

---

## Recomendação Final

### Cenário atual (1 instância, schedule semanal)

**`queueCapacity = 0` + Flag em memória (Opção D) = Combinação Robusta**

```java
@Configuration
public class AsyncConfig {

    @Bean(name = "gutendexSyncExecutor")
    public Executor gutendexSyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.setQueueCapacity(0);           // ← rejeita se thread ocupada
        executor.setThreadNamePrefix("gutendex-sync-");
        executor.initialize();
        return executor;
    }
}
```

```java
@Service
public class SyncGutendexJobService {

    private final SyncAllGutendexBooksUseCase syncAllGutendexBooksUseCase;
    private final AtomicBoolean running = new AtomicBoolean(false);

    public SyncGutendexJobService(SyncAllGutendexBooksUseCase syncAllGutendexBooksUseCase) {
        this.syncAllGutendexBooksUseCase = syncAllGutendexBooksUseCase;
    }

    @Async("gutendexSyncExecutor")
    public void triggerSync() {
        if (!running.compareAndSet(false, true)) {
            log.warn("Job já está em execução. Esta requisição foi ignorada.");
            return;
        }
        try {
            log.info("Job de sincronização iniciado em thread separada.");
            syncAllGutendexBooksUseCase.execute();
            log.info("Job de sincronização finalizado.");
        } finally {
            running.set(false);
        }
    }
}
```

**Justificativa:**
- `queueCapacity=0` → proteção contra acúmulo inesperado de tarefas
- `AtomicBoolean` → proteção explícita e legível, que independe do pool
- `@Async + pool=1` → apenas 1 job por vez
- Três camadas de proteção: pool (infra) + flag (lógica) + 429 no controller (HTTP)

### Cenário futuro (N instâncias, auto-scaling)

**`queueCapacity = 0` + Flag no Banco (Opção E)**

Ou usar um lock distribuído via **AWS DynamoDB Lock Client** ou **Redis (Redisson)**, mas isso é over-engineering para o cenário atual.

---

## Resumo para Tomada de Decisão

```
Pergunta: "Qual queueCapacity usar?"

Se você quer:
  ├── Simplicidade máxima → queueCapacity=0 + 429 no controller 🏆
  ├── Não perder triggers → queueCapacity=1 + 429 no controller
  ├── Nunca rejeitar      → queueCapacity=MAX (⚠️ riscos)
  └── Controle explícito  → queueCapacity=0 + AtomicBoolean flag

Recomendação para o Alexandria: queueCapacity=0 + AtomicBoolean flag
```
