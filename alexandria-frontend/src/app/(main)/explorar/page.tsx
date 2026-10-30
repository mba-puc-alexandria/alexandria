"use client";

import { useState, useEffect, useRef } from "react";
import { Search, BookMarked } from "lucide-react";
import { BookCover } from "@/components/BookCover";
import { getBooks, searchBooks, addUserBook, type BookApiResponse } from "@/lib/api";
import { useAuth } from "@/contexts/AuthContext";
import { useAuthModal } from "@/contexts/AuthModalContext";

const LANGUAGES = [
  { code: "pt", label: "Português" },
  { code: "en", label: "Inglês" },
  { code: "es", label: "Espanhol" },
  { code: "fr", label: "Francês" },
  { code: "de", label: "Alemão" },
  { code: "it", label: "Italiano" },
];

function handleSearch(e: React.FormEvent) {
  e.preventDefault();
}

export default function ExplorarPage() {
  const [books, setBooks] = useState<BookApiResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [loadingMore, setLoadingMore] = useState(false);
  const [query, setQuery] = useState("");
  const [searching, setSearching] = useState(false);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [language, setLanguage] = useState<string | undefined>(undefined);
  const languageRef = useRef(language);

  // Reinicia lista quando idioma ou modo de busca muda
  useEffect(() => {
    if (searching) return;
    languageRef.current = language;
    setBooks([]);
    setPage(0);
    setLoading(true);
    getBooks(0, 30, language)
      .then((data) => {
        setBooks(data.content);
        setTotalPages(data.totalPages);
      })
      .catch(console.error)
      .finally(() => setLoading(false));
  }, [language, searching]);

  // Carrega mais páginas (não repete página 0)
  useEffect(() => {
    if (searching || page === 0) return;
    setLoadingMore(true);
    getBooks(page, 30, languageRef.current)
      .then((data) => {
        setBooks((prev) => [...prev, ...data.content]);
        setTotalPages(data.totalPages);
      })
      .catch(console.error)
      .finally(() => setLoadingMore(false));
  }, [page, searching]);

  function handleLanguageSelect(code: string) {
    setLanguage((prev) => (prev === code ? undefined : code));
  }

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

  return (
    <div className="flex flex-col items-center px-4 pt-4 md:pt-10 pb-16 gap-6 min-h-screen">
      {/* Logo + frase — oculto no mobile pois o MobileHeader já tem o branding */}
      <div className="flex flex-col items-center gap-2">
        <div className="flex items-center gap-3">
          <BookMarked size={36} className="text-brown" />
          <h1 className="font-brand font-bold text-brown text-5xl tracking-tight select-none">
            Alexandria
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

      {/* Filtros de idioma */}
      {!searching && (
        <div className="w-full max-w-5xl flex flex-wrap items-center gap-2">
          {LANGUAGES.map((lang) => (
            <button type="button"
              key={lang.code}
              onClick={() => handleLanguageSelect(lang.code)}
              className={`px-3 py-1.5 rounded-lg text-xs font-semibold border transition-colors ${
                language === lang.code
                  ? "bg-brown text-cream border-brown"
                  : "bg-cream-dark text-brown-soft border-cream-border hover:bg-cream-active"
              }`}
            >
              {lang.label}
            </button>
          ))}
          {language && (
            <button type="button"
              onClick={() => setLanguage(undefined)}
              className="px-3 py-1.5 rounded-lg text-xs font-semibold text-terra underline"
            >
              Limpar filtro
            </button>
          )}
        </div>
      )}

      {/* Grade de livros */}
      <section className="w-full max-w-5xl">
        {searching && (
          <p className="text-brown-soft text-xs mb-4">
            Resultados para <span className="font-semibold text-brown">“{query}”</span>
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
            <button type="button"
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

function CoverCard({ book }: Readonly<{ book: BookApiResponse }>) {
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

  return <BookCover book={book} state={state} onAdd={handleAdd} />;
}
