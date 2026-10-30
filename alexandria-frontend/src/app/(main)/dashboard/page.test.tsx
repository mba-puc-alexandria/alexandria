import { act } from 'react';
import { describe, expect, it, vi } from 'vitest';
import { render } from '@/test-utils/render';
import type { UserBookResponse } from '@/lib/api';
import DashboardPage from './page';

const mocks = vi.hoisted(() => ({ user: { userId: 1, username: 'conta-1' } as { userId: number; username: string } | null, books: vi.fn() }));
vi.mock('@/contexts/AuthContext', () => ({ useAuth: () => ({ user: mocks.user }) }));
vi.mock('@/components/recommendations/RecommendationSection', () => ({ RecommendationSection: () => <section>Recomendações</section> }));
vi.mock('@/lib/api', async original => ({ ...await original<typeof import('@/lib/api')>(), getUserBooks: mocks.books }));
const book = (title: string): UserBookResponse => ({ id: 1, status: 'reading', progress: 20, rating: null, book: { id: 1, title, authors: [], coverUrl: null, downloadUrl: null, languages: null, subjects: null, source: 'GUTENDEX', gutendexId: null, downloadCount: null, publisherId: null } });

describe('Painel e separação entre contas', () => {
  it('aborta biblioteca anterior, ignora resposta atrasada e limpa no logout', async () => {
    let finish!: (books: UserBookResponse[]) => void;
    mocks.user = { userId: 1, username: 'conta-1' };
    mocks.books.mockImplementationOnce(() => new Promise(resolve => { finish = resolve; }));
    const screen = await render(<DashboardPage />);
    const previousSignal = mocks.books.mock.calls[0][0] as AbortSignal;
    mocks.user = { userId: 2, username: 'conta-2' };
    mocks.books.mockResolvedValueOnce([book('Leitura da conta dois')]);
    await screen.rerender(<DashboardPage />);
    expect(previousSignal.aborted).toBe(true);
    expect(screen.container).toHaveTextContent('Leitura da conta dois');
    await act(async () => { finish([book('Leitura privada da conta um')]); });
    expect(screen.container).not.toHaveTextContent('Leitura privada da conta um');
    mocks.user = null;
    await screen.rerender(<DashboardPage />);
    expect(screen.container).not.toHaveTextContent('Leitura da conta dois');
    await screen.unmount();
  });
});
