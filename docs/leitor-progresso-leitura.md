# Progresso de Leitura — Análise e Melhorias

## Visão Geral

Este documento descreve a implementação atual do sistema de progresso de leitura do leitor EPUB da Alexandria, os problemas identificados e as melhorias propostas. Abrange frontend (`page.tsx`), backend (entidade `UserBooks`) e banco de dados.

---

## Implementação Atual

### Fluxo geral

```
Abre página
  → carrega progress (int) do banco via getUserBooks
  → restaura posição via localStorage (CFI)
  → epub.js gera locations (~1600 chars cada)
  → usuário vira página → evento "relocated"
    → calcula % via percentageFromCfi()
    → atualiza barra visual + estimativas
    → debounce 2s → salva progress (int) no banco
```

### Cálculo de progresso (`handleRendition`)

O evento `relocated` do epub.js é disparado a cada virada de página. O percentual é calculado em três níveis de fallback:

| Situação | Fonte | Precisão |
|---|---|---|
| Locations geradas (`locationsReadyRef = true`) | `percentageFromCfi(loc.start.cfi)` | Alta |
| Locations ainda não prontas, epub tem `percentage` | `loc.start.percentage` | Média |
| Nenhum dos anteriores | índice do capítulo / total de capítulos | Baixa |

### Geração de locations

```ts
rendition.book.locations.generate(1600)
```

Divide o EPUB em fragmentos de ~1600 caracteres cada (equivalente à unidade Kindle). O progresso só fica preciso após esse processo terminar — pode levar alguns segundos em livros grandes.

### Persistência

- **localStorage**: CFI (`epub-location-{id}`) — posição exata para restauração local
- **Backend**: `progress INT` (0–100) via `updateUserBook`
- **Debounce**: 2 segundos após última virada de página
- **Condição de save**: `percent !== lastSavedProgressRef.current` (qualquer diferença)

### Estimativa de tempo restante

Usa dois modos:

1. **Fallback imediato** (primeira relocated da sessão): `locations_restantes × 1,28 min/location` (baseado em 250 WPM, ~320 palavras por location)
2. **Velocidade real** (após ≥1 min de sessão com progresso mensurável): `(100 - percent) / (percent_delta / minutos_decorridos)`

### Modos de exibição (clicável no header)

`percent → minutes → pages → percent → ...`

O modo "pages" exibe o número de **locations** restantes, não páginas reais.

---

## Problemas Identificados

### 1. Progresso como `INT` — perda de precisão na restauração

O banco armazena `progress INT` (0–100). Em um EPUB de 400 locations, cada 1% representa ~4 locations (~6400 caracteres). Ao restaurar a leitura, o leitor não consegue voltar ao ponto exato — apenas ao percentual aproximado.

### 2. CFI não é persistido no backend

A posição exata (`loc.start.cfi`) só vai para o `localStorage`. Trocar de navegador, reinstalar o browser ou acessar de outro dispositivo faz o leitor iniciar do percentual salvo sem posição exata, perdendo o ponto real de leitura.

### 3. Debounce sem threshold mínimo

Qualquer mudança de 1% (`percent !== lastSavedProgressRef.current`) agenda um save. Em EPUBs com locations pequenas ou leitura rápida, isso pode gerar escritas frequentes no banco.

### 4. "Páginas restantes" não são páginas

O valor exibido no modo `pages` é o número de **locations** restantes (fragmentos de ~1600 chars), não páginas reais do livro. Mostrar "~142 pág. restantes" pode ser enganoso para o usuário.

### 5. Estimativa de tempo reseta a cada sessão

`sessionStartTimeRef` é zerado ao abrir o leitor. Se o usuário fechar e reabrir várias vezes, a estimativa oscila nos primeiros minutos de cada sessão.

---

## Bugs Corrigidos

### Bug 1 — `relocated` disparando antes das locations prontas (progresso errado)

**Sintoma:** O progresso mostrado no leitor caía bruscamente ao virar a primeira página (ex.: de 40% para 12.5%).

**Causa raiz:** O evento `relocated` do epub.js dispara duas vezes ao abrir o leitor:
1. Ao restaurar a posição salva (CFI) — navegação interna, não do usuário
2. Ao virar a primeira página pelo usuário

Na segunda disparada, as `locations` ainda não estavam prontas. O código caía nos fallbacks:
- `loc.start.percentage` → percentual dentro do *capítulo atual*, não do livro todo
- `spine.index / total` → cálculo grosseiro (ex.: capítulo 1 de 8 = 12.5%)

