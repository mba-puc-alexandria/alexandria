"use client";

import { useState } from "react";
import Link from "next/link";
import { Plus, Check, Loader2 } from "lucide-react";
import { getAuthorDisplay, addUserBook, type BookApiResponse } from "@/lib/api";

type Props = {
  book: BookApiResponse;
};

export default function BookCard({ book }: Props) {
  const [state, setState] = useState<"idle" | "loading" | "added">("idle");

  function getStateIcon() {
    if (state === "loading") return <Loader2 size={16} className="animate-spin" />;
    if (state === "added") return <Check size={16} />;
    return <Plus size={16} />;
  }

  async function handleAdd(e: React.MouseEvent) {
    e.preventDefault();
    e.stopPropagation();
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

  return (
    <div className="flex flex-col gap-3 group">
      <div className="relative bg-cream-book rounded overflow-hidden shadow-sm">
        <Link href={`/explorar/${book.id}`} className="block">
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
        </Link>
        <div className="absolute inset-0 bg-brown/40 backdrop-blur-sm opacity-0 group-hover:opacity-100 transition-opacity flex items-end justify-between gap-2 p-3 pointer-events-none group-hover:pointer-events-auto">
          <Link
            href={`/explorar/${book.id}`}
            className="flex-1 h-12 flex items-center justify-center bg-brown text-cream font-bold text-sm rounded-xl hover:bg-brown/80 transition-colors cursor-pointer whitespace-nowrap"
          >
            Detalhes
          </Link>
          <button
            type="button"
            onClick={handleAdd}
            className="w-12 h-12 flex items-center justify-center bg-brown text-cream rounded-xl hover:bg-brown/80 hover:scale-110 transition-all duration-200 cursor-pointer shrink-0"
          >
            {getStateIcon()}
          </button>
        </div>
      </div>

      <div className="flex flex-col">
        <h4 className="font-serif font-bold text-brown text-base leading-6">{book.title}</h4>
        <p className="text-brown-soft text-sm leading-5">{getAuthorDisplay(book)}</p>
      </div>
    </div>
  );
}
