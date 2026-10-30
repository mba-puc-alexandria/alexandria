'use client';

import { useEffect, useRef, useState } from 'react';
import { addUserBook, ApiError, getAuthorDisplay, type RecommendationItem } from '@/lib/api';
import { BookCover } from '@/components/BookCover';

export interface RecommendationCardProps {
  item: RecommendationItem;
  onRemoved: (bookId: number, reason: 'added' | 'already_added' | 'unavailable') => void;
  onErrorRevalidate: () => void;
}
export function RecommendationCard({ item, onRemoved, onErrorRevalidate }: RecommendationCardProps) {
  const [pending, setPending] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const busy = useRef(false);
  const alive = useRef(true);
  useEffect(() => { alive.current = true; return () => { alive.current = false; }; }, []);
  async function add() {
    if (busy.current) return;
    busy.current = true;
    setPending(true);
    setError(null);
    try {
      await addUserBook(item.book.id);
      // Owner-bound callbacks must survive a carousel group change; only local
      // state updates depend on this card remaining mounted.
      onRemoved(item.book.id, 'added');
    } catch (failure) {
      if ((failure instanceof ApiError && failure.status === 409) || (failure instanceof Error && failure.message === 'already_added')) {
        onRemoved(item.book.id, 'already_added');
      } else if (failure instanceof ApiError && failure.status === 404 && failure.code === 'BOOK_NOT_FOUND') {
        onRemoved(item.book.id, 'unavailable');
      } else {
        if (alive.current) setError('Não foi possível adicionar o livro. Tente novamente.');
        if (failure instanceof ApiError && failure.status === 404) onErrorRevalidate();
      }
    } finally {
      busy.current = false;
      if (alive.current) setPending(false);
    }
  }
  return (
    <article data-book-id={item.book.id} aria-labelledby={`recommendation-${item.book.id}`} className="flex min-w-0 flex-col gap-3">
      <BookCover book={item.book} state={pending ? 'loading' : 'idle'} testId="recommendation-cover" onAdd={event => { event.preventDefault(); event.stopPropagation(); void add(); }} />
      <div className="min-w-0">
        <h3 id={`recommendation-${item.book.id}`} className="line-clamp-2 min-h-10 font-serif text-sm font-bold leading-5 text-brown">{item.book.title}</h3>
        <p className="mt-1 line-clamp-1 text-xs leading-5 text-brown-soft">{getAuthorDisplay(item.book) || 'Autoria não informada'}</p>
      </div>
      {error && <p role="alert" className="text-xs leading-5 text-brown">{error}</p>}
    </article>
  );
}