Esses valores sobrescreviam o progresso salvo e, como o delta era > 0.5%, agendavam um save com o valor errado.

**Correção (`handleRendition` em `page.tsx`):**

```ts
let isInitialLoad = true;

rendition.on('relocated', (loc) => {
  // Pula o primeiro evento (restauração de posição, não navegação do usuário)
  if (isInitialLoad) {
    isInitialLoad = false;
    return;
  }
  // Aguarda locations para garantir cálculo preciso — sem fallbacks
  if (!locationsReadyRef.current) return;
  // ... cálculo via percentageFromCfi apenas
});
```

Adicionalmente, assim que `locations.generate()` termina, o progresso é recalculado via `rendition.currentLocation()` para atualizar o display sem precisar que o usuário vire uma página.

---

### Bug 2 — Race condition: saída rápida perde o progresso (debounce cancelado)

**Sintoma:** Ao avançar para 30% e voltar rapidamente para o card, o card continuava mostrando 20%. Ao re-entrar no leitor, a posição saltava de 20% para 30% em milissegundos, mas o card permanecia em 20%.

**Causa raiz:** O debounce de 2 segundos agendava o save, mas o cleanup do `useEffect` (`return () => { clearTimeout(...) }`) cancelava o timeout antes que ele disparasse quando o usuário saía rapidamente. O progresso nunca chegava ao backend.

**Fluxo com bug:**
```
Avança para 30% → agenda save em 2s
→ usuário volta em 1s
→ useEffect cleanup: clearTimeout() → save cancelado
→ backend ainda tem 20% → card mostra 20%
```

**Correção:** Dois novos refs rastreiam o progresso e CFI pendentes. O cleanup, em vez de apenas cancelar o timeout, verifica se há um save pendente e o executa imediatamente:

```ts
// Refs adicionados:
const pendingProgressRef = useRef<number>(0);
const pendingCfiRef = useRef<string>('');

// Atualizados quando há novo progresso a salvar (relocated + locations.generate)
pendingProgressRef.current = percent;
pendingCfiRef.current = loc.start.cfi;

// Cleanup do useEffect:
return () => {
  if (saveTimerRef.current) clearTimeout(saveTimerRef.current);
  // Flush imediato se houver save pendente
  if (
    userBookIdRef.current &&
    pendingCfiRef.current &&
    Math.abs(pendingProgressRef.current - lastSavedProgressRef.current) >= 0.5
  ) {
    persistProgress(pendingProgressRef.current, pendingCfiRef.current);
  }
};
```

**Fluxo corrigido:**
```
Avança para 30% → pendingProgressRef = 30%, pendingCfiRef = CFI
→ agenda save em 2s
→ usuário volta em 1s
→ cleanup: cancela timeout + detecta pending (delta = 10% ≥ 0.5%)
→ persistProgress(30%, cfi) executado imediatamente
→ backend atualizado → card mostra 30%
```

**Observação:** O flush no unmount usa `fetch` normal (fire-and-forget via `.catch(() => {})`). A requisição HTTP continua em andamento mesmo após o componente desmontar — comportamento garantido pelos browsers modernos para requisições já iniciadas.

---

### Bug 3 — Race condition: posição salva não restaura ao reabrir o livro

**Sintoma:** O progresso salvo aparecia corretamente no card da Biblioteca (ex.: 5%), mas, ao clicar no livro para reabri-lo no leitor, a leitura sempre recomeçava na capa (posição 0) em vez de retomar de onde o usuário parou.

**Causa raiz:** O componente inicializava o state `location` em `0` e restaurava a posição salva manualmente dentro de `getRendition`, chamando `rendition.display(cfiSalvo)` depois que `book.ready` e `locations.generate()` resolviam (ambos assíncronos).

O problema é que a própria lib `react-reader` também navega para a posição na inicialização, em `initReader()` (`node_modules/react-reader/dist/react-reader.es.js`):

```js
getRendition && getRendition(rendition);
if (typeof location === "string" || typeof location === "number") {
  rendition.display(location + "");   // location = 0, chamada síncrona
}
```

Ou seja, existiam **duas chamadas concorrentes de `rendition.display()`**: a da lib (síncrona, para `location=0`, disparada imediatamente após `getRendition`) e a manual do componente (assíncrona, para o CFI salvo, disparada só depois que `locations.generate()` terminava). Como `display()` é assíncrono por dentro — precisa carregar e renderizar a seção do EPUB —, não havia garantia de qual das duas terminava por último. Na prática, a renderização da capa costumava "ganhar" a corrida e sobrescrever a posição restaurada.

