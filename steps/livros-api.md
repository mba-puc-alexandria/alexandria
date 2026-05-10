# Livros — Integração com a API

## Contexto

As páginas `/explorar` e `/biblioteca` usavam dados mockados em `src/data/books.ts`.  
Este step substitui os mocks por chamadas reais à API do backend (`GET /books` e `GET /books/search`).

---

## Endpoints utilizados

| Método | Rota | Auth | Descrição |
|--------|------|------|-----------|
| `GET` | `/books?page=0&size=10` | Não | Lista livros paginados |
| `GET` | `/books/search?query=...&page=0&size=10` | Não | Busca livros por título |

Ambos retornam uma `Page<BookResponse>`:

```json
{
  "content": [
    {
      "id": 1,
      "title": "Dom Quixote",
      "author": "Miguel de Cervantes",
      "coverUrl": "https://...",
      "downloadUrl": "https://...",
      "languages": "pt",
      "subjects": "Fiction",
      "source": "GUTENDEX",
      "gutendexId": 996,
      "downloadCount": 12000,
      "publisherId": null
    }
  ],
  "totalPages": 5,
  "totalElements": 48,
  "number": 0,
  "size": 10
}
```

---

## Arquivos modificados

### `src/lib/api.ts`

Adicionados tipo e funções:

- `BookApiResponse` — interface mapeando o `BookResponse` do backend
- `BooksPage` — interface para a resposta paginada
- `getBooks(page, size)` → `GET /books`
- `searchBooks(query, page, size)` → `GET /books/search`

### `src/components/BookCard.tsx`

Atualizado para aceitar `BookApiResponse` em vez do tipo mock `Book`:

- `rating` removido (não existe na tabela `books` — fica em `user_books`, implementação futura)
- `cover` substituído por `coverUrl` (pode ser `null` — exibe placeholder "Sem capa")
- Botão de hover chama `onAdd?(book)` passando o objeto completo

### `src/app/(main)/explorar/page.tsx`

Convertida para Client Component (`'use client'`):

- `useEffect` busca `getBooks(0, 10)` na montagem
- Input de busca chama `searchBooks(query)` no submit do formulário
- Limpar a busca volta a listar todos os livros
- Seção "Recomendado" substituída por dados da API
- Estado de loading com skeleton animado (5 cards)
- Estado vazio: mensagem "Nenhum livro encontrado"
- Curadorias em Destaque permanecem estáticas (dados editoriais, sem vínculo com o banco)

### `src/app/(main)/biblioteca/page.tsx`

Convertida para Client Component (`'use client'`):

- `useEffect` busca `getBooks(0, 20)` na montagem
- Busca local (filtro por `title` e `author` no array já carregado)
- Contador de livros no título (`X livros`)
- Estado de loading com skeleton animado (8 cards)
- Estado vazio com mensagem contextual (busca vazia vs biblioteca vazia)
- Badges de status removidos temporariamente — dependem da tabela `user_books`

---

## Limitações desta implementação

### Filtros de status (`/biblioteca`)

Os filtros "Lendo", "Concluído", "Para Ler", "Emprestado" estão na UI mas não filtram dados.  
O status de leitura fica na tabela `user_books` (relação usuário ↔ livro), que ainda não foi integrada ao frontend.

**Próximo step:** integrar `GET /user-books` com JWT para exibir o acervo pessoal real com status e progresso.

### Paginação

Atualmente carrega uma página fixa (`size=10` em Explorar, `size=20` em Biblioteca).  
Paginação com botões ou scroll infinito pode ser adicionada futuramente.

### Busca em `/biblioteca`

A busca atual filtra localmente no array já carregado.  
Para bases grandes, substituir por chamada a `GET /books/search` com debounce.

---

## Fluxo de dados

```
Componente monta
      ↓
useEffect → getBooks() → GET /books
      ↓
setBooks(page.content)
      ↓
Renderiza grid de livros

Usuário digita na busca (Explorar)
      ↓
handleSearch → searchBooks(query) → GET /books/search?query=...
      ↓
setBooks(page.content)
      ↓
Título da seção muda para "Resultados para 'query'"
```
