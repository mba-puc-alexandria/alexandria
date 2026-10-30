'use client';

import Link from 'next/link';
import { useState, type MouseEvent } from 'react';
import { Check, Loader2, Plus } from 'lucide-react';
import { getAuthorDisplay } from '@/lib/api';

interface BookCoverProps {
  book: { id: number; title: string; authors: string | { name: string }[]; coverUrl: string | null };
  state: 'idle' | 'loading' | 'added';
  onAdd: (event: MouseEvent<HTMLButtonElement>) => void;
  testId?: string;
}

/** Shared cover presentation used by Explore and personalized suggestions. */
export function BookCover({ book, state, onAdd, testId }: Readonly<BookCoverProps>) {
  const [coverFailed, setCoverFailed] = useState(false);
  const buttonTitle = { idle: 'Adicionar à biblioteca', loading: 'Adicionando…', added: 'Adicionado à biblioteca' }[state];
  const icon = { idle: <Plus aria-hidden="true" size={14} />, loading: <Loader2 aria-hidden="true" size={14} className="animate-spin" />, added: <Check aria-hidden="true" size={14} /> }[state];
  return (
    <div data-testid={testId} className="relative aspect-[2/3] w-full rounded overflow-hidden group shadow-sm bg-cream-book">
      <Link href={`/explorar/${book.id}`} aria-label={`Ver livro: ${book.title}`} className="absolute inset-0 cursor-pointer focus-visible:outline-2 focus-visible:-outline-offset-2 focus-visible:outline-terra">
        {book.coverUrl && !coverFailed ? (
          // The link and caption supply the book's accessible name.
          // eslint-disable-next-line @next/next/no-img-element
          <img src={book.coverUrl} alt="" onError={() => setCoverFailed(true)} className="absolute inset-0 w-full h-full object-cover" />
        ) : (
          <div className="absolute inset-0 flex items-end p-2" aria-hidden="true">
            <span className="text-brown-soft/40 text-[9px] leading-tight line-clamp-3">{book.title}</span>
          </div>
        )}
      </Link>
      <div className="absolute inset-0 flex flex-col items-center justify-end gap-2 p-2 pointer-events-none">
        <div data-testid="book-cover-overlay" aria-hidden="true" className="absolute inset-0 bg-brown/60 backdrop-blur-[2px] opacity-0 group-hover:opacity-100 group-focus-within:opacity-100 transition-opacity" />
        <p aria-hidden="true" className="relative text-cream text-[10px] font-bold text-center leading-tight line-clamp-2 w-full opacity-0 group-hover:opacity-100 group-focus-within:opacity-100 transition-opacity">{book.title}</p>
        <p aria-hidden="true" className="relative text-cream/70 text-[9px] text-center leading-tight line-clamp-1 w-full opacity-0 group-hover:opacity-100 group-focus-within:opacity-100 transition-opacity">{getAuthorDisplay(book)}</p>
        <button type="button" onClick={onAdd} aria-label={state === 'added' ? 'Adicionado à biblioteca' : 'Adicionar à biblioteca'} title={buttonTitle} aria-busy={state === 'loading'} disabled={state !== 'idle'} className="relative w-7 h-7 shrink-0 bg-cream text-brown rounded-full flex items-center justify-center hover:scale-110 transition-[opacity,transform] mt-1 cursor-pointer pointer-events-auto disabled:cursor-default focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-cream opacity-0 group-hover:opacity-100 group-focus-within:opacity-100 [@media(hover:none)]:opacity-100">
          {icon}
        </button>
      </div>
    </div>
  );
}
