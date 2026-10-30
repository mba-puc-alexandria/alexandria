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
afterEach(async () => { if (screen?.container.isConnected) await screen.unmount(); });
const mount = async () => { screen = await render(<RecommendationCard item={item} onRemoved={removed} onErrorRevalidate={revalidate} />); };

describe('cartão e inclusão confirmada', () => {
  it('mantém título e autor fora da capa, ação no overlay e metadados históricos fora da apresentação', async () => {
    await mount();
    const cover = screen.container.querySelector('[data-testid="recommendation-cover"]')!;
    const title = screen.container.querySelector('h3')!;
    const author = [...screen.container.querySelectorAll('p')].find(element => element.textContent === 'Autora' && !cover.contains(element));
    expect(title).toHaveTextContent('Livro sugerido');
    expect(author).toBeDefined();
    expect(cover.contains(title)).toBe(false);
    expect(screen.container).not.toHaveTextContent('30/10/2026');
    expect(screen.container).not.toHaveTextContent(item.reason.text);
    expect(screen.container.querySelectorAll('a')).toHaveLength(1);
    expect(screen.container.querySelector('a')).toHaveAttribute('href', '/explorar/42');
    expect(screen.container.querySelector('a')).toHaveAttribute('aria-label', 'Ver livro: Livro sugerido');
    expect(screen.container.querySelector('button')).toHaveAttribute('aria-label', 'Adicionar à biblioteca');
    expect(cover.contains(screen.container.querySelector('button'))).toBe(true);
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

  it('confirma BOOK_NOT_FOUND do POST em voo mesmo após trocar o grupo visível', async () => {
    let reject!: (reason: unknown) => void;
    api.add.mockImplementation(() => new Promise((_, fail) => { reject = fail; }));
    await mount();
    await click(screen.container.querySelector('button')!);
    await screen.unmount();
    await act(async () => { reject(new ApiError('ausente', 404, 'BOOK_NOT_FOUND')); });
    expect(removed).toHaveBeenCalledExactlyOnceWith(42, 'unavailable');
    expect(revalidate).not.toHaveBeenCalled();
  });

  it('404 genérico do POST em voo revalida após desmontagem sem confirmar remoção', async () => {
    let reject!: (reason: unknown) => void;
    api.add.mockImplementation(() => new Promise((_, fail) => { reject = fail; }));
    await mount();
    await click(screen.container.querySelector('button')!);
    await screen.unmount();
    await act(async () => { reject(new ApiError('404 genérico', 404)); });
    expect(removed).not.toHaveBeenCalled();
    expect(revalidate).toHaveBeenCalledOnce();
  });
});
