"use client";

import { useState, useEffect, useRef } from "react";
import { Search, Bell, Loader2 } from "lucide-react";
import Link from "next/link";
import { searchBooks, getAuthorDisplay, type BookApiResponse } from "@/lib/api";

export default function Header() {
  const [query, setQuery] = useState("");
  const [results, setResults] = useState<BookApiResponse[]>([]);
  const [loading, setLoading] = useState(false);
  const [open, setOpen] = useState(false);
  const wrapperRef = useRef<HTMLDivElement>(null);

  // Debounce da busca
  useEffect(() => {
    const trimmed = query.trim();
    if (!trimmed) {
      setResults([]);
      setOpen(false);
      return;
    }
    setLoading(true);
    const timer = setTimeout(() => {
      searchBooks(trimmed, 0, 6)
        .then((data) => {
          setResults(data.content);
          setOpen(true);
        })
        .catch(() => setResults([]))
        .finally(() => setLoading(false));
    }, 350);
    return () => clearTimeout(timer);
  }, [query]);

  // Fechar ao clicar fora
  useEffect(() => {
    function handleClickOutside(e: MouseEvent) {
      if (wrapperRef.current && !wrapperRef.current.contains(e.target as Node)) {
        setOpen(false);
      }
    }
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  function handleSelect() {
    setQuery("");
    setOpen(false);
    setResults([]);
  }

  return (
    <header className="flex items-center justify-between px-8 py-4 bg-cream border-b border-cream-border shrink-0 z-10">
      <div className="w-48" />

      <div className="flex items-center gap-4">
        <div className="relative" ref={wrapperRef}>
          {loading ? (
            <Loader2 size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-slate opacity-60 animate-spin" />
          ) : (
            <Search size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-slate opacity-60" />
          )}
          <input
            type="text"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            onKeyDown={(e) => e.key === "Escape" && setOpen(false)}
            placeholder="Pesquisar clássicos, ensaios..."
            className="bg-cream-dark text-sm text-slate placeholder:text-slate/60 rounded-xl pl-9 pr-4 py-2 w-64 outline-none border border-transparent focus:border-cream-border transition-colors"
          />

          {open && (
            <div className="absolute top-full mt-2 right-0 w-80 bg-cream border border-cream-border rounded-xl shadow-lg overflow-hidden z-50">
              {results.length === 0 ? (
                <p className="text-slate/50 text-sm px-4 py-3">Nenhum resultado.</p>
              ) : (
                <ul>
                  {results.map((book) => (
                    <li key={book.id}>
                      <Link
                        href={`/explorar/${book.id}`}
                        onClick={handleSelect}
                        className="flex items-center gap-3 px-4 py-3 hover:bg-cream-dark transition-colors"
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
        </div>

        <button className="p-2 rounded-xl text-slate hover:bg-cream-active transition-colors">
          <Bell size={18} />
        </button>

        <div className="size-8 rounded-xl bg-cream-border overflow-hidden shrink-0">
          <div className="size-full bg-gradient-to-br from-slate to-brown opacity-60 rounded-xl" />
        </div>
      </div>
    </header>
  );
}
