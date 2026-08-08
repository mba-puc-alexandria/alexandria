# Mudanças na API de Livros — Alinhamento Frontend

## O que mudou no backend

A API de livros agora retorna `authors` como **array de objetos** (com `id`, `name`, `birthYear`, `deathYear`) em vez de uma `author` string única.

---

## 1. Tipo `BookApiResponse` — `lib/api.ts`

### Antes
```typescript
export interface BookApiResponse {
  id: number;
  title: string;
  author: string;           // ← string única
  coverUrl: string | null;
  downloadUrl: string | null;
  languages: string | null;
  subjects: string | null;
  source: string;
  gutendexId: number | null;
  downloadCount: number | null;
  publisherId: number | null;
}
```

### Depois
```typescript
export interface AuthorInfo {
  id: number;
  name: string;
  birthYear: number | null;
  deathYear: number | null;
}

export interface BookApiResponse {
  id: number;
  title: string;
  authors: AuthorInfo[];    // ← array de objetos
  coverUrl: string | null;
  downloadUrl: string | null;
  languages: string | null;
  subjects: string | null;
  source: string;
  gutendexId: number | null;
  downloadCount: number | null;
  publisherId: number | null;
}
```

### Exemplo de resposta real da API
```json
{
  "id": 1,
  "title": "Amor Crioulo: vida argentina",
  "authors": [
    {
      "id": 1,
      "name": "Abel Acácio de Almeida Botelho",
      "birthYear": 1856,
      "deathYear": 1917
    }
  ],
  "coverUrl": "https://...",
  "languages": "pt",
  "subjects": "Portuguese fiction",
  "source": "GUTENDEX",
  "gutendexId": 24919,
  "downloadCount": 13153
}
```

---

## 2. `components/BookCard.tsx`

```diff
- <p className="text-brown-soft text-sm leading-5 mb-4">{book.author}</p>
+ <p className="text-brown-soft text-sm leading-5 mb-4">
+   {book.authors?.map(a => a.name).join(", ") || "Desconhecido"}
+ </p>
```

---

## 3. `app/(main)/explorar/page.tsx`

```diff
- <p className="text-slate text-sm">{book.author}</p>
+ <p className="text-slate text-sm">
+   {book.authors?.map(a => a.name).join(", ") || "Desconhecido"}
+ </p>
```

> Apenas 1 ocorrência (por volta da linha 106, visualização mobile em lista).

---

## 4. `app/(main)/explorar/[id]/page.tsx`

```diff
- <p className="text-brown-soft text-lg">{book.author}</p>
+ <p className="text-brown-soft text-lg">
+   {book.authors?.map(a => a.name).join(", ") || "Desconhecido"}
+ </p>
```

### 💡 Sugestão extra — exibir birthYear/deathYear na página de detalhes
```tsx
{book.authors?.map((a, i) => (
  <span key={a.id}>
    {i > 0 && ", "}
    {a.name} ({a.birthYear ?? "?"} – {a.deathYear ?? "?"})
  </span>
))}
```

---

## 5. `app/(main)/biblioteca/page.tsx`

### Filtro local por autor (por volta da linha 18)
```diff
- book.author.toLowerCase().includes(query.toLowerCase())
+ book.authors?.some(a => a.name.toLowerCase().includes(query.toLowerCase()))
```

### Exibição do nome do autor (por volta da linha 100)
```diff
- <p className="text-slate/70 text-xs mt-1">{book.author}</p>
+ <p className="text-slate/70 text-xs mt-1">
+   {book.authors?.map(a => a.name).join(", ") || "Desconhecido"}
+ </p>
```

---

## 6. Arquivos que NÃO precisam de alteração

| Arquivo | Motivo |
|---------|--------|
| `dashboard/page.tsx` | Usa dados mockados (`data/books.ts`), não a API |
| `leitor/page.tsx` | Usa dados mockados (`data/books.ts`), não a API |
| `leitor/[id]/page.tsx` | Usa dados mockados (`data/books.ts`), não a API |

---

## Resumo das alterações necessárias

| # | Arquivo | Tipo de mudança |
|---|---------|----------------|
| 1 | `lib/api.ts` | Adicionar interface `AuthorInfo` + atualizar `BookApiResponse.authors` |
| 2 | `components/BookCard.tsx` | `book.author` → `book.authors.map(a => a.name).join(", ")` |
| 3 | `explorar/page.tsx` | `book.author` → `book.authors.map(a => a.name).join(", ")` |
| 4 | `explorar/[id]/page.tsx` | `book.author` → `book.authors.map(a => a.name).join(", ")` |
| 5 | `biblioteca/page.tsx` | `book.author` no filtro e na exibição |
