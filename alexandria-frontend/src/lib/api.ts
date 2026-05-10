const API_URL = process.env.NEXT_PUBLIC_API_URL!;

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
  const res = await fetch(`${API_URL}/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data),
  });

  if (!res.ok) {
    const error = await res.json().catch(() => ({}));
    throw new Error((error as { message?: string }).message || 'Credenciais inválidas');
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
  const res = await fetch(`${API_URL}/auth/register`, {
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

export interface BookApiResponse {
  id: number;
  title: string;
  author: string;
  coverUrl: string | null;
  downloadUrl: string | null;
  languages: string | null;
  subjects: string | null;
  source: string;
  gutendexId: number | null;
  downloadCount: number | null;
  publisherId: number | null;
}

export interface BooksPage {
  content: BookApiResponse[];
  totalPages: number;
  totalElements: number;
  number: number;
  size: number;
}

export async function getBooks(page = 0, size = 10): Promise<BooksPage> {
  const res = await fetch(
    `${process.env.NEXT_PUBLIC_API_URL}/books?page=${page}&size=${size}`
  );
  if (!res.ok) throw new Error('Falha ao buscar livros');
  return res.json();
}

export async function searchBooks(query: string, page = 0, size = 10): Promise<BooksPage> {
  const res = await fetch(
    `${process.env.NEXT_PUBLIC_API_URL}/books/search?query=${encodeURIComponent(query)}&page=${page}&size=${size}`
  );
  if (!res.ok) throw new Error('Falha ao buscar livros');
  return res.json();
}

export function getAuthHeaders(): HeadersInit {
  const token = typeof window !== 'undefined' ? localStorage.getItem('auth-token') : null;
  return token ? { Authorization: `Bearer ${token}` } : {};
}

export async function apiFetch(path: string, init: RequestInit = {}): Promise<Response> {
  return fetch(`${API_URL}${path}`, {
    ...init,
    headers: {
      'Content-Type': 'application/json',
      ...getAuthHeaders(),
      ...init.headers,
    },
  });
}
