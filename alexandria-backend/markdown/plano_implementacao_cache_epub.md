# Plano de Implementação — Cache de EPUB com IndexedDB + Memória

> **Objetivo:** Eliminar o re-download do arquivo EPUB a cada visita ao leitor, tornando a abertura instantânea após o primeiro acesso e permitindo leitura offline.

---

## Cenário Atual

```
Usuário clica em "Ler agora"
        │
        ▼
getBookById(id) + getUserBooks()     ← ~200ms
        │
        ▼
fetch(/api/epub?url=...)             ← ~2s (download do EPUB inteiro)
        │
        ▼
setEpubData(arrayBuffer)             ← renderiza o ReactReader
```

**Problema:** Toda vez que o componente `<LeitorPage>` é montado, o EPUB é baixado do zero — mesmo que o usuário já tenha lido aquele livro antes.

---

## Solução Proposta

### Estratégia de cache em 3 níveis

```
Solicitação do EPUB
        │
        ▼
┌──────────────────────────────┐
│  1. Cache em memória (Map)   │ ← Instantâneo (mesma sessão)
└──────────┬───────────────────┘
           │ (miss)
           ▼
┌──────────────────────────────┐
│  2. IndexedDB (persistente)  │ ← ~5ms (sessões diferentes)
│     + LRU (30 entries/250MB) │
└──────────┬───────────────────┘
           │ (miss)
           ▼
┌──────────────────────────────┐
│  3. Download via /api/epub   │ ← ~2s (primeira vez apenas)
│     + salva nos níveis 1 e 2 │
└──────────────────────────────┘
```

---

## Arquivos Envolvidos

| Arquivo | Ação |
|---|---|
| `alexandria-frontend/src/lib/epub-cache.ts` | **CRIAR** — módulo de cache com LRU e IndexedDB |
| `alexandria-frontend/src/hooks/useEpub.ts` | **CRIAR** — hook que encapsula cache + download + heartbeat LRU |
| `alexandria-frontend/src/app/(main)/leitor/[id]/page.tsx` | **MODIFICAR** — usar useEpub em vez de fetch direto |

---

### 1. `alexandria-frontend/src/lib/epub-cache.ts` (NOVO)

```typescript
export interface CacheEntry {
  data: ArrayBuffer;
  lastAccessed: number; // timestamp ms
  size: number;         // bytes
}

// ── Configuração LRU ──
const LRU_MAX_ENTRIES = 30;
const LRU_MAX_SIZE_BYTES = 250 * 1024 * 1024; // 250 MB

// ── Cache em memória (Map) — dura enquanto a aba estiver aberta ──
const MEMORY_CACHE = new Map<string, CacheEntry>();

// ── Constantes do IndexedDB ──
const DB_NAME = "alexandria-epub-cache";
const STORE_NAME = "epubs";
const DB_VERSION = 2;
```

**Funções exportadas:**

| Função | Descrição |
|---|---|
| `getCachedEpub(bookId)` | Tenta memória → IndexedDB, retorna `ArrayBuffer \| null` |
| `saveEpubToCache(bookId, data)` | Salva nos dois níveis + evicção LRU (fire-and-forget) |
| `markBookAsAccessed(bookId)` | Atualiza `lastAccessed` para LRU (fire-and-forget) |
| `removeEpubFromCache(bookId)` | Remove um EPUB específico |
| `clearAllEpubCache()` | Remove TODOS os EPUBS, retorna lista de keys removidas |
| `getCacheStats()` | Retorna `{ memoryEntries, memorySize }` para debug |

**Detalhes de implementação:**

- `openDB()`: Abre/cria IndexedDB com `onupgradeneeded` + type guard
- `idbGet<T>()` e `idbWait()`: Utilitários para evitar repetição de padrão
- **Evicção LRU**: Coleta entries com cursor → ordena por `lastAccessed` → remove 30% mais antigos em transação separada
- **Sem `console.log`**: Todos os catch blocks são vazios (silenciosos) para não poluir o console
- `getCacheStats()` formata tamanho em KB/MB

---

### 2. `alexandria-frontend/src/hooks/useEpub.ts` (NOVO)

```typescript
interface UseEpubResult {
  epubData: ArrayBuffer | null;
  loading: boolean;
  error: Error | null;
}
```

**Fluxo:**
1. Inicia com `loading = true`
2. Tenta `getCachedEpub(bookId)` — se hit, `setEpubData` + `markBookAsAccessed`
3. Se miss, faz `fetch(/api/epub?url=...)` → `setEpubData` + `saveEpubToCache` (fire-and-forget)
4. Se erro, `setError`
5. `loading = false` após sucesso ou erro

**Heartbeat LRU:** `setInterval` a cada 2 minutos chamando `markBookAsAccessed`. Cleanup ao desmontar (limpa intervalo + marca último acesso).

