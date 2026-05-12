"use client";

import Link from "next/link";
import { Plus } from "lucide-react";
import type { BookApiResponse } from "@/lib/api";

type Props = {
  book: BookApiResponse;
  onAdd?: (book: BookApiResponse) => void;
};

export default function BookCard({ book, onAdd }: Props) {
  return (
    <div className="flex flex-col gap-4 group">
      <Link href={`/explorar/${book.id}`} className="relative bg-cream-book rounded overflow-hidden shadow-sm block">
        {book.coverUrl ? (
          <img
            src={book.coverUrl}
            alt={book.title}
            className="w-full h-[250px] object-cover"
          />
        ) : (
          <div className="w-full h-[250px] flex items-center justify-center bg-cream-book">
            <span className="text-slate/30 text-xs uppercase tracking-widest">Sem capa</span>
          </div>
        )}
        <div className="absolute inset-0 bg-brown/40 backdrop-blur-sm opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center">
          <span className="bg-cream px-4 py-2 rounded-xl shadow-lg text-brown text-xs font-bold uppercase tracking-wide">
            Ver detalhes
          </span>
        </div>
      </Link>

      <div className="flex flex-col">
        <h4 className="font-serif font-bold text-brown text-base leading-6">{book.title}</h4>
        <p className="text-brown-soft text-sm leading-5 mb-4">{book.author}</p>
        <button
          type="button"
          onClick={() => onAdd?.(book)}
          className="border border-cream-border rounded-xl py-2 px-4 text-xs font-bold tracking-wide uppercase text-brown hover:bg-cream-active transition-colors flex items-center justify-center gap-2"
        >
          <Plus size={12} /> Adicionar à Coleção
        </button>
      </div>
    </div>
  );
}
