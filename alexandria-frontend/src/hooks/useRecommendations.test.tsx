import { act, useEffect } from 'react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { render, click } from '@/test-utils/render';
import { ApiError, type RecommendationResponse } from '@/lib/api';
import { useRecommendations } from './useRecommendations';

const api = vi.hoisted(() => ({ get: vi.fn(), generate: vi.fn() }));
vi.mock('@/lib/api', async (original) => ({
  ...await original<typeof import('@/lib/api')>(),
  getRecommendations: api.get,
  requestRecommendationGeneration: api.generate,
}));

const ready: RecommendationResponse = {
  state: 'READY', generationStatus: 'READY', updatePending: false,
  emptyReason: null, retryAfterSeconds: null, message: null,
  selection: { id: 1, referenceDate: '2026-10-30', generatedAt: '2026-10-30T03:00:00Z', algorithmVersion: 'content-v1', selectedCount: 1 },
  items: [{ position: 1, book: { id: 8, title: 'Livro oito', authors: [], coverUrl: null }, reason: { type: 'SUBJECT', matchedValue: 'ficção', text: 'Assunto presente na biblioteca nessa data.' } }],
};
const result = (data: RecommendationResponse, status = 200, retryAfterSeconds: number | null = null) => ({ data, status, retryAfterSeconds });
function Harness({ userId }: { userId: number | null }) {
  const value = useRecommendations(userId);
  return <>
    <output>{JSON.stringify({ data: value.data, loading: value.loading, error: value.error, canRetry: value.canRetry, pollsExhausted: value.pollsExhausted })}</output>
    <button onClick={() => void value.refresh()}>Verificar</button>
    <button onClick={() => void value.retry()}>Tentar</button>
    <button onClick={() => value.removeConfirmed(8, 'added')}>Retirar</button>
  </>;
}
let mounted: Awaited<ReturnType<typeof render>> | undefined;
beforeEach(() => { vi.clearAllMocks(); api.get.mockResolvedValue(result(ready)); api.generate.mockResolvedValue({ status: 202, data: { state: 'ACCEPTED', referenceDate: '2026-10-30' }, retryAfterSeconds: 3 }); });
afterEach(async () => { await mounted?.unmount(); mounted = undefined; vi.useRealTimers(); });
const state = () => JSON.parse(mounted!.container.querySelector('output')!.textContent!);
const button = (text: string) => [...mounted!.container.querySelectorAll('button')].find(b => b.textContent === text)!;
async function settle() { await act(async () => { await Promise.resolve(); await Promise.resolve(); }); }