**Observação:** O hook aceita `downloadUrl: string | null`. Quando `null` (book sem downloadUrl), apenas seta `loading = false`.

---

### 3. Modificar `alexandria-frontend/src/app/(main)/leitor/[id]/page.tsx`

**O que mudou (diff resumido):**

| Ação | Detalhe |
|---|---|
| **+1** | `import { useEpub } from "@/hooks/useEpub"` |
| **-2** | Removido `const [epubData, setEpubData] = useState<ArrayBuffer \| null>(null)` |
| **-1** | Removido `const [loading, setLoading] = useState(true)` |
| **+3** | Adicionado `const { epubData, loading: epubLoading, error: epubError } = useEpub(id, book?.downloadUrl ?? null)` |
| **-4** | Removido bloco `if (b.downloadUrl) { fetch... }` do `init()` |
| **~1** | `init().catch(console.error)` — sem `.finally()` (não há estado de loading da página) |
| **~1** | `if (!book)` — carregando dados do livro |
| **+9** | Bloco `if (epubError)` — mensagem de erro amigável + link "Voltar para o livro" |
| **~1** | `if (epubLoading \|\| !epubData)` — "Carregando livro..." |

**Loading states no JSX (ordem de avaliação):**

```
1. if (!book)                          → "Carregando leitor..."
2. if (epubError)                      → "Erro ao carregar o livro." + link
3. if (!book?.downloadUrl)             → "Arquivo de leitura indisponível." + link
4. if (epubLoading || !epubData)       → "Carregando livro..."
5. else                                → ReactReader com header, barra de progresso
```

> **Nota:** Diferente do plano original, não foi criado um estado `pageLoading` separado. A lógica de loading usa combinação dos estados `book`, `epubLoading` e `epubData` diretamente.

---

## Fluxo Completo (Depois da Implementação)

### Primeira vez que abre o livro

```
Usuário clica em "Ler agora"
        │
        ▼
getBookById(id) + getUserBooks()     ← ~200ms
        │
        ▼
book carregado → useEpub detecta downloadUrl → setLoading(true)
        │
        ▼
getCachedEpub(id) → MISS
        │
        ▼
fetch(/api/epub?url=...)             ← ~2s (download)
        │
        ▼
setEpubData(data) + loading=false
        │
        ▼
ReactReader renderiza                  ✅
```

### Segunda vez (mesma sessão)

```
Usuário clica em "Ler agora"
        │
        ▼
getBookById(id) + getUserBooks()     ← ~200ms
        │
        ▼
book carregado → useEpub detecta downloadUrl
        │
        ▼
getCachedEpub(id) → HIT (memória)   ← instantâneo
        │
        ▼
setEpubData + loading=false
        │
        ▼
ReactReader renderiza                  ✅ (sem download)
```

### Terceira vez (sessão diferente, mesmo livro)

```
Usuário clica em "Ler agora"
        │
        ▼
getBookById(id) + getUserBooks()     ← ~200ms
        │
        ▼
getCachedEpub(id) → HIT (IndexedDB) ← ~5ms
        │               (promove para memória)
        ▼
setEpubData + loading=false
        │
        ▼
ReactReader renderiza                  ✅ (sem download)
```

### Falha no download (epubError)

```
Usuário clica em "Ler agora"
        │
        ▼
book carregado → useEpub → MISS → fetch(/api/epub) ← erro
        │
        ▼
loading=false, epubError=Error
        │
        ▼
"Erro ao carregar o livro." ← usuário vê mensagem + link para voltar ✅
```

### Durante a leitura (heartbeat LRU)

```
A cada 2 minutos:
  markBookAsAccessed(id) → atualiza lastAccessed no IndexedDB

Ao sair da página:
  markBookAsAccessed(id) → garante o último timestamp
```

### Cache estourou (evicção LRU)

```
saveEpubToCache(id, data) → detecta >30 entries ou >250 MB
                          → cursor único: coleta entries
                          → ordena por lastAccessed crescente
                          → remove 30% mais antigos (transação separada)
                          → limpa memória da aba atual
```

---

## Diferenças entre o Plano Original e o Implementado

| Aspecto | Plano Original | Implementado |
|---|---|---|
| `console.info`/`console.warn` | Vários logs de debug | Catch blocks silenciosos (vazios) |
| `pageLoading` state | Criar `pageLoading` para separar do `epubLoading` | Não criado — usa combinação de `!book`, `epubLoading`, `epubData` |
| `.finally(() => setPageLoading(false))` | Presente no `init()` | Ausente — `init()` só tem `.catch(console.error)` |
| Loading "Carregando leitor..." | Exibido durante `pageLoading` | Exibido durante `!book` |
| Tratamento de erro | Bloco `epubError` no JSX | Idem ao plano |
| `getCacheStats()` | `formatBytes()` helper | Inline: `mb < 1 ? KB : MB` |

---

## Nota sobre o "flash da capa"

