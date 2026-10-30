'use client';

import { useEffect, useRef, useState } from 'react';
import type { RecommendationItem } from '@/lib/api';
import { RecommendationCard, type RecommendationCardProps } from './RecommendationCard';

interface Props extends Omit<RecommendationCardProps, 'item'> { items: RecommendationItem[] }
export function RecommendationCarousel({ items, ...card }: Props) {
  const [activeIndex, setActiveIndex] = useState(0);
  const container = useRef<HTMLDivElement>(null);
  const pointerStart = useRef<{ x: number; y: number } | null>(null);
  const suppressSwipeClick = useRef(false);
  const focusedBook = useRef<number | null>(null);
  const index = Math.min(activeIndex, Math.max(0, items.length - 1));
  const item = items[index];
  const ids = items.map(value => value.book.id).join(',');
  useEffect(() => {
    if (focusedBook.current !== null && !items.some(value => value.book.id === focusedBook.current)) {
      focusedBook.current = null;
      const target = container.current?.querySelector<HTMLElement>('article button, button') || container.current;
      target?.focus();
    }
  }, [ids, items]);
  const move = (step: number) => { if (items.length) setActiveIndex((index + step + items.length) % items.length); };
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
    {item && <RecommendationCard key={item.book.id} item={item} {...card} />}
    {items.length > 0 && <div className="mt-4 flex items-center justify-between gap-3">
      <button type="button" aria-label="Recomendação anterior" onClick={() => move(-1)} className="rounded border border-brown/30 px-4 py-2 text-sm text-brown focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-terra">Anterior</button>
      <span aria-live="polite" aria-atomic="true" className="text-sm text-slate">{index + 1} de {items.length}</span>
      <button type="button" aria-label="Próxima recomendação" onClick={() => move(1)} className="rounded border border-brown/30 px-4 py-2 text-sm text-brown focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-terra">Próximo</button>
    </div>}
  </div>;
}