**Fluxo com bug:**
```
getRendition(rendition) é chamado
  → lib dispara rendition.display("0") (síncrono, mas resolve de forma assíncrona)
  → component agenda book.ready.then(() => locations.generate().then(() => display(cfiSalvo)))
→ as duas chamadas de display() correm em paralelo
→ não há ordem garantida de conclusão
→ display("0") às vezes termina depois de display(cfiSalvo) → leitor fica na capa
```

**Correção (`page.tsx`):** eliminar a segunda chamada de `display()` em vez de tentar vencer a corrida. A posição salva passou a ser lida do `localStorage` já no valor inicial do `useState` de `location` — a prop controlada que a própria lib usa para navegar na inicialização:

```ts
const [location, setLocation] = useState<string | number>(() => {
  if (typeof window === "undefined") return 0;
  return localStorage.getItem(`epub-location-${id}`) ?? 0;
});
```

Com isso, existe apenas **uma** chamada de `display()` na inicialização (feita pela própria lib, com o CFI já correto), eliminando a condição de corrida. O bloco que fazia `rendition.display(restoreLocationRef.current)` dentro de `handleRendition` foi removido.

---

## Melhorias Propostas

### Banco de dados

```sql
-- Adicionar via nova migration (ex: V5__Add_reading_progress_fields.sql)
ALTER TABLE user_books
  ADD COLUMN progress_cfi  VARCHAR(512)   NULL AFTER progress,
  ADD COLUMN progress_pct  DECIMAL(5,2)   NULL AFTER progress_cfi,
  ADD COLUMN last_read_at  DATETIME       NULL AFTER progress_pct;
```

| Campo | Tipo | Descrição |
|---|---|---|
| `progress` | `INT` | Mantido por compatibilidade durante transição |
| `progress_pct` | `DECIMAL(5,2)` | Percentual com precisão decimal (ex: `37.42`) |
| `progress_cfi` | `VARCHAR(512)` | CFI exato para restauração precisa |
| `last_read_at` | `DATETIME` | Base para sincronização multi-dispositivo |

Após migração e estabilização do frontend, `progress INT` pode ser removido.

### Backend

**`UserBooksEntity.java`**
```java
@Column(name = "progress_cfi", length = 512)
private String progressCfi;

@Column(name = "progress_pct", precision = 5, scale = 2)
private BigDecimal progressPct;

@Column(name = "last_read_at")
private LocalDateTime lastReadAt;
```

**`UpdateUserBooksRequest.java` / `UpdateUserBooksInput.java`**
```java
private String progressCfi;   // nullable
private Double progressPct;   // substitui int progress
```

**`UpdateUserBooksUseCase.java`** — atribuir `lastReadAt = LocalDateTime.now()` sempre que o status for `reading` e houver update de progresso.

**`UserBooksResponse.java` / `UserBooksOutput.java`**
```java
private String progressCfi;
private Double progressPct;
private LocalDateTime lastReadAt;
```

### Frontend — `page.tsx`

**Percentual com decimal:**
```ts
// linha 118 — usar parseFloat + toFixed
percent = parseFloat(
  (rendition.book.locations.percentageFromCfi(loc.start.cfi) * 100).toFixed(2)
);
```

**Threshold mínimo de 0.5% antes de agendar save:**
```ts
// linha 156 — substituir a condição atual
const delta = Math.abs(percent - lastSavedProgressRef.current);
if (!userBookIdRef.current || delta < 0.5) return;

if (saveTimerRef.current) clearTimeout(saveTimerRef.current);
saveTimerRef.current = setTimeout(
  () => persistProgress(percent, loc.start.cfi),
  2000
);
```

**`persistProgress` salva CFI junto:**
```ts
function persistProgress(percent: number, cfi: string) {
  lastSavedProgressRef.current = percent;
  updateUserBook(userBookIdRef.current!, {
    status: 'reading',
    progressPct: percent,
    progressCfi: cfi,
  }).catch(() => {});
}
```

**Restauração priorizando CFI do backend (sincronização multi-dispositivo):**
```ts
// no init(), após buscar userBook
const serverCfi = userBook.progressCfi ?? null;
const serverTs  = userBook.lastReadAt
  ? new Date(userBook.lastReadAt).getTime()
  : 0;
const localTs   = Number(localStorage.getItem(`epub-ts-${id}`) ?? 0);

if (serverTs > localTs && serverCfi) {
  setLocation(serverCfi);
} else {
  const localCfi = localStorage.getItem(`epub-location-${id}`);
  if (localCfi) setLocation(localCfi);
}

// ao salvar no localStorage, salvar também o timestamp
localStorage.setItem(`epub-location-${id}`, loc);
localStorage.setItem(`epub-ts-${id}`, Date.now().toString());
```

