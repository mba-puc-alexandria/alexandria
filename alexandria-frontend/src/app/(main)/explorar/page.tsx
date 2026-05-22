"use client";

import { useState, useEffect } from "react";
import Link from "next/link";
import { Search, ArrowRight, ChevronLeft, ChevronRight, ChevronsLeft, ChevronsRight, Plus, Check, Loader2 } from "lucide-react";
import BookCard from "@/components/BookCard";
import { getBooks, getTopBooks, searchBooks, addUserBook, getAuthorDisplay, type BookApiResponse } from "@/lib/api";

export default function ExplorarPage() {
  const [books, setBooks] = useState<BookApiResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [query, setQuery] = useState("");
  const [searching, setSearching] = useState(false);
  const [addStates, setAddStates] = useState<Record<number, "idle" | "loading" | "added">>({});
  const [topBooks, setTopBooks] = useState<BookApiResponse[]>([]);
  const [topLoading, setTopLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);

  async function handleAddMobile(e: React.MouseEvent, bookId: number) {
    e.preventDefault();
    const current = addStates[bookId] ?? "idle";
    if (current !== "idle") return;
    setAddStates((s) => ({ ...s, [bookId]: "loading" }));
    try {
      await addUserBook(bookId);
      setAddStates((s) => ({ ...s, [bookId]: "added" }));
    } catch (err: unknown) {
      if (err instanceof Error && err.message === "already_added") {
        setAddStates((s) => ({ ...s, [bookId]: "added" }));
      } else {
        setAddStates((s) => ({ ...s, [bookId]: "idle" }));
      }
    }
  }

  // Top books — só na montagem
  useEffect(() => {
    getTopBooks(4)
      .then(setTopBooks)
      .catch(console.error)
      .finally(() => setTopLoading(false));
  }, []);

  // Browse com paginação — roda quando page muda e não está em modo busca
  useEffect(() => {
    if (searching) return;
    setLoading(true);
    getBooks(page, 10)
      .then((data) => {
        setBooks(data.content);
        setTotalPages(data.totalPages);
      })
      .catch(console.error)
      .finally(() => setLoading(false));
  }, [page, searching]);

  // Busca em tempo real com debounce de 400ms
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
      searchBooks(trimmed, 0, 10)
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

  function getAddIcon(state: "idle" | "loading" | "added") {
    if (state === "loading") return <Loader2 size={16} className="animate-spin" />;
    if (state === "added") return <Check size={16} className="text-green-700" />;
    return <Plus size={16} />;
  }

  function renderDesktopBooks() {
    if (loading) {
      return (
        <div className="grid grid-cols-5 gap-8">
          {Array.from({ length: 5 }).map((_, i) => (
            <div key={i} className="flex flex-col gap-4">
              <div className="w-full h-[250px] bg-cream-book rounded animate-pulse" />
              <div className="h-4 bg-cream-book rounded animate-pulse w-3/4" />
              <div className="h-3 bg-cream-book rounded animate-pulse w-1/2" />
            </div>
          ))}
        </div>
      );
    }
    if (books.length === 0) return <p className="text-slate/50 text-sm">Nenhum livro encontrado.</p>;
    return (
      <div className="grid grid-cols-5 gap-8">
        {books.map((book) => (
          <BookCard key={book.id} book={book} />
        ))}
      </div>
    );
  }

  return (
    <div className="flex flex-col">
      {/* ── MOBILE LAYOUT ── */}
      <div className="md:hidden px-6 pt-6 pb-8 flex flex-col gap-12">
        <section className="flex flex-col gap-6">
          <h1 className="font-brand font-bold text-brown text-4xl tracking-tight">Explorar</h1>
          <form onSubmit={handleSearch} className="relative">
            <input
              type="text"
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              placeholder="Títulos, autores ou assuntos..."
              className="w-full bg-cream-dark rounded-lg px-6 py-5 text-base text-slate placeholder:text-slate/50 outline-none"
            />
            <button type="submit">
              <Search size={18} className="absolute right-4 top-1/2 -translate-y-1/2 text-slate/60" />
            </button>
          </form>
        </section>

        <section className="flex flex-col gap-6">
          <h2 className="font-brand font-bold text-brown text-2xl leading-8">Mais Baixados</h2>
          {topLoading ? (
            <div className="grid grid-cols-2 gap-3">
              {[0, 1, 2, 3].map((i) => (
                <div key={i} className="h-40 bg-cream-book rounded-lg animate-pulse" />
              ))}
            </div>
          ) : topBooks.length > 0 && (
            <div className="grid grid-cols-2 gap-3">
              {topBooks.map((book, i) => (
                <Link key={book.id} href={`/explorar/${book.id}`} className="relative rounded-lg overflow-hidden h-40">
                  {book.coverUrl ? (
                    <img src={book.coverUrl} alt={book.title} className="absolute inset-0 w-full h-full object-cover" />
                  ) : (
                    <div className="absolute inset-0 bg-cream-book" />
                  )}
                  <div className="absolute inset-0 bg-gradient-to-t from-black/80 via-black/20 to-transparent" />
                  <div className="absolute bottom-0 left-0 right-0 p-3">
                    <p className="text-terra text-[10px] font-extrabold tracking-widest">#{i + 1}</p>
                    <h4 className="font-brand font-bold text-white text-sm leading-5 line-clamp-2">{book.title}</h4>
                  </div>
                </Link>
              ))}
            </div>
          )}
        </section>

        <section className="flex flex-col gap-6">
          <h2 className="font-brand font-bold text-brown text-2xl">
            {searching ? `Resultados para "${query}"` : "Recomendado"}
          </h2>
          {loading ? (
            <p className="text-slate/50 text-sm">Carregando...</p>
          ) : books.length === 0 ? (
            <p className="text-slate/50 text-sm">Nenhum livro encontrado.</p>
          ) : (
            <div className="flex flex-col gap-6">
              {books.map((book) => {
                const addState = addStates[book.id] ?? "idle";
                return (
                  <div key={book.id} className="bg-cream-dark rounded-lg p-4 flex gap-4 items-center hover:bg-cream-active transition-colors">
                    <Link href={`/explorar/${book.id}`} className="flex gap-4 items-center flex-1 min-w-0">
                      {book.coverUrl ? (
                        <img src={book.coverUrl} alt={book.title} className="w-20 h-28 object-cover rounded-sm shadow-sm shrink-0 border border-white/10" />
                      ) : (
                        <div className="w-20 h-28 bg-cream-book rounded-sm shrink-0 flex items-center justify-center">
                          <span className="text-slate/30 text-[10px]">Sem capa</span>
                        </div>
                      )}
                      <div className="flex flex-col flex-1 min-w-0">
                        <h4 className="font-brand font-bold text-brown text-lg leading-[22px]">{book.title}</h4>
                        <p className="text-slate text-sm">{getAuthorDisplay(book)}</p>
                        <div className="flex items-center gap-3 mt-3">
                          <span className="bg-brown text-cream text-[10px] font-bold tracking-widest uppercase px-4 py-1.5 rounded-xl">
                            VER DETALHES
                          </span>
                        </div>
                      </div>
                    </Link>
                    <button
                      type="button"
                      onClick={(e) => handleAddMobile(e, book.id)}
                      className="w-10 h-10 rounded-full border border-cream-border flex items-center justify-center text-brown hover:bg-cream-active transition-colors shrink-0 cursor-pointer"
                    >
                      {getAddIcon(addState)}
                    </button>
                  </div>
                );
              })}
            </div>
          )}
        </section>

        <button className="flex items-center justify-center gap-3 bg-cream-border border border-white/20 rounded-xl px-8 py-4 text-brown text-xs font-bold tracking-widest uppercase w-full">
          <Plus size={20} />
          SUGESTÃO PERSONALIZADA
        </button>
      </div>

      {/* ── DESKTOP LAYOUT ── */}
      <div className="hidden md:flex flex-col px-8 pt-12 pb-48 gap-16 max-w-5xl">
        <section className="grid grid-cols-12 gap-8 min-h-[320px] items-end">
          <div className="col-span-8 flex flex-col gap-4">
            <p className="text-terra text-xs font-extrabold tracking-[1.2px] uppercase">Descubra livros</p>
            <h1 className="font-serif font-bold text-brown text-[96px] leading-none tracking-tight">
              Encontre sua<br />próxima<br />leitura
            </h1>
          </div>
          <div className="col-span-4 pb-4">
            <p className="text-brown-soft text-lg font-light leading-relaxed">
              Milhares de livros disponíveis para leitura livre. Explore, salve e leia quando quiser.
            </p>
          </div>
        </section>

        <section className="flex flex-col gap-8">
          <h2 className="font-serif font-bold text-brown text-3xl">Mais Baixados</h2>
          {topLoading ? (
            <div className="grid grid-cols-4 gap-6">
              <div className="col-span-2 row-span-2 min-h-[392px] bg-cream-book rounded-lg animate-pulse" />
              <div className="col-span-2 h-48 bg-cream-book rounded-lg animate-pulse" />
              <div className="col-span-1 min-h-[200px] bg-cream-book rounded-lg animate-pulse" />
              <div className="col-span-1 min-h-[200px] bg-cream-book rounded-lg animate-pulse" />
            </div>
          ) : topBooks.length > 0 && (
            <div className="grid grid-cols-4 gap-6">
              {/* #1 — card grande */}
              <Link href={`/explorar/${topBooks[0].id}`} className="col-span-2 row-span-2 relative rounded-lg overflow-hidden min-h-[392px]">
                {topBooks[0].coverUrl ? (
                  <img src={topBooks[0].coverUrl} alt={topBooks[0].title} className="absolute inset-0 w-full h-full object-cover" />
                ) : (
                  <div className="absolute inset-0 bg-slate" />
                )}
                <div className="absolute inset-0 bg-gradient-to-t from-black/85 via-black/20 to-transparent" />
                <div className="absolute bottom-0 left-0 right-0 p-8 flex flex-col gap-2">
                  <p className="text-terra text-xs font-extrabold tracking-widest uppercase">#1 Mais Baixado</p>
                  <h3 className="font-serif font-bold text-white text-2xl leading-8">{topBooks[0].title}</h3>
                  <p className="text-white/70 text-sm">{getAuthorDisplay(topBooks[0])}</p>
                  {topBooks[0].downloadCount && (
                    <p className="text-white/50 text-xs">{topBooks[0].downloadCount.toLocaleString('pt-BR')} downloads</p>
                  )}
                  <span className="mt-2 bg-cream text-brown font-bold text-sm px-6 py-3 rounded-xl flex items-center gap-2 w-fit">
                    Ver livro <ArrowRight size={12} />
                  </span>
                </div>
              </Link>

              {/* #2 — card médio horizontal */}
              {topBooks[1] && (
                <Link href={`/explorar/${topBooks[1].id}`} className="col-span-2 bg-cream-border rounded-lg overflow-hidden h-48 flex">
                  {topBooks[1].coverUrl ? (
                    <img src={topBooks[1].coverUrl} alt={topBooks[1].title} className="h-full w-32 object-cover shrink-0" />
                  ) : (
                    <div className="h-full w-32 bg-cream-book shrink-0" />
                  )}
                  <div className="p-6 flex flex-col justify-center gap-1">
                    <p className="text-terra text-xs font-extrabold tracking-widest uppercase">#2</p>
                    <h4 className="font-serif font-bold text-brown text-xl leading-7 line-clamp-2">{topBooks[1].title}</h4>
                    <p className="text-brown-soft text-sm">{getAuthorDisplay(topBooks[1])}</p>
                    {topBooks[1].downloadCount && (
                      <p className="text-brown-soft text-xs mt-1">{topBooks[1].downloadCount.toLocaleString('pt-BR')} downloads</p>
                    )}
                  </div>
                </Link>
              )}

              {/* #3 — card pequeno */}
              {topBooks[2] && (
                <Link href={`/explorar/${topBooks[2].id}`} className="col-span-1 relative rounded-lg overflow-hidden min-h-[200px]">
                  {topBooks[2].coverUrl ? (
                    <img src={topBooks[2].coverUrl} alt={topBooks[2].title} className="absolute inset-0 w-full h-full object-cover" />
                  ) : (
                    <div className="absolute inset-0 bg-brown" />
                  )}
                  <div className="absolute inset-0 bg-gradient-to-t from-black/80 to-transparent" />
                  <div className="absolute bottom-0 left-0 right-0 p-5">
                    <p className="text-terra text-xs font-bold mb-1">#3</p>
                    <h4 className="font-serif font-bold text-white text-base leading-6 line-clamp-2">{topBooks[2].title}</h4>
                  </div>
                </Link>
              )}

              {/* #4 — card pequeno */}
              {topBooks[3] && (
                <Link href={`/explorar/${topBooks[3].id}`} className="col-span-1 relative rounded-lg overflow-hidden min-h-[200px]">
                  {topBooks[3].coverUrl ? (
                    <img src={topBooks[3].coverUrl} alt={topBooks[3].title} className="absolute inset-0 w-full h-full object-cover" />
                  ) : (
                    <div className="absolute inset-0 bg-cream-book" />
                  )}
                  <div className="absolute inset-0 bg-gradient-to-t from-black/80 to-transparent" />
                  <div className="absolute bottom-0 left-0 right-0 p-5">
                    <p className="text-terra text-xs font-bold mb-1">#4</p>
                    <h4 className="font-serif font-bold text-white text-base leading-6 line-clamp-2">{topBooks[3].title}</h4>
                  </div>
                </Link>
              )}
            </div>
          )}
        </section>

        <section className="bg-cream-dark rounded-lg p-12 relative overflow-hidden">
          <div className="absolute right-0 top-0 w-1/3 h-full opacity-10 pointer-events-none">
            <div className="absolute -top-20 -right-20 w-72 h-52 border-8 border-brown rounded-full" />
          </div>
          <form onSubmit={handleSearch} className="flex gap-4 items-center relative">
            <div className="flex-1 relative">
              <input
                type="text"
                value={query}
                onChange={(e) => setQuery(e.target.value)}
                placeholder="O que você quer ler hoje? Ex: romance, filosofia, Machado de Assis..."
                className="w-full bg-cream border border-cream-border border-b-2 border-b-brown-soft px-4 py-4 font-serif text-base text-brown placeholder:text-brown-soft/70 outline-none pr-12"
              />
              <Search size={33} className="absolute right-4 top-1/2 -translate-y-1/2 text-slate" />
            </div>
            <button type="submit" className="bg-brown text-cream font-bold text-sm px-8 py-4 rounded-xl shadow-[0_24px_40px_rgba(48,13,0,0.06)] hover:bg-brown/90 transition-colors shrink-0">
              Buscar
            </button>
          </form>
        </section>

        <section className="flex flex-col gap-8">
          <div className="flex items-center justify-between border-b border-cream-border pb-4">
            <h2 className="font-serif font-bold text-brown text-2xl">
              {searching ? `Resultados para "${query}"` : "Livros disponíveis"}
            </h2>
            <div className="flex items-center gap-1">
              <button
                onClick={() => setPage(0)}
                disabled={page === 0}
                className="p-2 rounded-xl hover:bg-cream-active transition-colors disabled:opacity-30 disabled:cursor-not-allowed"
              >
                <ChevronsLeft size={18} className="text-brown" />
              </button>
              <button
                onClick={() => setPage((p) => p - 1)}
                disabled={page === 0}
                className="p-2 rounded-xl hover:bg-cream-active transition-colors disabled:opacity-30 disabled:cursor-not-allowed"
              >
                <ChevronLeft size={18} className="text-brown" />
              </button>
              <span className="text-brown-soft text-xs px-2 tabular-nums">
                {page + 1} / {totalPages}
              </span>
              <button
                onClick={() => setPage((p) => p + 1)}
                disabled={page >= totalPages - 1}
                className="p-2 rounded-xl hover:bg-cream-active transition-colors disabled:opacity-30 disabled:cursor-not-allowed"
              >
                <ChevronRight size={18} className="text-brown" />
              </button>
              <button
                onClick={() => setPage(totalPages - 1)}
                disabled={page >= totalPages - 1}
                className="p-2 rounded-xl hover:bg-cream-active transition-colors disabled:opacity-30 disabled:cursor-not-allowed"
              >
                <ChevronsRight size={18} className="text-brown" />
              </button>
            </div>
          </div>
          {renderDesktopBooks()}
        </section>
      </div>
    </div>
  );
}

