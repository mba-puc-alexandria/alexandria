import { act } from 'react';
import { describe, expect, it, vi } from 'vitest';
import { render } from '@/test-utils/render';
import type { UserBookResponse } from '@/lib/api';
import BibliotecaPage from './page';

const mocks = vi.hoisted(() => ({
  user: { userId: 1, username: 'conta-1' } as { userId: number; username: string } | null,
  books: vi.fn(),
}));
vi.mock('@/contexts/AuthContext', () => ({ useAuth: () => ({ user: mocks.user }) }));
vi.mock('@/lib/api', async original => ({ ...await original<typeof import('@/lib/api')>(), getUserBooks: mocks.books }));

const entry = (id: number, title: string): UserBookResponse => ({
  id, status: 'toread', progress: null, rating: null,
  book: { id, title, authors: [], coverUrl: null, downloadUrl: null, languages: null, subjects: null, source: 'GUTENDEX', gutendexId: null, downloadCount: null, publisherId: null },
});

describe('Biblioteca e separação entre contas', () => {
  it('oculta livros anteriores e descarta resposta atrasada ao trocar de usuário', async () => {
    mocks.books.mockReset();
    mocks.user = { userId: 1, username: 'conta-1' };
    mocks.books.mockResolvedValueOnce([entry(1, 'Livro privado da conta um')]);
    const screen = await render(<BibliotecaPage />);
    expect(screen.container).toHaveTextContent('Livro privado da conta um');

    let finish!: (books: UserBookResponse[]) => void;
    mocks.books.mockImplementationOnce(() => new Promise(resolve => { finish = resolve; }));
    mocks.user = { userId: 2, username: 'conta-2' };
    await screen.rerender(<BibliotecaPage />);
    expect(screen.container).not.toHaveTextContent('Livro privado da conta um');
    const previousSignal = mocks.books.mock.calls[1][0] as AbortSignal;

    mocks.books.mockResolvedValueOnce([entry(3, 'Livro da conta três')]);
    mocks.user = { userId: 3, username: 'conta-3' };
    await screen.rerender(<BibliotecaPage />);
    expect(previousSignal.aborted).toBe(true);
    await act(async () => { finish([entry(2, 'Livro privado da conta dois')]); });
    expect(screen.container).toHaveTextContent('Livro da conta três');
    expect(screen.container).not.toHaveTextContent('Livro privado da conta dois');
    await screen.unmount();
  });
});
