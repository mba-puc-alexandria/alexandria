import { emitLibraryChange, libraryAccountId } from './library-events';

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
    credentials: 'include',
    body: JSON.stringify(data),
  });

  if (!res.ok) {
    throw new Error('Usuário ou senha incorretos');
  }

  return res.json();
}

export async function loginWithGoogle(credential: string): Promise<LoginResponse> {
  const res = await fetch(`${API_URL_LOCAL}/auth/google`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include',
    body: JSON.stringify({ credential }),
  });

  if (!res.ok) {
    throw new Error('Falha no login com Google');
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

export function getAuthorDisplay(book: { authors: string | { name: string }[] }): string {
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
  const accountId = libraryAccountId();
  const res = await apiFetch('/user-books', {
    method: 'POST',
    body: JSON.stringify({ bookId, status: 'TOREAD' }),
  });
  if (res.status === 409) {
    emitLibraryChange({ kind: 'already_added', accountId, bookId });
    throw new ApiError('already_added', 409);
  }
  if (!res.ok) {
    const error = await res.json().catch(() => ({}));
    throw new ApiError('Falha ao adicionar livro', res.status, error.code);
  }
  const data: UserBookResponse = await res.json();
  emitLibraryChange({ kind: 'added', accountId, bookId });
  return data;
}

export async function getUserBooks(signal?: AbortSignal): Promise<UserBookResponse[]> {
  const res = await apiFetch('/user-books', { signal });
  if (!res.ok) throw new Error('Falha ao buscar biblioteca');
  const page = await res.json();
  return page.content;
}

export async function updateUserBook(
  userBookId: number,
  data: { status?: string; progress?: number; rating?: number }
): Promise<UserBookResponse> {
  const accountId = libraryAccountId();
  const res = await apiFetch(`/user-books/${userBookId}`, {
    method: 'PUT',
    body: JSON.stringify(data),
  });
  if (!res.ok) throw new Error('Falha ao atualizar livro');
  const updated: UserBookResponse = await res.json();
  emitLibraryChange({ kind: 'updated', accountId, bookId: updated.book.id });
  return updated;
}

export async function removeUserBook(userBookId: number): Promise<void> {
  const accountId = libraryAccountId();
  const res = await apiFetch(`/user-books/${userBookId}`, {
    method: 'DELETE',
  });
  if (!res.ok) throw new Error('Falha ao remover livro da biblioteca');
  emitLibraryChange({ kind: 'removed', accountId });
}

export type RecommendationGenerationStatus = 'IDLE' | 'PENDING' | 'BUILDING' | 'FAILED' | 'READY' | 'NO_PROFILE';
export interface RecommendationSelection {
  id: number;
  referenceDate: string;
  generatedAt: string;
  algorithmVersion: string;
  selectedCount: number;
}
export interface RecommendationItem {
  position: number;
  book: { id: number; title: string; authors: { id: number; name: string }[]; coverUrl: string | null };
  reason: { type: 'SUBJECT' | 'AUTHOR'; matchedValue: string; text: string };
}
export interface RecommendationResponse {
  state: 'READY' | 'EMPTY' | 'STALE' | 'GENERATING';
  selection: RecommendationSelection | null;
  items: RecommendationItem[];
  emptyReason: 'NO_PROFILE' | 'NO_MATCHES' | 'ALL_ADDED_OR_UNAVAILABLE' | null;
  updatePending: boolean;
  retryAfterSeconds: number | null;
  generationStatus: RecommendationGenerationStatus;
  message: string | null;
}
export interface RecommendationResult {
  data: RecommendationResponse;
  status: number;
  retryAfterSeconds: number | null;
}
export interface RecommendationGenerationResult {
  data: { state: 'ACCEPTED' | 'ALREADY_RUNNING' | 'READY' | 'NO_PROFILE'; referenceDate: string };
  status: number;
  retryAfterSeconds: number | null;
}
export class ApiError extends Error {
  constructor(
    message: string,
    public readonly status: number,
    public readonly code?: string,
    public readonly generationStatus?: RecommendationGenerationStatus,
    public readonly retryAfterSeconds: number | null = null,
  ) { super(message); this.name = 'ApiError'; }
}

function retryAfter(response: Response, fallback?: number | null): number | null {
  const seconds = Number(response.headers.get('Retry-After'));
  if (Number.isFinite(seconds) && seconds >= 1) return Math.ceil(seconds);
  return typeof fallback === 'number' && Number.isFinite(fallback) && fallback >= 1 ? Math.ceil(fallback) : null;
}
async function recommendationRequest(path: string, init: RequestInit) {
  const response = await apiFetch(path, { ...init, cache: 'no-store' });
  const data = await response.json().catch(() => ({}));
  const delay = retryAfter(response, data.retryAfterSeconds);
  if (!response.ok) throw new ApiError(
    response.status === 401 ? 'Sessão expirada. Entre de novo.' : 'Não foi possível atualizar suas recomendações.',
    response.status, data.code,
    data.generationStatus ?? (path === '/recommendations/generation' && response.status === 429 ? 'FAILED' : undefined), delay,
  );
  return { data, status: response.status, retryAfterSeconds: delay };
}
export function getRecommendations(signal?: AbortSignal): Promise<RecommendationResult> {
  return recommendationRequest('/recommendations', { signal });
}
export function requestRecommendationGeneration(signal?: AbortSignal): Promise<RecommendationGenerationResult> {
  return recommendationRequest('/recommendations/generation', { method: 'POST', signal });
}

export interface ProfileResponse {
  userId: number;
  username: string;
  firstName: string;
  lastName: string;
  email: string;
  createdAt: string;
}

export async function getProfile(): Promise<ProfileResponse> {
  const res = await apiFetch('/profile/me');
  if (!res.ok) throw new Error('Falha ao buscar perfil');
  return res.json();
}

export interface UpdateProfileRequest {
  username: string;
  firstName: string;
  lastName: string;
}

export async function updateProfile(data: UpdateProfileRequest): Promise<ProfileResponse> {
  const res = await apiFetch('/profile/me', {
    method: 'PUT',
    body: JSON.stringify(data),
  });
  if (!res.ok) {
    const error = await res.json().catch(() => ({}));
    throw new Error((error as { message?: string }).message || 'Falha ao atualizar perfil');
  }
  return res.json();
}

export interface UpdatePasswordRequest {
  currentPassword: string;
  newPassword: string;
}

export async function updatePassword(data: UpdatePasswordRequest): Promise<void> {
  const res = await apiFetch('/profile/password', {
    method: 'PUT',
    body: JSON.stringify(data),
  });
  if (!res.ok) {
    const error = await res.json().catch(() => ({}));
    throw new Error((error as { message?: string }).message || 'Falha ao atualizar senha');
  }
}

export interface Subscription {
  status: 'TRIALING' | 'ACTIVE' | 'PAST_DUE' | 'EXPIRED' | 'CANCELED';
  trialEndsAt: string | null;
  currentPeriodEndsAt: string | null;
  price: number;
  currency: string;
  periodDays: number;
  paymentScheduled: boolean;
}

export async function getSubscription(): Promise<Subscription> {
  const res = await apiFetch('/subscriptions/me');
  if (!res.ok) {
    if (res.status === 404) {
      throw new Error('subscription_not_found');
    }
    throw new Error('Falha ao buscar assinatura');
  }
  return res.json();
}

export interface CheckoutRequest {
  paymentMethod: 'PIX' | 'CARD';
  cardToken?: string;
  cardBrand?: string;
  installments?: number;
  payerEmail?: string;
  payerDocumentType?: string;
  payerDocumentNumber?: string;
}

export interface CheckoutResponse {
  status: string;
  paymentId: string;
  mpPaymentId: number | null;
  qrCode: string | null;
  qrCodeBase64: string | null;
  ticketUrl: string | null;
  subscriptionStatus: string;
  message: string;
}

export async function createCheckout(data: CheckoutRequest): Promise<CheckoutResponse> {
  const res = await apiFetch('/subscriptions/checkout', {
    method: 'POST',
    body: JSON.stringify(data),
  });
  if (!res.ok) {
    const error = await res.json().catch(() => ({}));
    throw new Error((error as { message?: string }).message || 'Falha ao criar pagamento');
  }
  return res.json();
}

export async function cancelSubscription(): Promise<void> {
  const res = await apiFetch('/subscriptions/cancel', {
    method: 'POST',
  });
  if (!res.ok) {
    throw new Error('Falha ao cancelar assinatura');
  }
}

export async function updateSubscriptionPaymentMethod(data: {
  cardToken: string;
  cardBrand?: string;
}): Promise<void> {
  const res = await apiFetch('/subscriptions/payment-method', {
    method: 'POST',
    body: JSON.stringify(data),
  });
  if (!res.ok) {
    const error = await res.json().catch(() => ({}));
    throw new Error((error as { message?: string }).message || 'Falha ao atualizar cartão');
  }
}

export function getAuthHeaders(): HeadersInit {
  const token = typeof window !== 'undefined' ? localStorage.getItem('auth-token') : null;
  return token ? { Authorization: `Bearer ${token}` } : {};
}

export async function apiFetch(path: string, init: RequestInit = {}): Promise<Response> {
  return fetch(`${API_URL_LOCAL}${path}`, {
    ...init,
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json',
      ...getAuthHeaders(),
      ...init.headers,
    },
  });
}

export interface SupportMessage {
  role: 'user' | 'assistant';
  content: string;
}

export async function sendSupportMessage(messages: SupportMessage[]): Promise<string> {
  const res = await apiFetch('/support/chat', {
    method: 'POST',
    body: JSON.stringify({ messages }),
  });
  if (!res.ok) {
    throw new Error(res.status === 401 ? 'Sessão expirada. Entre de novo.' : `Falha ao falar com o assistente (erro ${res.status})`);
  }
  const data: { reply: string } = await res.json();
  return data.reply;
}
