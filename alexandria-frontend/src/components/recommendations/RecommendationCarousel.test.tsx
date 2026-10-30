import { act } from 'react';
import { afterEach, describe, expect, it, vi } from 'vitest';
import { click, render } from '@/test-utils/render';
import type { RecommendationItem } from '@/lib/api';
import { RecommendationCarousel } from './RecommendationCarousel';

const api = vi.hoisted(() => ({ add: vi.fn() }));
vi.mock('@/lib/api', async original => ({ ...await original<typeof import('@/lib/api')>(), addUserBook: api.add }));

function items(count: number): RecommendationItem[] {
  return Array.from({ length: count }, (_, index) => ({ position: index + 1, book: { id: index + 1, title: `Livro ${index + 1}`, authors: [], coverUrl: null }, reason: { type: 'SUBJECT', matchedValue: 'ficção', text: 'Assunto presente na sua biblioteca nessa data.' } }));
}
const props = { onRemoved: vi.fn(), onErrorRevalidate: vi.fn() };
const control = (container: HTMLElement, name: string) => container.querySelector<HTMLButtonElement>(`button[aria-label="${name}"]`)!;
const originalWidth = window.innerWidth;
async function viewport(width: number) {
  Object.defineProperty(window, 'innerWidth', { configurable: true, value: width });
  await act(async () => { window.dispatchEvent(new Event('resize')); });
}
afterEach(async () => { await viewport(originalWidth); });
const layouts = [[390, 2], [640, 3], [1024, 4], [1280, 5]].flatMap(([width, perPage]) => [1, 7, 30].map(count => ({ width, perPage, count })));

describe('carrossel circular', () => {
  it.each(layouts)('exibe grupos de $perPage em $width px com $count livros e repete após a volta', async ({ width, perPage, count }) => {
    await viewport(width);
    const screen = await render(<RecommendationCarousel {...props} items={items(count)} />);
    const pages = Math.ceil(count / perPage);
    const status = screen.container.querySelector('[role="status"]')!;
    expect(status).toHaveClass('sr-only');
    for (let page = 0; page < pages; page++) {
      const expected = items(count).slice(page * perPage, (page + 1) * perPage).map(item => String(item.book.id));
      expect([...screen.container.querySelectorAll('article')].map(article => article.getAttribute('data-book-id'))).toEqual(expected);
      expect(status).toHaveTextContent(`${page * perPage + 1} de ${count}`);
      await click(control(screen.container, 'Próxima recomendação'));
    }
    expect(screen.container).toHaveTextContent(`1 de ${count}`);
    await click(control(screen.container, 'Recomendação anterior'));
    expect(status).toHaveTextContent(`${(pages - 1) * perPage + 1} de ${count}`);
    await screen.unmount();
  });

  it('mil voltas não consultam rede nem acumulam clones', async () => {
    await viewport(1280);
    const fetch = vi.spyOn(globalThis, 'fetch');
    const screen = await render(<RecommendationCarousel {...props} items={items(7)} />);
    for (let i = 0; i < 2_000; i++) await click(control(screen.container, 'Próxima recomendação'));
    expect(screen.container).toHaveTextContent('1 de 7');
    expect(screen.container.querySelectorAll('article')).toHaveLength(5);
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
    await viewport(390);
    const screen = await render(<RecommendationCarousel {...props} items={items(7)} />);
    await click(control(screen.container, 'Próxima recomendação'));
    expect(screen.container).toHaveTextContent('Livro 3');
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

  it('reorganiza o grupo e mantém foco utilizável ao redimensionar a tela', async () => {
    await viewport(1280);
    const screen = await render(<RecommendationCarousel {...props} items={items(7)} />);
    await click(control(screen.container, 'Próxima recomendação'));
    screen.container.querySelector<HTMLButtonElement>('article button')!.focus();
    await viewport(390);
    expect(screen.container.querySelectorAll('article')).toHaveLength(2);
    expect(screen.container).toHaveTextContent('3 de 7');
    expect(document.activeElement).not.toBe(document.body);
    await screen.unmount();
  });
});
