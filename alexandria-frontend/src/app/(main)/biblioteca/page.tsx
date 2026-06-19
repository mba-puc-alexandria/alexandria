"use client";

import { useState, useEffect } from "react";
import Link from "next/link";
import { Search, Star, BookOpen, X } from "lucide-react";
import { getUserBooks, removeUserBook, getAuthorDisplay, type UserBookResponse } from "@/lib/api";

const filters = ["Todos", "Lendo", "Concluído", "Para Ler"];
const SKELETON_KEYS = ["sk-a", "sk-b", "sk-c", "sk-d", "sk-e", "sk-f", "sk-g", "sk-h"];

const STATUS_MAP: Record<string, number> = {
  reading: 1,
  done: 2,
  toread: 3,
};

const STATUS_BADGE: Record<string, { className: string }> = {
  reading: { className: 'bg-terra text-cream' },
  done:    { className: 'bg-brown text-cream' },
};

function getBadgeLabel(status: string, progress: number | null): string | null {
  if (status === 'reading') return progress === 100 ? 'Lido' : `${progress ?? 0}%`;
  if (status === 'done') return 'Concluído';
  return null;
}

function BookCover({ ub, onRemove }: { ub: UserBookResponse; onRemove: (ub: UserBookResponse) => void }) {
  const badgeStyle = STATUS_BADGE[ub.status];
  const badgeLabel = getBadgeLabel(ub.status, ub.progress);
  const badge = badgeStyle && badgeLabel ? { label: badgeLabel, className: badgeStyle.className } : null;

  const cover = ub.book.coverUrl ? (
    <img src={ub.book.coverUrl} alt={ub.book.title} className="w-full h-[239px] object-cover" />
  ) : (
    <div className="w-full h-[239px] bg-cream-book flex items-center justify-center">
      <span className="text-slate/30 text-xs uppercase tracking-widest">Sem capa</span>
    </div>
  );

  const indicators = (
    <>
      {badge && (
        <span className={`absolute top-2 left-2 text-xs font-bold px-2 py-0.5 rounded-full z-10 ${badge.className}`}>
          {badge.label}
        </span>
      )}
      {ub.status === 'reading' && ub.progress != null && (
        <div className="absolute bottom-0 left-0 right-0 h-1 bg-brown/20 z-10">
          <div className="h-full bg-terra" style={{ width: `${ub.progress}%` }} />
        </div>
      )}
    </>
  );

  const removeButton = (
    <button
      type="button"
      onClick={(e) => {
        e.preventDefault();
        e.stopPropagation();
        onRemove(ub);
      }}
      title="Remover da biblioteca"
      aria-label="Remover da biblioteca"
      className="absolute top-2 right-2 z-10 size-7 rounded-full bg-brown/70 text-cream flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity hover:bg-terra"
    >
      <X size={14} />
    </button>
  );

  if (ub.book.downloadUrl) {
    return (
      <Link href={`/leitor/${ub.book.id}`} className="relative mb-4 overflow-hidden rounded block group">
        {cover}
        <div className="absolute inset-0 bg-brown/0 group-hover:bg-brown/50 transition-colors flex items-center justify-center">
          <BookOpen size={32} className="text-cream opacity-0 group-hover:opacity-100 transition-opacity" />
        </div>
        {indicators}
        {removeButton}
      </Link>
    );
  }

  return (
    <div className="relative mb-4 overflow-hidden rounded group cursor-not-allowed">
      {cover}
      <div className="absolute inset-0 bg-brown/0 group-hover:bg-brown/40 transition-colors flex items-center justify-center">
        <span className="text-cream text-xs font-bold opacity-0 group-hover:opacity-100 transition-opacity uppercase tracking-widest">
          Indisponível
        </span>
      </div>
      {indicators}
      {removeButton}
    </div>
  );
}

export default function BibliotecaPage() {
  const [books, setBooks] = useState<UserBookResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [activeFilter, setActiveFilter] = useState(0);
  const [query, setQuery] = useState("");

  useEffect(() => {
    getUserBooks()
      .then(setBooks)
      .catch(console.error)
      .finally(() => setLoading(false));
  }, []);

  async function handleRemove(ub: UserBookResponse) {
    if (!window.confirm(`Remover "${ub.book.title}" da sua biblioteca?`)) return;
    try {
      await removeUserBook(ub.id);
      setBooks((prev) => prev.filter((b) => b.id !== ub.id));
    } catch (err) {
      console.error(err);
      window.alert("Não foi possível remover o livro. Tente novamente.");
    }
  }

  const filtered = books.filter((ub) => {
    const matchesQuery =
      query.trim() === "" ||
      ub.book.title.toLowerCase().includes(query.toLowerCase()) ||
      getAuthorDisplay(ub.book).toLowerCase().includes(query.toLowerCase());

    const isConcluido = ub.status === 'done' || (ub.status === 'reading' && ub.progress === 100);
    const matchesFilter =
      activeFilter === 0 ||
      (activeFilter === 2 ? isConcluido : STATUS_MAP[ub.status] === activeFilter);

    return matchesQuery && matchesFilter;
  });

  function renderContent() {
    if (loading) {
      return (
        <div className="grid grid-cols-2 md:grid-cols-4 gap-x-6 gap-y-10">
          {SKELETON_KEYS.map((key) => (
            <div key={key} className="flex flex-col gap-3">
              <div className="w-full h-[239px] bg-cream-book rounded animate-pulse" />
              <div className="h-4 bg-cream-book rounded animate-pulse w-3/4" />
              <div className="h-3 bg-cream-book rounded animate-pulse w-1/2" />
            </div>
          ))}
        </div>
      );
    }
    if (filtered.length === 0) {
      return (
        <div className="flex flex-col items-center justify-center py-24 gap-4">
          <Star size={32} className="text-slate/20" />
          <p className="text-slate/40 text-sm">
            {query ? "Nenhum livro encontrado para essa busca." : "Sua biblioteca está vazia."}
          </p>
        </div>
      );
    }
    return (
      <div className="grid grid-cols-2 md:grid-cols-4 gap-x-6 gap-y-10">
        {filtered.map((ub) => (
          <div key={ub.id} className="flex flex-col">
            <BookCover ub={ub} onRemove={handleRemove} />
            <h3 className="font-brand font-bold text-brown text-lg leading-[22px]">{ub.book.title}</h3>
            <p className="text-slate/70 text-xs mt-1">{getAuthorDisplay(ub.book)}</p>
          </div>
        ))}
      </div>
    );
  }

  return (
    <div className="px-6 md:px-8 pt-8 md:pt-12 pb-8 flex flex-col gap-8 max-w-5xl relative">
      <div className="flex flex-col gap-6">
        <div className="flex items-baseline gap-3">
          <h1 className="font-brand font-bold text-brown text-4xl tracking-tight">Sua Biblioteca</h1>
          {!loading && <span className="text-slate/50 text-sm">{books.length} livros</span>}
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

      {renderContent()}
    </div>
  );
}