describe('useRecommendations — carregamento e recuperação', () => {
  it('não consulta sem conta e carrega a seleção inteira para uma conta', async () => {
    mounted = await render(<Harness userId={null} />);
    expect(api.get).not.toHaveBeenCalled();
    await mounted.rerender(<Harness userId={1} />);
    expect(state().data.items[0].book.id).toBe(8);
    expect(state().loading).toBe(false);
  });

  it('respeita Retry-After e limita a dez consultas automáticas por abertura', async () => {
    vi.useFakeTimers();
    api.get.mockResolvedValue(result({ ...ready, state: 'GENERATING', generationStatus: 'BUILDING', selection: null, items: [], updatePending: true, retryAfterSeconds: 5 }, 202, 5));
    mounted = await render(<Harness userId={1} />);
    await act(async () => { await vi.advanceTimersByTimeAsync(4_999); });
    expect(api.get).toHaveBeenCalledTimes(1);
    for (let i = 0; i < 10; i++) await act(async () => { await vi.advanceTimersByTimeAsync(5_000); });
    expect(api.get).toHaveBeenCalledTimes(10);
    expect(state().pollsExhausted).toBe(true);
  });

  it('STALE vazia preserva data, não faz polling e verifica por GET', async () => {
    vi.useFakeTimers();
    api.get.mockResolvedValue(result({ ...ready, state: 'STALE', generationStatus: 'PENDING', items: [], updatePending: true }));
    mounted = await render(<Harness userId={1} />);
    await act(async () => { await vi.advanceTimersByTimeAsync(60_000); });
    expect(api.get).toHaveBeenCalledTimes(1);
    expect(state().data.selection.referenceDate).toBe('2026-10-30');
    await click(button('Tentar'));
    expect(api.generate).not.toHaveBeenCalled();
    expect(api.get).toHaveBeenCalledTimes(2);
  });

  it('usa POST apenas para FAILED e só após o cooldown', async () => {
    vi.useFakeTimers();
    api.get.mockRejectedValue(new ApiError('Indisponível', 503, undefined, 'FAILED', 5));
    mounted = await render(<Harness userId={1} />);
    expect(state().canRetry).toBe(false);
    await click(button('Tentar'));
    expect(api.generate).not.toHaveBeenCalled();
    await act(async () => { await vi.advanceTimersByTimeAsync(5_000); });
    expect(state().canRetry).toBe(true);
    expect(vi.getTimerCount()).toBe(0);
    api.get.mockResolvedValue(result(ready));
    await click(button('Tentar'));
    expect(api.generate).toHaveBeenCalledTimes(1);
  });

  it('preserva os cartões em falha de transporte e repete somente GET', async () => {
    mounted = await render(<Harness userId={1} />);
    api.get.mockRejectedValueOnce(new TypeError('rede'));
    await click(button('Verificar'));
    expect(state().data.items[0].book.id).toBe(8);
    expect(state().error.message).toContain('recomendações');
    await click(button('Tentar'));
    expect(api.generate).not.toHaveBeenCalled();
  });

  it('NO_PROFILE limpa sugestões antigas e não implica biblioteca vazia', async () => {
    mounted = await render(<Harness userId={1} />);
    api.get.mockResolvedValue(result({ ...ready, state: 'EMPTY', selection: null, items: [], emptyReason: 'NO_PROFILE', generationStatus: 'NO_PROFILE' }));
    await click(button('Verificar'));
    expect(state().data.items).toEqual([]);
    expect(state().data.selection).toBeNull();
    expect(state().data.emptyReason).toBe('NO_PROFILE');
  });

  it('429 após tentativa mantém FAILED e cooldown sem novos POSTs precoces', async () => {
    vi.useFakeTimers();
    api.get.mockRejectedValue(new ApiError('Indisponível', 503, undefined, 'FAILED', null));
    mounted = await render(<Harness userId={1} />);
    api.generate.mockRejectedValue(new ApiError('Aguarde', 429, undefined, 'FAILED', 3));
    await click(button('Tentar'));
    expect(state().canRetry).toBe(false);
    await click(button('Tentar'));
    expect(api.generate).toHaveBeenCalledTimes(1);
    await act(async () => { await vi.advanceTimersByTimeAsync(3_000); });
    expect(state().canRetry).toBe(true);
  });

  it('401 remove os dados imediatamente e encerra a sessão pelo callback', async () => {
    const logout = vi.fn();
    let snapshot: ReturnType<typeof useRecommendations> | undefined;
    function AuthHarness() {
      const value = useRecommendations(1, logout);
      useEffect(() => { snapshot = value; }, [value]);
      return <output>{value.data?.items.length ?? 'sem dados'}</output>;
    }
    mounted = await render(<AuthHarness />);
    api.get.mockRejectedValue(new ApiError('Sessão expirada', 401));
    await act(async () => { await snapshot!.refresh(); });
    expect(logout).toHaveBeenCalledOnce();
    expect(snapshot!.data).toBeNull();
  });

  it('aborta a conta anterior e ignora respostas atrasadas após troca/logout', async () => {
    let finish!: (data: ReturnType<typeof result>) => void;
    api.get.mockImplementationOnce(() => new Promise(resolve => { finish = resolve; }));
    mounted = await render(<Harness userId={1} />);
    const signal = api.get.mock.calls[0][0] as AbortSignal;
    await mounted.rerender(<Harness userId={2} />);
    expect(signal.aborted).toBe(true);
    await act(async () => { finish(result({ ...ready, items: [{ ...ready.items[0], book: { ...ready.items[0].book, title: 'Conta anterior' } }] })); });
    expect(mounted.container).not.toHaveTextContent('Conta anterior');
    await mounted.rerender(<Harness userId={null} />);
    await settle();
    expect(state().data).toBeNull();
  });

  it('retira confirmação de inclusão sem reposição e ignora GET anterior', async () => {
    mounted = await render(<Harness userId={1} />);
    let finish!: (data: ReturnType<typeof result>) => void;
    api.get.mockImplementationOnce(() => new Promise(resolve => { finish = resolve; }));
    await click(button('Verificar'));
    api.get.mockResolvedValue(result({ ...ready, state: 'EMPTY', items: [], emptyReason: 'ALL_ADDED_OR_UNAVAILABLE' }));
    await click(button('Retirar'));
    await act(async () => { finish(result(ready)); });
    expect(state().data.items).toEqual([]);
    expect(api.generate).not.toHaveBeenCalled();
  });
});
