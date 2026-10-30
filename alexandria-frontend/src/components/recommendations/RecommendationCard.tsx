'use client';

import Link from 'next/link';
import { useEffect, useRef, useState } from 'react';
import { addUserBook, ApiError, getAuthorDisplay, type RecommendationItem } from '@/lib/api';

export interface RecommendationCardProps {
  item: RecommendationItem;
  referenceDate: string;
  onRemoved: (bookId: number, reason: 'added' | 'already_added' | 'unavailable') => void;
  onErrorRevalidate: () => void;
}
export function selectionDate(value: string): string {
  return value.split('-').reverse().join('/');
}
export function RecommendationCard({ item, referenceDate, onRemoved, onErrorRevalidate }: RecommendationCardProps) {
  const [pending, setPending] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [coverFailed, setCoverFailed] = useState(false);
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
      if (alive.current) onRemoved(item.book.id, 'added');
    } catch (failure) {
      if (!alive.current) return;
      if ((failure instanceof ApiError && failure.status === 409) || (failure instanceof Error && failure.message === 'already_added')) {
        onRemoved(item.book.id, 'already_added');
      } else if (failure instanceof ApiError && failure.status === 404 && failure.code === 'BOOK_NOT_FOUND') {
        onRemoved(item.book.id, 'unavailable');
      } else {
        setError('Não foi possível adicionar o livro. Tente novamente.');
        if (failure instanceof ApiError && failure.status === 404) onErrorRevalidate();
      }
    } finally {
      busy.current = false;
      if (alive.current) setPending(false);
    }
  }
  return (
    <article data-book-id={item.book.id} aria-labelledby={`recommendation-${item.book.id}`} className="rounded-xl bg-cream-dark p-4 md:p-6">
      <div className="flex items-start gap-4 md:gap-6">
        {item.book.coverUrl && !coverFailed ? (
          // Cover is decorative: title and author are already text beside it.
          // eslint-disable-next-line @next/next/no-img-element
          <img src={item.book.coverUrl} alt="" width={96} height={144} onError={() => setCoverFailed(true)} className="h-36 w-24 shrink-0 rounded object-cover" />
        ) : <div className="flex h-36 w-24 shrink-0 items-center justify-center rounded bg-cream-book text-center text-xs text-slate">Capa não disponível</div>}
        <div className="min-w-0 flex-1">
          <h3 id={`recommendation-${item.book.id}`} className="font-serif text-lg font-bold text-brown">{item.book.title}</h3>
          <p className="mt-1 text-sm text-slate">{getAuthorDisplay(item.book) || 'Autoria não informada'}</p>
          <p className="mt-3 text-sm leading-relaxed text-brown">{item.reason.text}</p>
        </div>
      </div>
      <p className="mt-4 text-xs text-slate">Selecionado com base na sua biblioteca em {selectionDate(referenceDate)}.</p>
      <div className="mt-4 flex flex-wrap gap-3">
        <Link href={`/explorar/${item.book.id}`} className="rounded border border-brown/30 px-4 py-2 text-sm font-bold text-brown focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-terra">Ver livro</Link>
        <button type="button" disabled={pending} onClick={() => void add()} className="rounded bg-terra px-4 py-2 text-sm font-bold text-white disabled:opacity-60 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-brown">
          {pending ? 'Adicionando…' : 'Adicionar à biblioteca'}
        </button>
      </div>
      {error && <p role="alert" className="mt-3 text-sm text-brown">{error}</p>}
    </article>
  );
}
