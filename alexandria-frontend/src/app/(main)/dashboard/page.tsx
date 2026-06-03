"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { BookOpen, Clock, Library } from "lucide-react";
import { getUserBooks, getAuthorDisplay, type UserBookResponse } from "@/lib/api";

const AVG_BOOK_MINUTES = 280; // ~70k palavras / 250 WPM

function formatMinutes(minutes: number): string {
  if (minutes < 1) return "< 1 min";
  if (minutes < 60) return `~${minutes} min`;
  const h = Math.floor(minutes / 60);
  const m = minutes % 60;
  return m > 0 ? `~${h}h ${m}min` : `~${h}h`;
}

export default function DashboardPage() {
  const [allBooks, setAllBooks] = useState<UserBookResponse[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    getUserBooks()
      .then(setAllBooks)
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  const readingBooks = allBooks.filter((b) => b.status === "reading" && (b.progress ?? 0) < 100);

  const totalAdded = allBooks.length;
  const totalRead = allBooks.filter(
    (b) => b.status === "done" || (b.status === "reading" && (b.progress ?? 0) === 100)
  ).length;
  const totalReading = readingBooks.length;
  const totalToRead = allBooks.filter((b) => b.status === "toread").length;

  const engagedBooks = totalRead + totalReading;
  const engagedPercent = totalAdded > 0 ? Math.round((engagedBooks / totalAdded) * 100) : 0;

  const estimatedMinutes = Math.round(
    allBooks
      .filter((b) => b.status === "reading" || b.status === "done")
      .reduce((acc, b) => acc + ((b.progress ?? 0) / 100) * AVG_BOOK_MINUTES, 0)
  );

  function renderReadingBooks() {
    if (loading) return <p className="text-slate text-sm">Carregando...</p>;
    if (readingBooks.length === 0) return <p className="text-slate text-sm">Nenhum livro em leitura no momento.</p>;
    return readingBooks.map((ub) => {
      const progress = ub.progress ?? 0;
      return (
        <div key={ub.id} className="bg-cream-dark rounded-lg p-4 flex gap-4">
          {ub.book.coverUrl ? (
            <img
              src={ub.book.coverUrl}
              alt={ub.book.title}
              className="w-24 h-36 object-cover rounded-sm shadow-sm shrink-0"
            />
          ) : (
            <div className="w-24 h-36 rounded-sm shadow-sm shrink-0 bg-cream-book flex items-center justify-center">
              <span className="text-slate/30 text-[9px] uppercase tracking-widest text-center px-1">Sem capa</span>
            </div>
          )}
          <div className="flex flex-col justify-between flex-1">
            <div className="flex flex-col gap-1">
              <h3 className="font-serif font-bold text-brown text-lg leading-7">{ub.book.title}</h3>
              <p className="text-slate text-xs">{getAuthorDisplay(ub.book)}</p>
            </div>
            <div className="flex flex-col gap-2">
              <div className="flex justify-between text-[9px] font-bold text-slate uppercase tracking-tight">
                <span>{progress}% concluído</span>
              </div>
              <div className="bg-cream-border h-1 rounded-full overflow-hidden">
                <div className="bg-terra h-full transition-all duration-500" style={{ width: `${progress}%` }} />
              </div>
              <Link
                href={`/leitor/${ub.book.id}`}
                className="flex items-center gap-1.5 text-brown text-[10px] font-bold mt-2 hover:text-terra transition-colors w-fit"
              >
                <BookOpen size={10} />
                Retomar Leitura
              </Link>
            </div>
          </div>
        </div>
      );
    });
  }

  return (
    <div className="px-6 md:px-8 pt-8 md:pt-12 pb-8 flex flex-col gap-8 md:gap-12 max-w-5xl">

      {/* Stats */}
      {!loading && totalAdded > 0 && (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">

          {/* Biblioteca */}
          <div className="bg-cream-dark rounded-xl p-5 flex flex-col gap-4">
            <div className="flex items-center gap-2">
              <Library size={14} className="text-terra" />
              <span className="text-brown text-xs font-bold uppercase tracking-widest">Sua Biblioteca</span>
            </div>
            <div className="flex items-end gap-6">
              <div className="flex flex-col">
                <span className="font-serif font-bold text-brown text-3xl">{totalAdded}</span>
                <span className="text-slate text-[10px] uppercase tracking-wide">adicionados</span>
              </div>
              <div className="flex flex-col">
                <span className="font-serif font-bold text-terra text-3xl">{totalRead}</span>
                <span className="text-slate text-[10px] uppercase tracking-wide">lidos</span>
              </div>
              <div className="flex flex-col">
                <span className="font-serif font-bold text-brown/60 text-3xl">{totalReading}</span>
                <span className="text-slate text-[10px] uppercase tracking-wide">lendo</span>
              </div>
              {totalToRead > 0 && (
                <div className="flex flex-col">
                  <span className="font-serif font-bold text-brown/40 text-3xl">{totalToRead}</span>
                  <span className="text-slate text-[10px] uppercase tracking-wide">para ler</span>
                </div>
              )}
            </div>
            <div className="flex flex-col gap-1.5">
              <div className="flex justify-between text-[9px] font-bold text-slate uppercase tracking-tight">
                <span>{engagedBooks} lidos ou em leitura</span>
                <span>{engagedPercent}%</span>
              </div>
              <div className="bg-cream-border h-1.5 rounded-full overflow-hidden">
                <div
                  className="bg-terra h-full rounded-full transition-all duration-700"
                  style={{ width: `${engagedPercent}%` }}
                />
              </div>
            </div>
          </div>

          {/* Tempo estimado */}
          <div className="bg-cream-dark rounded-xl p-5 flex flex-col gap-4">
            <div className="flex items-center gap-2">
              <Clock size={14} className="text-terra" />
              <span className="text-brown text-xs font-bold uppercase tracking-widest">Tempo de Leitura</span>
            </div>
            <div className="flex flex-col gap-0.5">
              <span className="font-serif font-bold text-brown text-3xl">
                {formatMinutes(estimatedMinutes)}
              </span>
              <span className="text-slate text-xs">estimado lido até agora</span>
            </div>
            <p className="text-slate/60 text-[10px] leading-relaxed">
              Estimativa baseada no progresso de cada livro a ~250 palavras por minuto.
            </p>
          </div>

        </div>
      )}

      {/* Lendo Agora */}
      <section className="flex flex-col gap-6">
        <div className="flex items-end justify-between">
          <div>
            <h2 className="font-serif font-bold text-brown text-xl">Lendo Agora</h2>
            <p className="text-slate text-xs mt-0.5">Suas buscas intelectuais em andamento</p>
          </div>
          <Link
            href="/biblioteca"
            className="text-terra text-[10px] font-bold tracking-widest uppercase"
          >
            VER TUDO
          </Link>
        </div>

        <div className="flex flex-col gap-4">
          {renderReadingBooks()}
        </div>
      </section>

    </div>
  );
}