**Exibição honesta no modo "pages":**
```tsx
// trocar label de "pág. restantes" para "seções restantes"
~{pagesLeft} seções restantes
```

---

## Sincronização Multi-dispositivo

### Estratégia

O campo `last_read_at` é a chave. Ao abrir o leitor em qualquer dispositivo:

1. Backend retorna `{ progressPct, progressCfi, lastReadAt }`
2. Frontend compara `lastReadAt` do servidor com timestamp salvo no `localStorage`
3. **Ganha o mais recente** — se o servidor for mais novo, usa o CFI do servidor; caso contrário, usa o CFI local

```
Desktop lê até 62% às 20:00
  → salva { progressPct: 62.4, progressCfi: "epubcfi(...)", lastReadAt: "2026-06-17T20:00" }

Mobile abre às 20:05
  → busca userBook → lastReadAt servidor (20:00) > lastReadAt local (nunca)
  → restaura do CFI do servidor → abre exatamente em 62.4%
```

### Limitações da abordagem atual

- Não há resolução de conflito para leituras simultâneas em dois dispositivos — o último a salvar vence
- Suficiente para o caso de uso normal (um dispositivo por vez)

---

## Extensão Futura: Marcações, Destaques e Anotações

O CFI já é o identificador padrão do epub.js para qualquer ponto ou range no texto. Uma tabela de anotações usando o mesmo modelo:

```sql
CREATE TABLE reading_annotations (
  id            BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_book_id  BIGINT       NOT NULL,
  type          ENUM('highlight', 'bookmark', 'note') NOT NULL,
  cfi_start     VARCHAR(512) NOT NULL,
  cfi_end       VARCHAR(512) NULL,     -- null para bookmarks
  color         VARCHAR(20)  NULL,     -- para highlights
  note_text     TEXT         NULL,     -- para notes
  created_at    DATETIME     NOT NULL,
  CONSTRAINT fk_annotations_userbook FOREIGN KEY (user_book_id) REFERENCES user_books(id)
);
```

No frontend, o epub.js já disponibiliza o evento `rendition.on('selected')` que retorna o CFI do range selecionado — base para capturar highlights.

---

## Prioridades de Implementação

| Status | Prioridade | Mudança | Arquivo(s) |
|---|---|---|---|
| ✅ Implementado | Alta | Migration com `progress_cfi`, `progress_pct`, `last_read_at` | `V003__Add_reading_progress_fields.sql` |
| ✅ Implementado | Alta | Salvar CFI no backend ao virar página | `page.tsx`, `UpdateUserBooksRequest`, `UserBooksEntity` |
| ✅ Implementado | Alta | Restaurar posição pelo CFI do servidor | `page.tsx` (`init()`) |
| ✅ Implementado | Alta | Bug: `relocated` antes das locations → valores errados | `page.tsx` (`handleRendition`) |
| ✅ Implementado | Alta | Bug: race condition debounce → perda de progresso ao sair rápido | `page.tsx` (cleanup + `pendingProgressRef`) |
| ✅ Implementado | Média | `DECIMAL(5,2)` no cálculo de progresso | `page.tsx` (`handleRendition`) |
| ✅ Implementado | Média | Threshold de 0.5% no debounce | `page.tsx` (`handleRendition`) |
| ✅ Implementado | Média | `last_read_at` para sincronização | `UpdateUserBooksUseCase` |
| ✅ Implementado | Baixa | Renomear "pág. restantes" → "seções restantes" | `page.tsx` (`renderReadingInfo`) |
| ⏳ Futura | — | Tabela `reading_annotations` para destaques/notas | Nova migration + endpoints |

---

## Referências

- Documentação epub.js: [github.com/futurepress/epub.js](https://github.com/futurepress/epub.js)
- CFI spec: [EPUB Canonical Fragment Identifiers](https://idpf.org/epub/linking/cfi/)
- Análise de zoom e páginas: `docs/porcentagemxleitor/logica_zoom_paginas.md`
- Implementação atual: `alexandria-frontend/src/app/(main)/leitor/[id]/page.tsx`
- Entidade backend: `alexandria-backend/src/main/java/.../domain/userbook/UserBooks.java`
