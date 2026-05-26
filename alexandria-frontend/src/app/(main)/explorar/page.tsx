"use client";

import { useState, useEffect } from "react";
import Link from "next/link";
import { Search, Plus, Check, Loader2, BookMarked } from "lucide-react";
import { getBooks, searchBooks, addUserBook, getAuthorDisplay, type BookApiResponse } from "@/lib/api";
import { useAuth } from "@/contexts/AuthContext";
import { useAuthModal } from "@/contexts/AuthModalContext";

export default function ExplorarPage() {
  const [books, setBooks] = useState<BookApiResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [loadingMore, setLoadingMore] = useState(false);
  const [query, setQuery] = useState("");
  const [searching, setSearching] = useState(false);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);

  useEffect(() => {
    if (searching) return;
    if (page === 0) {
      setLoading(true);
    } else {
      setLoadingMore(true);
    }
    getBooks(page, 30)
      .then((data) => {
        setBooks((prev) => page === 0 ? data.content : [...prev, ...data.content]);
        setTotalPages(data.totalPages);
      })
      .catch(console.error)
      .finally(() => {
        setLoading(false);
        setLoadingMore(false);
      });
  }, [page, searching]);

  useEffect(() => {
    const trimmed = query.trim();
    if (!trimmed) {
      setSearching(false);
      setPage(0);
      return;
    }
    const timer = setTimeout(() => {
      setSearching(true);
      setLoading(true);
      setPage(0);
      searchBooks(trimmed, 0, 30)
        .then((data) => {
          setBooks(data.content);
          setTotalPages(data.totalPages);
        })
        .catch(console.error)
        .finally(() => setLoading(false));
    }, 400);
    return () => clearTimeout(timer);
  }, [query]);

  function handleSearch(e: React.FormEvent) {
    e.preventDefault();
  }

  return (
    <div className="flex flex-col items-center px-4 pt-10 pb-16 gap-6 min-h-screen">
      {/* Logo + frase */}
      <div className="flex flex-col items-center gap-2">
        <div className="flex items-center gap-3">
          <BookMarked size={36} className="text-brown" />
          <h1 className="font-brand font-bold text-brown text-5xl tracking-tight select-none">
            alexandria
          </h1>
        </div>
        <p className="font-serif text-brown-soft text-base tracking-wide">
          Encontre sua próxima leitura
        </p>
      </div>

      {/* Search bar — mesma largura da grade */}
      <form onSubmit={handleSearch} className="w-full max-w-5xl relative">
        <Search
          size={18}
          className="absolute left-4 top-1/2 -translate-y-1/2 text-brown-soft pointer-events-none"
        />
        <input
          type="text"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          placeholder="Pesquise por título, autor ou assunto..."
          className="w-full bg-cream-dark border border-cream-border rounded-xl pl-11 pr-4 py-3 text-sm text-brown placeholder:text-brown-soft/60 outline-none focus:border-brown-soft transition-colors"
        />
      </form>

      {/* Grade de livros */}
      <section className="w-full max-w-5xl">
        {searching && (
          <p className="text-brown-soft text-xs mb-4">
            Resultados para <span className="font-semibold text-brown">"{query}"</span>
          </p>
        )}

        {loading && books.length === 0 ? (
          <div className="grid grid-cols-3 sm:grid-cols-4 md:grid-cols-5 lg:grid-cols-6 gap-2">
            {Array.from({ length: 24 }).map((_, i) => (
              <div key={i} className="aspect-[2/3] bg-cream-book rounded animate-pulse" />
            ))}
          </div>
        ) : books.length === 0 ? (
          <p className="text-brown-soft/60 text-sm text-center py-16">Nenhum livro encontrado.</p>
        ) : (
          <div className="grid grid-cols-3 sm:grid-cols-4 md:grid-cols-5 lg:grid-cols-6 gap-2">
            {books.map((book) => (
              <CoverCard key={book.id} book={book} />
            ))}
            {loading && Array.from({ length: 6 }).map((_, i) => (
              <div key={`sk-${i}`} className="aspect-[2/3] bg-cream-book rounded animate-pulse" />
            ))}
          </div>
        )}

        {/* Carregar mais */}
        {!searching && page < totalPages - 1 && (
          <div className="flex justify-center mt-8">
            <button
              onClick={() => {
                setLoadingMore(true);
                setTimeout(() => setPage((p) => p + 1), 2000);
              }}
              disabled={loadingMore}
              className="min-w-[140px] bg-cream-dark border border-cream-border text-brown text-sm font-semibold px-8 py-3 rounded-xl hover:bg-cream-active transition-colors disabled:cursor-not-allowed flex items-center justify-center gap-2"
            >
              {loadingMore ? (
                <span className="flex items-center gap-0.5">
                  Carregando
                  <span className="dot-1 ml-0.5">.</span>
                  <span className="dot-2">.</span>
                  <span className="dot-3">.</span>
                </span>
              ) : (
                "Carregar mais"
              )}
            </button>
          </div>
        )}
      </section>
    </div>
  );
}

function CoverCard({ book }: { book: BookApiResponse }) {
  const [state, setState] = useState<"idle" | "loading" | "added">("idle");
  const { user } = useAuth();
  const { openLoginModal } = useAuthModal();

  async function handleAdd(e: React.MouseEvent) {
    e.preventDefault();
    e.stopPropagation();
    if (!user) {
      openLoginModal();
      return;
    }
    if (state !== "idle") return;
    setState("loading");
    try {
      await addUserBook(book.id);
      setState("added");
    } catch (err: unknown) {
      if (err instanceof Error && err.message === "already_added") {
        setState("added");
      } else {
        setState("idle");
      }
    }
  }

  function icon() {
    if (state === "loading") return <Loader2 size={14} className="animate-spin" />;
    if (state === "added") return <Check size={14} />;
    return <Plus size={14} />;
  }

  return (
    <Link
      href={`/explorar/${book.id}`}
      className="relative aspect-[2/3] rounded overflow-hidden group shadow-sm bg-cream-book"
    >
      {book.coverUrl ? (
        <img
          src={book.coverUrl}
          alt={book.title}
          className="absolute inset-0 w-full h-full object-cover"
        />
      ) : (
        <div className="absolute inset-0 flex items-end p-2">
          <span className="text-brown-soft/40 text-[9px] leading-tight line-clamp-3">{book.title}</span>
        </div>
      )}

      {/* Hover overlay */}
      <div className="absolute inset-0 bg-brown/60 backdrop-blur-[2px] opacity-0 group-hover:opacity-100 transition-opacity flex flex-col items-center justify-end gap-2 p-2 pointer-events-none group-hover:pointer-events-auto">
        <p className="text-cream text-[10px] font-bold text-center leading-tight line-clamp-2 w-full">
          {book.title}
        </p>
        <p className="text-cream/70 text-[9px] text-center leading-tight line-clamp-1 w-full">
          {getAuthorDisplay(book)}
        </p>
        <button
          type="button"
          onClick={handleAdd}
          className="w-7 h-7 bg-cream text-brown rounded-full flex items-center justify-center hover:scale-110 transition-transform mt-1"
        >
          {icon()}
        </button>
      </div>
    </Link>
  );
}
