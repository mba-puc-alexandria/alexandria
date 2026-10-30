import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { addUserBook, ApiError, getRecommendations, requestRecommendationGeneration, updateUserBook, removeUserBook } from './api';
import { subscribeLibraryChanges, type LibraryChange } from './library-events';

const fetchMock = vi.fn();
beforeEach(() => {
  vi.stubGlobal('fetch', fetchMock);
  fetchMock.mockReset();
  localStorage.clear();
  localStorage.setItem('auth-token', 'test-token');
  localStorage.setItem('auth-user', JSON.stringify({ userId: 7 }));
});
afterEach(() => { vi.unstubAllGlobals(); });
const response = (value: object, status = 200, headers?: HeadersInit) => new Response(JSON.stringify(value), { status, headers });

describe('contrato do cliente de recomendações', () => {
  it('propaga JWT, AbortSignal, no-store e Retry-After exposto', async () => {
    fetchMock.mockResolvedValue(response({ state: 'GENERATING', retryAfterSeconds: 3 }, 202, { 'Retry-After': '9' }));
    const controller = new AbortController();
    const result = await getRecommendations(controller.signal);
    expect(result.status).toBe(202);
    expect(result.retryAfterSeconds).toBe(9);
    expect(fetchMock.mock.calls[0][1]).toMatchObject({ signal: controller.signal, cache: 'no-store', headers: { Authorization: 'Bearer test-token' } });
  });
  it('preserva generationStatus e cooldown no503 e429 sem inferir texto', async () => {
    fetchMock.mockResolvedValue(response({ generationStatus: 'FAILED', retryAfterSeconds: null }, 503));
    await expect(getRecommendations()).rejects.toMatchObject({ status: 503, generationStatus: 'FAILED', retryAfterSeconds: null });
    fetchMock.mockResolvedValue(response({ message: 'texto livre' }, 429, { 'Retry-After': '60' }));
    await expect(requestRecommendationGeneration()).rejects.toMatchObject({ status: 429, generationStatus: 'FAILED', retryAfterSeconds: 60 });
  });
  it('preserva status/código de inclusão e compatibilidade already_added', async () => {
    fetchMock.mockResolvedValue(response({ code: 'BOOK_NOT_FOUND' }, 404));
    await expect(addUserBook(42)).rejects.toMatchObject({ status: 404, code: 'BOOK_NOT_FOUND' });
    fetchMock.mockResolvedValue(response({}, 409));
    await expect(addUserBook(42)).rejects.toEqual(expect.objectContaining({ message: 'already_added', status: 409 }));
  });
  it('notifica somente mutações confirmadas e cruza abas apenas com invalidação', async () => {
    const changes: LibraryChange[] = [];
    const unsubscribe = subscribeLibraryChanges(value => changes.push(value));
    fetchMock.mockResolvedValue(response({}, 500));
    await expect(addUserBook(42)).rejects.toBeInstanceOf(ApiError);
    expect(changes).toEqual([]);
    fetchMock.mockResolvedValueOnce(response({ id: 3, book: { id: 42 } }));
    await addUserBook(42);
    fetchMock.mockResolvedValueOnce(response({ id: 3, book: { id: 42 } }));
    await updateUserBook(3, { status: 'READING' });
    fetchMock.mockResolvedValue(new Response(null, { status: 204 }));
    await removeUserBook(3);
    expect(changes.map(change => change.kind)).toEqual(['added', 'updated', 'removed']);
    const marker = localStorage.getItem('alexandria-library-invalidation')!;
    expect(marker).toMatch(/^\d+:0\.\d+$/);
    window.dispatchEvent(new StorageEvent('storage', { key: 'alexandria-library-invalidation', newValue: marker }));
    expect(changes.at(-1)).toEqual({ kind: 'invalidated', accountId: 7 });
    unsubscribe();
  });
  it('não publica evento de uma mutação da conta anterior', async () => {
    const events = vi.fn();
    const unsubscribe = subscribeLibraryChanges(events);
    let finish!: (value: Response) => void;
    fetchMock.mockImplementation(() => new Promise(resolve => { finish = resolve; }));
    const pending = addUserBook(42);
    localStorage.setItem('auth-user', JSON.stringify({ userId: 9 }));
    finish(response({ id: 3, book: { id: 42 } }));
    await pending;
    expect(events).not.toHaveBeenCalled();
    unsubscribe();
  });
});
