"use client";

import { useState, useEffect } from "react";
import { Search, Plus, Star } from "lucide-react";
import { getBooks, type BookApiResponse } from "@/lib/api";

const filters = ["Todos", "Lendo", "Concluído", "Para Ler", "Emprestado"];

export default function BibliotecaPage() {
  const [books, setBooks] = useState<BookApiResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [activeFilter, setActiveFilter] = useState(0);
  const [query, setQuery] = useState("");

  useEffect(() => {
    getBooks(0, 20)
      .then((page) => setBooks(page.content))
      .catch(console.error)
      .finally(() => setLoading(false));
  }, []);

  const filtered = books.filter((book) =>
    query.trim() === "" ||
    book.title.toLowerCase().includes(query.toLowerCase()) ||
    book.author.toLowerCase().includes(query.toLowerCase())
  );

  return (
    <div className="px-6 md:px-8 pt-8 md:pt-12 pb-8 flex flex-col gap-8 max-w-5xl relative">
      {/* Title + search */}
      <div className="flex flex-col gap-6">
        <div className="flex items-baseline gap-3">
          <h1 className="font-brand font-bold text-brown text-4xl tracking-tight">Sua Biblioteca</h1>
          {!loading && (
            <span className="text-slate/50 text-sm">{books.length} livros</span>
          )}
        </div>
        <div className="relative">
          <Search size={18} className="absolute left-4 top-1/2 -translate-y-1/2 text-slate/60" />
          <input
            type="text"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder="Pesquisar livros, autores..."
            className="w-full bg-cream-dark rounded-lg pl-12 pr-4 py-4 text-base text-slate placeholder:text-slate/40 outline-none"
          />
        </div>
      </div>

      {/* Filters */}
      <div className="flex gap-2 overflow-x-auto pb-1 -mx-6 px-6 md:mx-0 md:px-0 md:flex-wrap">
        {filters.map((f, i) => (
          <button
            key={f}
            onClick={() => setActiveFilter(i)}
            className={`shrink-0 px-5 py-2.5 rounded-xl text-sm font-medium transition-colors ${
              i === activeFilter ? "bg-brown text-cream" : "bg-cream-book text-slate hover:bg-cream-active"
            }`}
          >
            {f}
          </button>
        ))}
      </div>

      {/* Book grid */}
      {loading ? (
        <div className="grid grid-cols-2 md:grid-cols-4 gap-x-6 gap-y-10">
          {Array.from({ length: 8 }).map((_, i) => (
            <div key={i} className="flex flex-col gap-3">
              <div className="w-full h-[239px] bg-cream-book rounded animate-pulse" />
              <div className="h-4 bg-cream-book rounded animate-pulse w-3/4" />
              <div className="h-3 bg-cream-book rounded animate-pulse w-1/2" />
            </div>
          ))}
        </div>
      ) : filtered.length === 0 ? (
        <div className="flex flex-col items-center justify-center py-24 gap-4">
          <Star size={32} className="text-slate/20" />
          <p className="text-slate/40 text-sm">
            {query ? "Nenhum livro encontrado para essa busca." : "Sua biblioteca está vazia."}
          </p>
        </div>
      ) : (
        <div className="grid grid-cols-2 md:grid-cols-4 gap-x-6 gap-y-10">
          {filtered.map((book) => (
            <div key={book.id} className="flex flex-col">
              <div className="relative mb-4 overflow-hidden rounded">
                {book.coverUrl ? (
                  <img
                    src={book.coverUrl}
                    alt={book.title}
                    className="w-full h-[239px] object-cover"
                  />
                ) : (
                  <div className="w-full h-[239px] bg-cream-book flex items-center justify-center">
                    <span className="text-slate/30 text-xs uppercase tracking-widest">Sem capa</span>
                  </div>
                )}
              </div>
              <h3 className="font-brand font-bold text-brown text-lg leading-[22px]">{book.title}</h3>
              <p className="text-slate/70 text-xs mt-1">{book.author}</p>
            </div>
          ))}
        </div>
      )}

      {/* FAB */}
      <button className="fixed bottom-20 md:bottom-8 right-6 bg-brown text-cream size-14 rounded-xl flex items-center justify-center shadow-xl hover:bg-brown/90 transition-colors">
        <Plus size={14} />
      </button>
    </div>
  );
}
