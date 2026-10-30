'use client';

import { useEffect, useRef, useState } from 'react';
import { addUserBook, ApiError, getAuthorDisplay, type RecommendationItem } from '@/lib/api';
import { BookCover } from '@/components/BookCover';

export interface RecommendationCardProps {
  item: RecommendationItem;
  onRemoved: (bookId: number, reason: 'added' | 'already_added' | 'unavailable') => void;
  onErrorRevalidate: () => void;
}
export function RecommendationCard({ item, onRemoved, onErrorRevalidate }: Readonly<RecommendationCardProps>) {
  const [pending, setPending] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const busy = useRef(false);
  const alive = useRef(true);
  useEffect(() => { alive.current = true; return () => { alive.current = false; }; }, []);
  function handleFailure(error_: unknown) {
    if ((error_ instanceof ApiError && error_.status === 409) || (error_ instanceof Error && error_.message === 'already_added')) {
      onRemoved(item.book.id, 'already_added');
      return;
    }
    if (error_ instanceof ApiError && error_.status === 404 && error_.code === 'BOOK_NOT_FOUND') {
      onRemoved(item.book.id, 'unavailable');
      return;
    }
    if (alive.current) setError('Não foi possível adicionar o livro. Tente novamente.');
    if (error_ instanceof ApiError && error_.status === 404) onErrorRevalidate();
  }
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
    } catch (error_) {
      handleFailure(error_);
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
