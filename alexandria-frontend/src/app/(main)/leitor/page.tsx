import { books } from "@/data/books";
import { ChevronLeft, ChevronRight, ZoomIn, ZoomOut, Bookmark } from "lucide-react";
import Link from "next/link";

export default function LeitorPage() {
  const book = books[1]; // Meditations as default

  return (
    <div className="flex flex-col h-full">
      {/* Reader header */}
      <div className="flex items-center justify-between px-8 py-4 bg-cream-dark border-b border-cream-border shrink-0">
        <Link href="/biblioteca" className="flex items-center gap-2 text-slate text-sm hover:text-brown transition-colors">
          <ChevronLeft size={16} />
          Voltar à Biblioteca
        </Link>
        <div className="text-center">
          <p className="font-serif font-bold text-brown text-base">{book.title}</p>
          <p className="text-slate text-xs">{book.author}</p>
        </div>
        <div className="flex items-center gap-2">
          <button className="p-2 rounded-lg text-slate hover:bg-cream-active transition-colors">
            <ZoomOut size={16} />
          </button>
          <span className="text-slate text-sm px-2">100%</span>
          <button className="p-2 rounded-lg text-slate hover:bg-cream-active transition-colors">
            <ZoomIn size={16} />
          </button>
          <button className="p-2 rounded-lg text-terra hover:bg-terra/10 transition-colors">
            <Bookmark size={16} />
          </button>
        </div>
      </div>

      {/* PDF Viewer area */}
      <div className="flex-1 flex items-center justify-center bg-brown-soft/10 overflow-auto py-8">
        <div className="bg-white shadow-2xl w-[595px] min-h-[842px] p-16 flex flex-col gap-6">
          <h2 className="font-serif font-bold text-brown text-3xl">{book.title}</h2>
          <p className="text-slate text-sm font-medium">{book.author}</p>
          <div className="w-12 h-px bg-cream-border" />
          <p className="text-brown-soft text-base leading-8">
            {book.description}
          </p>
          <p className="text-brown-soft/60 text-sm leading-7">
            Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do
            eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim
            ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut
            aliquip ex ea commodo consequat.
          </p>
          <p className="text-brown-soft/60 text-sm leading-7">
            Duis aute irure dolor in reprehenderit in voluptate velit esse
            cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat
            cupidatat non proident, sunt in culpa qui officia deserunt mollit
            anim id est laborum.
          </p>
        </div>
      </div>

      {/* Navigation */}
      <div className="flex items-center justify-between px-8 py-4 bg-cream-dark border-t border-cream-border shrink-0">
        <button className="flex items-center gap-2 text-slate text-sm hover:text-brown transition-colors">
          <ChevronLeft size={16} />
          Página anterior
        </button>
        <p className="text-slate text-sm">Página 1 de {book.pages}</p>
        <button className="flex items-center gap-2 text-slate text-sm hover:text-brown transition-colors">
          Próxima página
          <ChevronRight size={16} />
        </button>
      </div>
    </div>
  );
}
