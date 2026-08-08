# Fluxo de Dados — Biblioteca do Usuário

## Visão Geral

Livros adicionados pelo usuário são armazenados no backend via endpoint autenticado `/user-books`. A página Biblioteca exibe apenas os livros do usuário logado, não o catálogo geral.

```
/explorar/:id  →  POST /user-books (backend :8080)  →  tabela user_books (MySQL)
/biblioteca    →  GET  /user-books (backend :8080)  →  tabela user_books (MySQL)
```

---

## 1. Backend — Endpoints

| Método | Endpoint | Auth | Descrição |
|--------|----------|------|-----------|
| POST | `/user-books` | JWT obrigatório | Adiciona livro à biblioteca do usuário |
| GET | `/user-books` | JWT obrigatório | Lista livros do usuário (paginado) |
| PUT | `/user-books/{id}` | JWT obrigatório | Atualiza status/progresso/avaliação |
| DELETE | `/user-books/{id}` | JWT obrigatório | Remove livro da biblioteca |

### Request — POST `/user-books`

```json
{
  "bookId": 42,
  "status": "TOREAD"
}
```

### Response — POST e GET `/user-books`

```json
{
  "id": 1,
  "book": {
    "id": 42,
    "title": "Dom Quixote",
    "author": "Cervantes",
    "coverUrl": "https://...",
    "downloadUrl": "https://...",
    "languages": "pt",
    "subjects": "Fiction",
    "source": "gutendex",
    "gutendexId": 996,
    "downloadCount": 12345,
    "publisherId": null
  },
  "status": "TOREAD",
  "progress": null,
  "rating": null
}
```

### Regras de negócio por status

| Status | `progress` | `rating` |
|--------|-----------|---------|
| `TOREAD` | null | null |
| `READING` | obrigatório (0–100) | null |
| `DONE` | null | obrigatório (0–5) |

**Restrição de duplicata:** cada usuário só pode adicionar um mesmo livro uma vez. Tentativa duplicada retorna HTTP 409.

---

## 2. Frontend — Camada de API

**Arquivo:** `alexandria-frontend/src/lib/api.ts`

### Tipo

```ts
interface UserBookResponse {
  id: number;
  book: BookApiResponse;
  status: string;
  progress: number | null;
  rating: number | null;
}
```

### Funções

```ts
// Adiciona livro à biblioteca do usuário logado
addUserBook(bookId: number): Promise<UserBookResponse>
// POST $NEXT_PUBLIC_API_URL/user-books  { bookId, status: "TOREAD" }
// Lança Error("already_added") se receber 409

// Lista livros da biblioteca do usuário logado
getUserBooks(): Promise<UserBookResponse[]>
// GET $NEXT_PUBLIC_API_URL/user-books
// Retorna page.content do Spring Page
```

Ambas usam `apiFetch`, que injeta automaticamente o header `Authorization: Bearer <token>` do `localStorage`.

---

## 3. Frontend — Página de Detalhes (`/explorar/:id`)

**Arquivo:** `alexandria-frontend/src/app/(main)/explorar/[id]/page.tsx`

### Estados adicionados

```ts
const [adding, setAdding] = useState(false); // requisição em andamento
const [added, setAdded] = useState(false);   // livro já adicionado com sucesso
```

### Handler do botão

```ts
async function handleAddToLibrary() {
  setAdding(true);
  try {
    await addUserBook(book!.id);
    setAdded(true);
  } catch (e) {
    if (e instanceof Error && e.message === 'already_added') {
      setAdded(true); // trata 409 como sucesso visual
    }
  } finally {
    setAdding(false);
  }
}
```

### Estados visuais do botão

| Estado | Texto exibido | Habilitado |
|--------|--------------|-----------|
| Inicial | "Adicionar à Biblioteca" | Sim |
| Aguardando resposta | "Adicionando..." | Não |
| Adicionado (ou 409) | "Adicionado à Biblioteca" | Não |

---

## 4. Frontend — Página Biblioteca (`/biblioteca`)

**Arquivo:** `alexandria-frontend/src/app/(main)/biblioteca/page.tsx`

### Fonte de dados

Substituído `getBooks()` (catálogo geral) por `getUserBooks()` (biblioteca pessoal autenticada).

### Filtros por status

```ts
const STATUS_MAP: Record<string, number> = {
  READING: 1, // "Lendo"
  DONE: 2,    // "Concluído"
  TOREAD: 3,  // "Para Ler"
};
```

O filtro `activeFilter === 0` exibe todos os livros sem restrição de status.

### Acesso aos dados no template

Como `UserBookResponse` aninha o livro em `ub.book`, o template acessa:
- `ub.book.title`, `ub.book.author`, `ub.book.coverUrl`
- `ub.id` como key do React (id do vínculo usuário–livro, não do livro)

### Cards clicáveis — componente `BookCover`

