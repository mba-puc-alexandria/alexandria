"use client";

import { use, useEffect, useState } from "react";
import Link from "next/link";
import { ArrowLeft, BookOpen, Plus } from "lucide-react";
import { getBookById, addUserBook, getAuthorDisplay, type BookApiResponse } from "@/lib/api";
import { useAuth } from "@/contexts/AuthContext";
import { useAuthModal } from "@/contexts/AuthModalContext";

export default function BookDetailPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = use(params);
  const [book, setBook] = useState<BookApiResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);
  const [adding, setAdding] = useState(false);
  const [added, setAdded] = useState(false);
  const { user } = useAuth();
  const { openLoginModal } = useAuthModal();

  useEffect(() => {
    getBookById(Number(id))
      .then(setBook)
      .catch(() => setError(true))
      .finally(() => setLoading(false));
  }, [id]);

  if (loading) {
    return (
      <div className="flex flex-col px-6 pt-8 md:px-12 md:pt-12 gap-8 max-w-4xl">
        <div className="h-6 w-24 bg-cream-book rounded animate-pulse" />
        <div className="flex flex-col md:flex-row gap-8">
          <div className="w-full md:w-48 h-64 bg-cream-book rounded animate-pulse shrink-0" />
          <div className="flex flex-col gap-4 flex-1">
            <div className="h-8 w-3/4 bg-cream-book rounded animate-pulse" />
            <div className="h-5 w-1/2 bg-cream-book rounded animate-pulse" />
            <div className="h-4 w-1/3 bg-cream-book rounded animate-pulse" />
          </div>
        </div>
      </div>
    );
  }

  if (error || !book) {
    return (
      <div className="flex flex-col items-center justify-center px-6 pt-24 gap-4">
        <p className="text-brown font-serif text-xl">Livro não encontrado.</p>
        <Link href="/explorar" className="text-terra text-sm font-bold underline">
          Voltar para Explorar
        </Link>
      </div>
    );
  }

  async function handleAddToLibrary() {
    if (!user) {
      openLoginModal();
      return;
    }
    setAdding(true);
    try {
      await addUserBook(book!.id);
      setAdded(true);
    } catch (e) {
      if (e instanceof Error && e.message === 'already_added') {
        setAdded(true);
      }
    } finally {
      setAdding(false);
    }
  }

  const subjects = book.subjects
    ? book.subjects.split(",").map((s) => s.trim()).filter(Boolean)
    : [];

  function getAddButtonLabel() {
    if (added) return 'Adicionado à Biblioteca';
    if (adding) return 'Adicionando...';
    return 'Adicionar à Biblioteca';
  }

  return (
    <div className="flex flex-col px-6 pt-8 pb-16 md:px-12 md:pt-12 gap-8 max-w-4xl">
      <Link
        href="/explorar"
        className="flex items-center gap-2 text-brown-soft text-sm font-bold hover:text-brown transition-colors w-fit"
      >
        <ArrowLeft size={16} />
        Voltar para Explorar
      </Link>

      <div className="flex flex-col md:flex-row gap-8 md:gap-12">
        {/* Capa */}
        <div className="shrink-0">
          {book.coverUrl ? (
            <img
              src={book.coverUrl}
              alt={book.title}
              className="w-full md:w-48 h-72 object-cover rounded-lg shadow-md"
            />
          ) : (
            <div className="w-full md:w-48 h-72 bg-cream-book rounded-lg flex items-center justify-center shadow-md">
              <span className="text-slate/30 text-xs uppercase tracking-widest">Sem capa</span>
            </div>
          )}
        </div>

        {/* Informações */}
        <div className="flex flex-col gap-6 flex-1">
          <div className="flex flex-col gap-2">
            <h1 className="font-serif font-bold text-brown text-3xl md:text-4xl leading-tight">
              {book.title}
            </h1>
            <p className="text-brown-soft text-lg">{getAuthorDisplay(book)}</p>
          </div>

          {subjects.length > 0 && (
            <div className="flex flex-wrap gap-2">
              {subjects.slice(0, 5).map((subject) => (
                <span
                  key={subject}
                  className="bg-cream-dark text-brown text-xs font-bold px-3 py-1 rounded-full border border-cream-border"
                >
                  {subject}
                </span>
              ))}
            </div>
          )}

          <dl className="flex flex-col gap-2 text-sm">
            {book.languages && (
              <div className="flex gap-2">
                <dt className="text-brown-soft font-bold w-24 shrink-0">Idioma</dt>
                <dd className="text-brown capitalize">{book.languages}</dd>
              </div>
            )}
            {book.source && (
              <div className="flex gap-2">
                <dt className="text-brown-soft font-bold w-24 shrink-0">Fonte</dt>
                <dd className="text-brown">{book.source}</dd>
              </div>
            )}
            {book.downloadCount != null && (
              <div className="flex gap-2">
                <dt className="text-brown-soft font-bold w-24 shrink-0">Downloads</dt>
                <dd className="text-brown">{book.downloadCount.toLocaleString()}</dd>
              </div>
            )}
          </dl>

          {/* Ações */}
          <div className="flex flex-col sm:flex-row gap-3 mt-2">
            {book.downloadUrl ? (
              <Link
                href={`/leitor/${book.id}`}
                className="flex items-center justify-center gap-2 bg-brown text-cream font-bold text-sm px-8 py-4 rounded-xl hover:bg-brown/90 transition-colors"
              >
                <BookOpen size={16} />
                Ler agora
              </Link>
            ) : (
              <span className="flex items-center justify-center gap-2 bg-cream-border text-brown-soft font-bold text-sm px-8 py-4 rounded-xl cursor-not-allowed">
                <BookOpen size={16} />
                Leitura indisponível
              </span>
            )}
            <button
              type="button"
              onClick={handleAddToLibrary}
              disabled={adding || added}
              className="flex items-center justify-center gap-2 border border-cream-border text-brown font-bold text-sm px-8 py-4 rounded-xl hover:bg-cream-active transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
            >
              <Plus size={16} />
              {getAddButtonLabel()}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
