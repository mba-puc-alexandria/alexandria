'use client';

import { useEffect, useRef, useState, useSyncExternalStore, type ReactNode } from 'react';
import { ChevronLeft, ChevronRight } from 'lucide-react';
import type { RecommendationItem } from '@/lib/api';
import { RecommendationCard, type RecommendationCardProps } from './RecommendationCard';

interface Props extends Omit<RecommendationCardProps, 'item'> { items: RecommendationItem[]; heading?: ReactNode }
function subscribeViewport(notify: () => void) {
  window.addEventListener('resize', notify);
  return () => window.removeEventListener('resize', notify);
}
function columns() {
  return window.innerWidth >= 1280 ? 5 : window.innerWidth >= 1024 ? 4 : window.innerWidth >= 640 ? 3 : 2;
}
export function RecommendationCarousel({ items, heading, ...card }: Props) {
  const perPage = useSyncExternalStore(subscribeViewport, columns, () => 2);
  const [activePage, setActivePage] = useState(0);
  const container = useRef<HTMLDivElement>(null);
  const pointerStart = useRef<{ x: number; y: number } | null>(null);
  const suppressSwipeClick = useRef(false);
  const focusedBook = useRef<number | null>(null);
  const pages = Math.max(1, Math.ceil(items.length / perPage));
  const page = Math.min(activePage, pages - 1);
  const index = page * perPage;
  const visible = items.slice(index, index + perPage);
  const ids = visible.map(value => value.book.id).join(',');
  useEffect(() => { setActivePage(previous => Math.min(previous, pages - 1)); }, [pages]);
  useEffect(() => {
    if (focusedBook.current !== null && !container.current?.querySelector(`article[data-book-id="${focusedBook.current}"]`)) {
      focusedBook.current = null;
      const target = container.current?.querySelector<HTMLElement>('article a, button') || container.current;
      target?.focus();
    }
  }, [ids]);
  const move = (step: number) => { if (items.length) setActivePage(previous => (Math.min(previous, pages - 1) + step + pages) % pages); };
  return <div ref={container} tabIndex={-1} role="group" aria-roledescription="carrossel" aria-label="Sugestões de leitura"
    onFocusCapture={event => {
      const article = (event.target as HTMLElement).closest<HTMLElement>('article[data-book-id]');
      focusedBook.current = article ? Number(article.dataset.bookId) : null;
    }}
    onPointerDown={event => { pointerStart.current = { x: event.clientX, y: event.clientY }; suppressSwipeClick.current = false; }}
    onPointerUp={event => {
      const start = pointerStart.current;
      const dx = start ? event.clientX - start.x : 0;
      const dy = start ? event.clientY - start.y : 0;
      if (start && Math.abs(dx) >= 40 && Math.abs(dx) > Math.abs(dy)) {
        suppressSwipeClick.current = true;
        event.preventDefault();
        move(dx < 0 ? 1 : -1);
      }
      pointerStart.current = null;
    }} onPointerCancel={() => { pointerStart.current = null; }}
    onClickCapture={event => {
      // A completed swipe must not also activate the link/add button beneath it.
      // New pointerdown resets this flag; keyboard activation has detail=0.
      if (suppressSwipeClick.current && event.detail > 0) { event.preventDefault(); event.stopPropagation(); }
      suppressSwipeClick.current = false;
    }} style={{ touchAction: 'pan-y' }}>
    <div className="mb-4 flex items-center justify-between gap-3">
      {heading}
      {items.length > 0 && <div className="ml-auto flex shrink-0 items-center gap-1.5">
        <button type="button" aria-label="Recomendação anterior" title="Anterior" onClick={() => move(-1)} className="flex size-9 cursor-pointer items-center justify-center rounded-full border border-cream-border text-brown transition-colors hover:bg-cream-dark focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-terra"><ChevronLeft aria-hidden="true" size={19} /></button>
        <button type="button" aria-label="Próxima recomendação" title="Próximo" onClick={() => move(1)} className="flex size-9 cursor-pointer items-center justify-center rounded-full border border-cream-border text-brown transition-colors hover:bg-cream-dark focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-terra"><ChevronRight aria-hidden="true" size={19} /></button>
      </div>}
    </div>
    <div className="grid grid-cols-2 gap-x-3 gap-y-5 sm:grid-cols-3 sm:gap-x-4 lg:grid-cols-4 xl:grid-cols-5">
      {visible.map(item => <RecommendationCard key={item.book.id} item={item} {...card} />)}
    </div>
    {items.length > 0 && <div role="status" aria-live="polite" aria-atomic="true" className="sr-only">
      {index + 1} de {items.length}
    </div>}
  </div>;
}