A capa de cada livro é renderizada pelo componente interno `BookCover`, que diferencia dois casos:

| Condição | Comportamento |
|----------|--------------|
| `ub.book.downloadUrl` preenchido | Capa envolta em `<Link href="/leitor/:id">` com overlay escuro + ícone `BookOpen` no hover |
| `ub.book.downloadUrl` nulo | Capa não clicável (`cursor-not-allowed`), overlay com texto "Indisponível" no hover |

### Indicadores de progresso e status

Sempre visíveis sobre a capa (z-index acima do overlay de hover):

**Badge de status** — canto superior esquerdo:

| Status | Label | Cor |
|--------|-------|-----|
| `READING` | Lendo | `bg-terra` (laranja) |
| `DONE` | Concluído | `bg-brown` (marrom escuro) |
| `TOREAD` | — | Sem badge |

**Barra de progresso** — rodapé da capa, visível somente quando `status === "READING"`:
- Track: `bg-brown/20` (fundo semi-transparente)
- Fill: `bg-terra`, largura proporcional a `ub.progress` (0–100) via `style={{ width: \`${ub.progress}%\` }}`
- Altura: `h-1` (4px)

```tsx
// badge
{badge && (
  <span className={`absolute top-2 left-2 ... z-10 ${badge.className}`}>
    {badge.label}
  </span>
)}

// barra de progresso
{ub.status === 'READING' && ub.progress != null && (
  <div className="absolute bottom-0 left-0 right-0 h-1 bg-brown/20 z-10">
    <div className="h-full bg-terra" style={{ width: `${ub.progress}%` }} />
  </div>
)}
```

---

## 5. Autenticação

O token JWT é armazenado em `localStorage` sob a chave `auth-token` pelo `AuthContext` (`src/contexts/AuthContext.tsx`). A função `apiFetch` o lê automaticamente em toda chamada.

Se o usuário não estiver autenticado, as chamadas a `/user-books` retornarão HTTP 401. Nesse caso, `getUserBooks` lança um erro e a biblioteca exibe o estado vazio.

---

## 6. Leitor — Rastreamento de Progresso

**Arquivo:** `alexandria-frontend/src/app/(main)/leitor/[id]/page.tsx`

### O que acontece ao entrar no leitor

1. `getBookById` e `getUserBooks` são chamados em paralelo via `Promise.all`
2. Se o livro estiver na biblioteca (`userBookId` encontrado):
   - Status `TOREAD` → atualizado imediatamente para `READING` com `progress: 0`
   - `progress` salvo é restaurado no estado visual
3. Posição CFI salva no `localStorage` é restaurada no `ReactReader`

### Rastreamento contínuo via `getRendition`

O evento `relocated` do epub.js fornece `loc.start.percentage` (0–1) a cada virada de página. O leitor:

1. Converte para inteiro 0–100 (`Math.round(percentage * 100)`)
2. Exibe visualmente no header e na barra de progresso
3. Aguarda **2 segundos** sem navegação (debounce) antes de chamar `PUT /user-books/{id}`

```ts
rendition.on('relocated', (loc) => {
  const percent = Math.round(loc.start.percentage * 100);
  setProgress(percent);

  clearTimeout(saveTimer);
  saveTimer = setTimeout(() => {
    updateUserBook(userBookId, { status: 'READING', progress: percent });
  }, 2000);
});
```

### Indicadores visuais no leitor

| Elemento | Visível quando | Descrição |
|----------|---------------|-----------|
| `{progress}%` no header | Livro na biblioteca | Percentual numérico ao lado do autor |
| Barra abaixo do header | Livro na biblioteca | Faixa `bg-terra` proporcional ao progresso |

### `updateUserBook` — `api.ts`

```ts
updateUserBook(userBookId: number, data: { status?: string; progress?: number; rating?: number })
// PUT $NEXT_PUBLIC_API_URL/user-books/{userBookId}
```

---

## 7. Fluxo completo

```
/explorar/:id
  → usuário clica "Adicionar à Biblioteca"
  → POST /user-books { bookId, status: "TOREAD" } + Bearer token
  → backend valida JWT, salva em user_books
  → botão exibe "Adicionado à Biblioteca" (desabilitado)

/biblioteca
  → GET /user-books + Bearer token
  → backend retorna Page<UserBooksResponse> com livros do usuário
  → exibidos em grade 2×4 com filtros por status
  → capa clicável abre /leitor/:id diretamente (se downloadUrl existir)
  → capa exibe badge de status + barra de progresso (se READING)

/leitor/:id
  → Promise.all: getBookById + getUserBooks
  → se livro na biblioteca:
      status TOREAD → PUT /user-books/{id} { status: READING, progress: 0 }
      restaura progress salvo no estado visual
  → restaura posição CFI do localStorage
  → a cada virada de página: relocated → debounce 2s → PUT /user-books/{id} { status, progress }
  → exibe percentual e barra de progresso no header
```
