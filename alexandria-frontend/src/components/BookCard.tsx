"use client";

import { Star, Plus } from "lucide-react";
import type { Book } from "@/data/books";

type Props = {
  book: Book;
  onAdd?: (book: Book) => void;
};

export default function BookCard({ book, onAdd }: Props) {
  return (
    <div className="flex flex-col gap-4 group">
      <div className="relative bg-cream-book rounded overflow-hidden shadow-sm">
        <img
          src={book.cover}
          alt={book.title}
          className="w-full h-[250px] object-cover"
        />
        <div className="absolute inset-0 bg-brown/40 backdrop-blur-sm opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center">
          <button className="bg-cream p-4 rounded-xl shadow-lg">
            <Plus size={14} className="text-brown" />
          </button>
        </div>
      </div>

      <div className="flex flex-col">
        <div className="flex items-center gap-1 mb-1">
          <Star size={10} className="text-terra fill-terra" />
          <span className="text-terra text-xs font-bold">{book.rating.toFixed(1)}</span>
        </div>
        <h4 className="font-serif font-bold text-brown text-base leading-6">
          {book.title}
        </h4>
        <p className="text-brown-soft text-sm leading-5 mb-4">{book.author}</p>

        <button
          onClick={() => onAdd?.(book)}
          className="border border-cream-border rounded-xl py-2 px-4 text-xs font-bold tracking-wide uppercase text-brown hover:bg-cream-active transition-colors"
        >
          Adicionar à Coleção
        </button>
      </div>
    </div>
  );
}
