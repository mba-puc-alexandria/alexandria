const API_URL = process.env.NEXT_PUBLIC_API_URL!;
const API_URL_LOCAL = API_URL;

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  userId: number;
  username: string;
}

export async function login(data: LoginRequest): Promise<LoginResponse> {
  const res = await fetch(`${API_URL_LOCAL}/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data),
  });

  if (!res.ok) {
    throw new Error('Usuário ou senha incorretos');
  }

  return res.json();
}

export interface RegisterRequest {
  username: string;
  firstName: string;
  lastName: string;
  email: string;
  password: string;
}

export interface RegisterResponse {
  userId: number;
  username: string;
  email: string;
}

export async function register(data: RegisterRequest): Promise<RegisterResponse> {
  const res = await fetch(`${API_URL_LOCAL}/auth/register`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data),
  });

  if (!res.ok) {
    const error = await res.json().catch(() => ({}));
    throw new Error((error as { message?: string }).message || 'Erro ao criar conta');
  }

  return res.json();
}

export interface AuthorInfo {
  id: number;
  name: string;
  birthYear: number | null;
  deathYear: number | null;
}

export interface BookApiResponse {
  id: number;
  title: string;
  authors: string | AuthorInfo[];
  coverUrl: string | null;
  downloadUrl: string | null;
  languages: string | null;
  subjects: string | null;
  source: string;
  gutendexId: number | null;
  downloadCount: number | null;
  publisherId: number | null;
}

export function getAuthorDisplay(book: BookApiResponse): string {
  if (typeof book.authors === 'string') return book.authors;
  return book.authors.map((a) => a.name).join(', ');
}

export interface BooksPage {
  content: BookApiResponse[];
  totalPages: number;
  totalElements: number;
  number: number;
  size: number;
}

export async function getBooks(page = 0, size = 10, language?: string): Promise<BooksPage> {
  const params = new URLSearchParams({ page: String(page), size: String(size) });
  if (language) params.set('language', language);
  const res = await fetch(`${API_URL_LOCAL}/books?${params}`);
  if (!res.ok) throw new Error('Falha ao buscar livros');
  return res.json();
}

export async function getTopBooks(size = 4): Promise<BookApiResponse[]> {
  const res = await fetch(
    `${API_URL_LOCAL}/books?page=0&size=${size}&sort=downloadCount,desc`
  );
  if (!res.ok) throw new Error('Falha ao buscar livros');
  const page: BooksPage = await res.json();
  return page.content;
}

export async function searchBooks(query: string, page = 0, size = 10): Promise<BooksPage> {
  const res = await fetch(
    `${API_URL_LOCAL}/books/search?query=${encodeURIComponent(query)}&page=${page}&size=${size}`
  );
  if (!res.ok) throw new Error('Falha ao buscar livros');
  return res.json();
}

export async function getBookById(id: number): Promise<BookApiResponse> {
  const res = await fetch(`${API_URL_LOCAL}/books/${id}`);
  if (!res.ok) throw new Error('Livro não encontrado');
  return res.json();
}

export interface UserBookResponse {
  id: number;
  book: BookApiResponse;
  status: string;
  progress: number | null;
  rating: number | null;
}

export async function addUserBook(bookId: number): Promise<UserBookResponse> {
  const res = await apiFetch('/user-books', {
    method: 'POST',
    body: JSON.stringify({ bookId, status: 'TOREAD' }),
  });
  if (res.status === 409) throw new Error('already_added');
  if (!res.ok) throw new Error('Falha ao adicionar livro');
  return res.json();
}

export async function getUserBooks(): Promise<UserBookResponse[]> {
  const res = await apiFetch('/user-books');
  if (!res.ok) throw new Error('Falha ao buscar biblioteca');
  const page = await res.json();
  return page.content;
}

export async function updateUserBook(
  userBookId: number,
  data: { status?: string; progress?: number; rating?: number }
): Promise<UserBookResponse> {
  const res = await apiFetch(`/user-books/${userBookId}`, {
    method: 'PUT',
    body: JSON.stringify(data),
  });
  if (!res.ok) throw new Error('Falha ao atualizar livro');
  return res.json();
}

export function getAuthHeaders(): HeadersInit {
  const token = typeof window !== 'undefined' ? localStorage.getItem('auth-token') : null;
  return token ? { Authorization: `Bearer ${token}` } : {};
}

export async function apiFetch(path: string, init: RequestInit = {}): Promise<Response> {
  return fetch(`${API_URL_LOCAL}${path}`, {
    ...init,
    headers: {
      'Content-Type': 'application/json',
      ...getAuthHeaders(),
      ...init.headers,
    },
  });
}
