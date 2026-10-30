import { act } from 'react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { click, render } from '@/test-utils/render';
import { ApiError, type RecommendationItem } from '@/lib/api';
import { RecommendationCard } from './RecommendationCard';

const api = vi.hoisted(() => ({ add: vi.fn() }));
vi.mock('@/lib/api', async original => ({ ...await original<typeof import('@/lib/api')>(), addUserBook: api.add }));
const item: RecommendationItem = { position: 8, book: { id: 42, title: 'Livro sugerido', authors: [{ id: 1, name: 'Autora' }], coverUrl: null }, reason: { type: 'AUTHOR', matchedValue: 'Autora', text: 'Autora presente na sua biblioteca na data da seleção.' } };
const removed = vi.fn();
const revalidate = vi.fn();
let screen: Awaited<ReturnType<typeof render>>;
beforeEach(() => { vi.clearAllMocks(); api.add.mockResolvedValue({ id: 1 }); });
afterEach(async () => { await screen?.unmount(); });
const mount = async () => { screen = await render(<RecommendationCard item={item} referenceDate="2026-10-30" onRemoved={removed} onErrorRevalidate={revalidate} />); };

describe('cartão e inclusão confirmada', () => {
  it('mostra dados, substituto da capa, evidência histórica e link sem hover', async () => {
    await mount();
    expect(screen.container).toHaveTextContent('Capa não disponível');
    expect(screen.container).toHaveTextContent('30/10/2026');
    expect(screen.container.querySelector('a')).toHaveAttribute('href', '/explorar/42');
    expect(screen.container.querySelector('button')).toHaveTextContent('Adicionar à biblioteca');
  });
  it('aguarda confirmação e impede cliques duplicados', async () => {
    let finish!: (value: unknown) => void;
    api.add.mockImplementation(() => new Promise(resolve => { finish = resolve; }));
    await mount();
    await click(screen.container.querySelector('button')!);
    await click(screen.container.querySelector('button')!);
    expect(api.add).toHaveBeenCalledTimes(1);
    expect(removed).not.toHaveBeenCalled();
    await act(async () => { finish({ id: 1 }); });
    expect(removed).toHaveBeenCalledWith(42, 'added');
  });
  it.each([
    [new ApiError('already_added', 409), 'already_added'],
    [new ApiError('ausente', 404, 'BOOK_NOT_FOUND'), 'unavailable'],
  ])('trata confirmação específica sem inclusão falsa', async (failure, reason) => {
    api.add.mockRejectedValue(failure);
    await mount();
    await click(screen.container.querySelector('button')!);
    expect(removed).toHaveBeenCalledWith(42, reason);
  });
  it.each([new ApiError('404 genérico', 404), new ApiError('servidor', 503), new ApiError('código incorreto', 503, 'BOOK_NOT_FOUND'), new TypeError('rede')])('mantém cartão e retry após erro não confirmado', async failure => {
    api.add.mockRejectedValue(failure);
    await mount();
    await click(screen.container.querySelector('button')!);
    expect(removed).not.toHaveBeenCalled();
    expect(screen.container.querySelector('[role="alert"]')).toHaveTextContent('Não foi possível adicionar');
    expect(screen.container.querySelector('button')).not.toBeDisabled();
    if (failure instanceof ApiError && failure.status === 404) expect(revalidate).toHaveBeenCalledOnce();
  });
});
