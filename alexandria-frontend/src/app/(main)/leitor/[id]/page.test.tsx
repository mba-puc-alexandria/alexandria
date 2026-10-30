import { act } from 'react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { render } from '@/test-utils/render';
import type { BookApiResponse, UserBookResponse } from '@/lib/api';
import type { Rendition } from 'epubjs';
import LeitorPage from './page';

const mocks = vi.hoisted(() => ({
  user: { userId: 1 } as { userId: number } | null,
  getBook: vi.fn(),
  getBooks: vi.fn(),
  updateBook: vi.fn(),
  rendition: null as null | ((rendition: Rendition) => void),
}));
vi.mock('@/contexts/AuthContext', () => ({ useAuth: () => ({ user: mocks.user }) }));
vi.mock('@/lib/api', async original => ({
  ...await original<typeof import('@/lib/api')>(),
  getBookById: mocks.getBook,
  getUserBooks: mocks.getBooks,
  updateUserBook: mocks.updateBook,
}));
vi.mock('@/hooks/useEpub', () => ({ useEpub: () => ({ epubData: 'book.epub', loading: false, error: null }) }));
vi.mock('next/navigation', () => ({ useRouter: () => ({ push: vi.fn(), back: vi.fn() }) }));
vi.mock('next/dynamic', () => ({ default: () => ({ getRendition }: { getRendition: (rendition: Rendition) => void }) => {
  mocks.rendition = getRendition;
  return <div>Leitor EPUB</div>;
} }));

const book: BookApiResponse = {
  id: 7, title: 'Livro compartilhado', authors: [], coverUrl: null, downloadUrl: 'book.epub',
  languages: null, subjects: null, source: 'GUTENDEX', gutendexId: null,
  downloadCount: null, publisherId: null,
};
const userBook = (id: number, progress: number): UserBookResponse => ({
  id, book, status: 'reading', progress, rating: null,
});

beforeEach(() => {
  mocks.user = { userId: 1 };
  mocks.getBook.mockReset().mockResolvedValue(book);
  mocks.getBooks.mockReset();
  mocks.updateBook.mockReset();
  mocks.rendition = null;
  localStorage.setItem('auth-user', JSON.stringify({ userId: 1 }));
  localStorage.setItem('auth-token', 'token-a');
});

describe('Leitor e troca de conta', () => {
  it('descarta progresso privado de uma busca atrasada após trocar a conta', async () => {
    let finishA!: (books: UserBookResponse[]) => void;
    mocks.getBooks.mockImplementationOnce(() => new Promise(resolve => { finishA = resolve; }));
    const params = Promise.resolve({ id: '7' });
    const screen = await render(<LeitorPage params={params} />);
    const previousSignal = mocks.getBooks.mock.calls[0][0] as AbortSignal;

    localStorage.setItem('auth-user', JSON.stringify({ userId: 2 }));
    localStorage.setItem('auth-token', 'token-b');
    mocks.user = { userId: 2 };
    mocks.getBooks.mockResolvedValueOnce([userBook(202, 15)]);
    await screen.rerender(<LeitorPage params={params} />);
    expect(previousSignal.aborted).toBe(true);
    expect(screen.container).toHaveTextContent('15%');

    await act(async () => { finishA([userBook(101, 92)]); });
    expect(screen.container).not.toHaveTextContent('92%');
    expect(mocks.updateBook).not.toHaveBeenCalledWith(101, expect.anything());
    await screen.unmount();
  });

  it.each(['abertura do EPUB', 'cálculo de posições'])('mostra erro quando falha a %s', async stage => {
    mocks.getBooks.mockResolvedValue([userBook(101, 10)]);
    const screen = await render(<LeitorPage params={Promise.resolve({ id: '7' })} />);
    expect(mocks.rendition).not.toBeNull();
    const error = new Error(`Falha na ${stage}`);
    const generate = vi.fn().mockImplementation(() => stage === 'cálculo de posições' ? Promise.reject(error) : Promise.resolve());
    const rendition = {
      book: { ready: stage === 'abertura do EPUB' ? Promise.reject(error) : Promise.resolve(), locations: { generate } },
      on: vi.fn(),
    } as unknown as Rendition;
    await act(async () => { mocks.rendition?.(rendition); });
    expect(screen.container).toHaveTextContent('Erro ao carregar o livro.');
    expect(screen.container).toHaveTextContent(error.message);
    await screen.unmount();
  });
});