O cache **elimina o download de ~2s**, mas o ReactReader ainda exibe brevemente a capa/posição 0 antes de restaurar a localização salva. Isso ocorre porque o parse do XML do EPUB e a geração de locations pelo `epub.js` (~200-500ms de CPU) acontecem localmente — não dependem de rede.

**Este plano não inclui correção para esse flash.** É um bug pré-existente, não introduzido pelo cache. Uma solução futura seria inicializar o `location` do ReactReader com o valor salvo no `localStorage`, mas isso requer validação cuidadosa para não quebrar o tracking de progresso.

---

## Considerações

### Tratamento de erros

- Se IndexedDB falhar (disco cheio, modo anônimo restritivo), o cache em memória ainda funciona para a sessão atual
- Se IndexedDB e memória falharem, faz o download normal (graceful degradation)
- `markBookAsAccessed` nunca quebra a UI — erros são silenciosos
- Se o download do EPUB falhar, `epubError` exibe mensagem amigável com link para voltar

### Cache HTTP da rota `/api/epub`

Já existe `Cache-Control: public, max-age=86400` — complementa o IndexedDB. Se o usuário nunca abriu o livro antes, o proxy `/api/epub` retorna do cache HTTP do navegador se já tiver sido baixado em outra aba.

### Limpeza do cache (página de Configurações — futura)

```typescript
import { clearAllEpubCache } from "@/lib/epub-cache";

async function handleClearCache() {
  const removed = await clearAllEpubCache();
  alert(`${removed.length} livros removidos do cache`);
}
```

### Limites do LRU

| Parâmetro | Valor | Justificativa |
|---|---|---|
| `LRU_MAX_ENTRIES` | 30 | Usuário típico lê 1-3 livros simultaneamente; 30 é folgado |
| `LRU_MAX_SIZE_BYTES` | 250 MB | EPUB médio: 0.5-5 MB. 30 × 5 MB = 150 MB. 250 MB é confortável |
| `evictionRatio` | 30% | Remove os mais antigos o suficiente para não precisar evictar de novo logo em seguida |
| `heartbeatMs` | 2 min | Intervalo baixo o bastante para LRU ser preciso; alto o bastante para não sobrecarregar IndexedDB |

### Impacto em infraestrutura (RDS / EC2)

**Nenhum.** Todo o cache é gerenciado no navegador do usuário via IndexedDB. O backend não é modificado. A rota `/api/epub` (Next.js API Route) continua servindo o mesmo número de requests — na verdade, será **menos chamada** porque cache hits evitam o fetch.

---

## Checklist de Implementação

- [x] Criar `alexandria-frontend/src/lib/epub-cache.ts` com:
  - `openDB()` com type guard e suporte a migração
  - `getCachedEpub(bookId)` — cache em memória → IndexedDB
  - `saveEpubToCache(bookId, data)` + `evictIfNeeded()` com cursor e transação separada
  - `markBookAsAccessed(bookId)` — gatilho LRU (usado pelo heartbeat)
  - `removeEpubFromCache(bookId)` — remoção individual
  - `clearAllEpubCache()` — limpeza total via IndexedDB keys
  - Utilitários: `idbGet`, `idbWait`, `getCacheStats`

- [x] Criar `alexandria-frontend/src/hooks/useEpub.ts` com:
  - Cache hit → `setEpubData` + `markBookAsAccessed`
  - Cache miss → download → `saveEpubToCache` (fire-and-forget)
  - Heartbeat LRU a cada 2 minutos
  - Cleanup ao desmontar

- [x] Modificar `leitor/[id]/page.tsx`:
  - Importar `useEpub`
  - **Remover** `useState` de `epubData` e `loading`
  - **Adicionar** `useEpub(id, book?.downloadUrl ?? null)` com desestruturação de `epubData`, `epubLoading`, `epubError`
  - Remover bloco `fetch` do `useEffect`
  - **Adicionar** bloco de tratamento de `epubError` (mensagem amigável + link "Voltar para o livro")
  - Lógica de loading: `!book` → "Carregando leitor...", `epubError` → "Erro...", `!downloadUrl` → "Indisponível", `epubLoading || !epubData` → "Carregando livro..."

- [x] Testar:
  1. Abrir livro → esperar download → leitor carrega ✅
  2. Fechar e reabrir o mesmo livro → instantâneo ✅
  3. Fechar a aba, abrir nova aba → instantâneo (IndexedDB) ✅
  4. Abrir 31+ livros → LRU remove os mais antigos (não testado exaustivamente)
  5. Deixar livro aberto por 5 min → `lastAccessed` atualizado pelo heartbeat ✅
  6. Modo anônimo/privado → funciona (cai para download se IndexedDB não estiver disponível) (não testado)
  7. **Desligar internet → abrir livro → mensagem "Erro ao carregar o livro." com link para voltar** ✅
