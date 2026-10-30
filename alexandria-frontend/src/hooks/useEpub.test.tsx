import { act } from 'react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { render } from '@/test-utils/render';
import { useEpub } from './useEpub';

const cache = vi.hoisted(() => ({ get: vi.fn(), save: vi.fn(), mark: vi.fn() }));
vi.mock('@/lib/epub-cache', () => ({
  getCachedEpub: cache.get,
  saveEpubToCache: cache.save,
  markBookAsAccessed: cache.mark,
}));

function Harness({ ownerId }: Readonly<{ ownerId: number }>) {
  const result = useEpub('7', ownerId);
  return <div>{result.error?.message ?? (result.epubData ? `EPUB ${new Uint8Array(result.epubData)[0]}` : 'Sem EPUB')}</div>;
}

beforeEach(() => {
  cache.get.mockReset();
  cache.save.mockReset();
  cache.mark.mockReset();
});
afterEach(() => vi.restoreAllMocks());

describe('EPUB em cache', () => {
  it('não exibe bytes em cache quando a autorização atual retorna 402', async () => {
    vi.spyOn(navigator, 'onLine', 'get').mockReturnValue(true);
    localStorage.setItem('auth-user', JSON.stringify({ userId: 1 }));
    cache.get.mockResolvedValue(new Uint8Array([11]).buffer);
    const fetchMock = vi.spyOn(globalThis, 'fetch').mockResolvedValue({ status: 402, ok: false } as Response);
    const screen = await render(<Harness ownerId={1} />);
    expect(screen.container).toHaveTextContent('subscription_required');
    expect(screen.container).not.toHaveTextContent('EPUB 11');
    expect(cache.get).toHaveBeenCalledWith('1:7');
    expect(fetchMock).toHaveBeenCalledWith('/api/epub?bookId=7', expect.objectContaining({ method: 'HEAD', cache: 'no-store' }));
    await screen.unmount();
  });

  it('descarta a autorização atrasada da conta anterior e busca a chave da nova conta', async () => {
    vi.spyOn(navigator, 'onLine', 'get').mockReturnValue(true);
    localStorage.setItem('auth-user', JSON.stringify({ userId: 1 }));
    cache.get.mockImplementation(async (key: string) => new Uint8Array([key === '1:7' ? 11 : 22]).buffer);
    let finishA!: (response: Response) => void;
    const fetchMock = vi.spyOn(globalThis, 'fetch')
      .mockImplementationOnce(() => new Promise(resolve => { finishA = resolve; }))
      .mockResolvedValueOnce(new Response(null, { status: 200, headers: { 'X-Authenticated-User-Id': '2' } }));
    const screen = await render(<Harness ownerId={1} />);

    localStorage.setItem('auth-user', JSON.stringify({ userId: 2 }));
    await screen.rerender(<Harness ownerId={2} />);
    expect(screen.container).toHaveTextContent('EPUB 22');
    await act(async () => { finishA(new Response(null, { status: 200, headers: { 'X-Authenticated-User-Id': '1' } })); });
    expect(screen.container).toHaveTextContent('EPUB 22');
    expect(screen.container).not.toHaveTextContent('EPUB 11');
    expect(cache.get.mock.calls.map(([key]) => key)).toEqual(['1:7', '2:7']);
    expect(fetchMock).toHaveBeenCalledTimes(2);
    await screen.unmount();
  });

  it('mantém leitura offline apenas para a conta que salvou o livro', async () => {
    vi.spyOn(navigator, 'onLine', 'get').mockReturnValue(false);
    localStorage.setItem('auth-user', JSON.stringify({ userId: 1 }));
    cache.get.mockImplementation(async (key: string) => key === '1:7' ? new Uint8Array([11]).buffer : null);
    const fetchMock = vi.spyOn(globalThis, 'fetch').mockRejectedValue(new TypeError('offline'));
    const screen = await render(<Harness ownerId={1} />);
    expect(screen.container).toHaveTextContent('EPUB 11');
    expect(fetchMock).not.toHaveBeenCalled();

    localStorage.setItem('auth-user', JSON.stringify({ userId: 2 }));
    await screen.rerender(<Harness ownerId={2} />);
    expect(screen.container).not.toHaveTextContent('EPUB 11');
    expect(cache.get).toHaveBeenCalledWith('2:7');
    await screen.unmount();
  });

  it('usa cache da mesma conta quando a rede falha mesmo com onLine verdadeiro', async () => {
    vi.spyOn(navigator, 'onLine', 'get').mockReturnValue(true);
    localStorage.setItem('auth-user', JSON.stringify({ userId: 1 }));
    cache.get.mockResolvedValue(new Uint8Array([11]).buffer);
    const fetchMock = vi.spyOn(globalThis, 'fetch').mockRejectedValue(new TypeError('DNS indisponível'));
    const screen = await render(<Harness ownerId={1} />);
    expect(screen.container).toHaveTextContent('EPUB 11');
    expect(fetchMock).toHaveBeenCalledWith('/api/epub?bookId=7', expect.objectContaining({ method: 'HEAD', cache: 'no-store' }));
    await screen.unmount();
  });

  it('não aceita autorização feita com cookie de outra conta mesmo se a UI ainda indicar a primeira', async () => {
    vi.spyOn(navigator, 'onLine', 'get').mockReturnValue(true);
    localStorage.setItem('auth-user', JSON.stringify({ userId: 1 }));
    cache.get.mockResolvedValue(new Uint8Array([11]).buffer);
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(new Response(null, { status: 200, headers: { 'X-Authenticated-User-Id': '2' } }));
    const screen = await render(<Harness ownerId={1} />);
    expect(screen.container).not.toHaveTextContent('EPUB 11');
    expect(screen.container).toHaveTextContent('A sessão mudou durante a leitura');
    await screen.unmount();
  });
});
