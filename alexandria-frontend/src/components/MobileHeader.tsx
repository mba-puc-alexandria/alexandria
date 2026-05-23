"use client";

import { useState, useEffect, useRef } from "react";
import Link from "next/link";
import { Search, BookMarked, X, Loader2 } from "lucide-react";
import { searchBooks, getAuthorDisplay, type BookApiResponse } from "@/lib/api";

export default function MobileHeader() {
  const [searchOpen, setSearchOpen] = useState(false);
  const [query, setQuery] = useState("");
  const [results, setResults] = useState<BookApiResponse[]>([]);
  const [loading, setLoading] = useState(false);
  const inputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    if (searchOpen) inputRef.current?.focus();
  }, [searchOpen]);

  useEffect(() => {
    const trimmed = query.trim();
    if (!trimmed) {
      setResults([]);
      return;
    }
    setLoading(true);
    const timer = setTimeout(() => {
      searchBooks(trimmed, 0, 6)
        .then((data) => setResults(data.content))
        .catch(() => setResults([]))
        .finally(() => setLoading(false));
    }, 350);
    return () => clearTimeout(timer);
  }, [query]);

  function handleClose() {
    setSearchOpen(false);
    setQuery("");
    setResults([]);
  }

  function handleSelect() {
    handleClose();
  }

  return (
    <header className="sticky top-0 z-30 backdrop-blur-sm bg-cream/95 border-b border-cream-active md:hidden">
      <div className="flex items-center justify-between px-6 py-5">
        {searchOpen ? (
          <div className="flex items-center gap-3 flex-1">
            {loading ? (
              <Loader2 size={15} className="text-slate shrink-0 animate-spin" />
            ) : (
              <Search size={15} className="text-slate shrink-0" />
            )}
            <input
              ref={inputRef}
              type="text"
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              placeholder="Pesquisar livros..."
              className="flex-1 bg-transparent text-sm text-brown placeholder:text-slate/50 outline-none"
            />
            <button onClick={handleClose} className="text-slate shrink-0">
              <X size={18} />
            </button>
          </div>
        ) : (
          <>
            <Link href="/explorar" className="flex items-center gap-3">
              <BookMarked size={16} className="text-brown" />
              <div>
                <p className="font-serif font-bold text-brown text-xl leading-none">Alexandria</p>
                <p className="text-slate text-[8px] tracking-widest uppercase opacity-70 mt-0.5">
                  BIBLIOTECA DIGITAL
                </p>
              </div>
            </Link>

            <div className="flex items-center gap-4">
              <button className="text-slate" onClick={() => setSearchOpen(true)}>
                <Search size={15} />
              </button>
              <div className="size-8 rounded-xl bg-cream-border overflow-hidden border border-white/10">
                <div className="size-full bg-gradient-to-br from-slate to-brown opacity-60" />
              </div>
            </div>
          </>
        )}
      </div>

      {searchOpen && query.trim() && (
        <div className="border-t border-cream-border bg-cream">
          {results.length === 0 && !loading ? (
            <p className="text-slate/50 text-sm px-6 py-4">Nenhum resultado.</p>
          ) : (
            <ul>
              {results.map((book) => (
                <li key={book.id} className="border-b border-cream-border last:border-0">
                  <Link
                    href={`/explorar/${book.id}`}
                    onClick={handleSelect}
                    className="flex items-center gap-3 px-6 py-3 active:bg-cream-dark transition-colors"
                  >
                    {book.coverUrl ? (
                      <img src={book.coverUrl} alt={book.title} className="w-8 h-11 object-cover rounded shrink-0" />
                    ) : (
                      <div className="w-8 h-11 bg-cream-book rounded shrink-0" />
                    )}
                    <div className="flex flex-col min-w-0">
                      <span className="text-brown text-sm font-bold truncate">{book.title}</span>
                      <span className="text-slate text-xs truncate">{getAuthorDisplay(book)}</span>
                    </div>
                  </Link>
                </li>
              ))}
            </ul>
          )}
        </div>
      )}
    </header>
  );
}
