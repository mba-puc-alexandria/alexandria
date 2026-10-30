import { act } from 'react';
import { describe, expect, it, vi } from 'vitest';
import { click, render } from '@/test-utils/render';
import type { RecommendationItem } from '@/lib/api';
import { RecommendationCarousel } from './RecommendationCarousel';

const api = vi.hoisted(() => ({ add: vi.fn() }));
vi.mock('@/lib/api', async original => ({ ...await original<typeof import('@/lib/api')>(), addUserBook: api.add }));

function items(count: number): RecommendationItem[] {
  return Array.from({ length: count }, (_, index) => ({ position: index + 1, book: { id: index + 1, title: `Livro ${index + 1}`, authors: [], coverUrl: null }, reason: { type: 'SUBJECT', matchedValue: 'ficção', text: 'Assunto presente na sua biblioteca nessa data.' } }));
}
const props = { referenceDate: '2026-10-30', onRemoved: vi.fn(), onErrorRevalidate: vi.fn() };
const control = (container: HTMLElement, name: string) => container.querySelector<HTMLButtonElement>(`button[aria-label="${name}"]`)!;

describe('carrossel circular', () => {
  it.each([1, 7, 30])('exibe somente um de %i cartões e repete após a volta', async count => {
    const screen = await render(<RecommendationCarousel {...props} items={items(count)} />);
    expect(screen.container.querySelectorAll('article')).toHaveLength(1);
    expect(screen.container).toHaveTextContent(`1 de ${count}`);
    for (let i = 1; i < count; i++) { await click(control(screen.container, 'Próxima recomendação')); expect(screen.container).toHaveTextContent(`${i + 1} de ${count}`); }
    await click(control(screen.container, 'Próxima recomendação'));
    expect(screen.container).toHaveTextContent(`1 de ${count}`);
    await click(control(screen.container, 'Recomendação anterior'));
    expect(screen.container).toHaveTextContent(`${count} de ${count}`);
    await screen.unmount();
  });

  it('mil voltas não consultam rede nem acumulam clones', async () => {
    const fetch = vi.spyOn(globalThis, 'fetch');
    const screen = await render(<RecommendationCarousel {...props} items={items(7)} />);
    for (let i = 0; i < 7_000; i++) await click(control(screen.container, 'Próxima recomendação'));
    expect(screen.container).toHaveTextContent('1 de 7');
    expect(screen.container.querySelectorAll('article')).toHaveLength(1);
    expect(fetch).not.toHaveBeenCalled();
    fetch.mockRestore();
    await screen.unmount();
  }, 60_000);

  it('gesto avança sem acionar inclusão pelo click residual; teclado permanece disponível', async () => {
    api.add.mockReset().mockResolvedValue({ id: 1 });
    const screen = await render(<RecommendationCarousel {...props} items={items(1)} />);
    const add = screen.container.querySelector('article button')!;
    await act(async () => {
      add.dispatchEvent(new MouseEvent('pointerdown', { bubbles: true, clientX: 120, clientY: 10 }));
      add.dispatchEvent(new MouseEvent('pointerup', { bubbles: true, clientX: 20, clientY: 10 }));
      add.dispatchEvent(new MouseEvent('click', { bubbles: true, cancelable: true, detail: 1 }));
    });
    expect(api.add).not.toHaveBeenCalled();
    await click(add);
    expect(api.add).toHaveBeenCalledOnce();
    await screen.unmount();
  });

  it('reconcilia índice e foco após retirada do cartão e suporta gesto', async () => {
    const screen = await render(<RecommendationCarousel {...props} items={items(7)} />);
    await click(control(screen.container, 'Próxima recomendação'));
    expect(screen.container).toHaveTextContent('Livro 2');
    const article = screen.container.querySelector('article')!;
    const action = article.querySelector('button')!;
    action.focus();
    await screen.rerender(<RecommendationCarousel {...props} items={items(1)} />);
    expect(screen.container).toHaveTextContent('1 de 1');
    expect(document.activeElement).not.toBe(document.body);
    await screen.rerender(<RecommendationCarousel {...props} items={[]} />);
    expect(screen.container.querySelectorAll('article')).toHaveLength(0);
    await screen.unmount();
  });
});
