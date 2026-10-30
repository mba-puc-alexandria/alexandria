import { act, useEffect } from 'react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { render } from '@/test-utils/render';
import type { RecommendationResponse } from '@/lib/api';
import { emitLibraryChange } from '@/lib/library-events';
import { useRecommendations } from './useRecommendations';

const api = vi.hoisted(() => ({ get: vi.fn(), generate: vi.fn() }));
vi.mock('@/lib/api', async original => ({ ...await original<typeof import('@/lib/api')>(), getRecommendations: api.get, requestRecommendationGeneration: api.generate }));
const ready: RecommendationResponse = { state: 'READY', generationStatus: 'READY', selection: { id: 1, referenceDate: '2026-10-30', generatedAt: '2026-10-30T03:00:00Z', algorithmVersion: 'content-v1', selectedCount: 1 }, items: [{ position: 1, book: { id: 42, title: 'Livro', authors: [], coverUrl: null }, reason: { type: 'SUBJECT', matchedValue: 'ficção', text: 'Assunto presente nessa data.' } }], emptyReason: null, updatePending: false, retryAfterSeconds: null, message: null };
const response = (data: RecommendationResponse) => ({ data, status: 200, retryAfterSeconds: null });
let snapshot: ReturnType<typeof useRecommendations>;
let screen: Awaited<ReturnType<typeof render>>;
function Harness({ id = 1 }: { id?: number | null }) {
  const value = useRecommendations(id);
  useEffect(() => { snapshot = value; }, [value]);
  return <output>{value.notice}{value.data?.items.map(item => item.book.title).join(',')}</output>;
}
beforeEach(() => {
  vi.clearAllMocks();
  localStorage.clear();
  localStorage.setItem('auth-user', JSON.stringify({ userId: 1 }));
  api.get.mockResolvedValue(response(ready));
});
afterEach(async () => { await screen?.unmount(); });

describe('consistência da biblioteca no hook', () => {
  it.each(['added', 'already_added'] as const)('retira evento %s, coalesce callback e não confirma criação em409', async kind => {
    screen = await render(<Harness />);
    api.get.mockResolvedValue(response({ ...ready, state: 'EMPTY', items: [], emptyReason: 'ALL_ADDED_OR_UNAVAILABLE' }));
    await act(async () => {
      emitLibraryChange({ accountId: 1, kind, bookId: 42 });
      snapshot.removeConfirmed(42, kind);
    });
    expect(snapshot.data?.items).toEqual([]);
    expect(api.get).toHaveBeenCalledTimes(2);
    expect(api.generate).not.toHaveBeenCalled();
    if (kind === 'already_added') {
      expect(snapshot.notice).toBe('Este livro já está na sua biblioteca.');
      expect(snapshot.notice).not.toBe('Livro adicionado à biblioteca.');
    }
  });

  it('uma remoção explícita permite reaparecer sem recalcular seleção', async () => {
    screen = await render(<Harness />);
    api.get.mockResolvedValueOnce(response({ ...ready, state: 'EMPTY', items: [], emptyReason: 'ALL_ADDED_OR_UNAVAILABLE' }));
    await act(async () => { snapshot.removeConfirmed(42, 'added'); });
    expect(snapshot.data?.items).toHaveLength(0);
    api.get.mockResolvedValue(response(ready));
    await act(async () => { emitLibraryChange({ accountId: 1, kind: 'removed' }); });
    expect(snapshot.data?.items[0].book.id).toBe(42);
    expect(api.generate).not.toHaveBeenCalled();
  });

  it('uma invalidação de outra aba revalida; logout limpa e remove listeners', async () => {
    screen = await render(<Harness />);
    await act(async () => { window.dispatchEvent(new StorageEvent('storage', { key: 'alexandria-library-invalidation', newValue: 'invalidado' })); });
    expect(api.get).toHaveBeenCalledTimes(2);
    await screen.rerender(<Harness id={null} />);
    const calls = api.get.mock.calls.length;
    await act(async () => { emitLibraryChange({ accountId: 1, kind: 'updated' }); window.dispatchEvent(new Event('focus')); });
    expect(api.get).toHaveBeenCalledTimes(calls);
    expect(snapshot.data).toBeNull();
  });
});
