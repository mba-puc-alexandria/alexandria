import { Search, ArrowRight, ChevronLeft, ChevronRight, Plus } from "lucide-react";
import BookCard from "@/components/BookCard";
import { books } from "@/data/books";

export default function ExplorarPage() {
  return (
    <div className="flex flex-col">
      {/* ── MOBILE LAYOUT ── */}
      <div className="md:hidden px-6 pt-6 pb-8 flex flex-col gap-12">
        {/* Search */}
        <section className="flex flex-col gap-6">
          <h1 className="font-brand font-bold text-brown text-4xl tracking-tight">Explorar</h1>
          <div className="relative">
            <input
              type="text"
              placeholder="Títulos, autores ou assuntos..."
              className="w-full bg-cream-dark rounded-lg px-6 py-5 text-base text-slate placeholder:text-slate/50 outline-none"
            />
            <Search size={18} className="absolute right-4 top-1/2 -translate-y-1/2 text-slate/60" />
          </div>
        </section>

        {/* Featured collections */}
        <section className="flex flex-col gap-6">
          <div className="flex items-start justify-between">
            <h2 className="font-brand font-bold text-brown text-2xl leading-8">
              Curadorias em Destaque
            </h2>
            <button className="text-slate text-xs font-bold tracking-widest uppercase mt-1">
              VER TUDO
            </button>
          </div>

          <div className="grid grid-cols-12 gap-4">
            {/* Large card */}
            <div className="col-span-12 bg-cream-book rounded-lg overflow-hidden">
              <div className="h-48 bg-gradient-to-b from-slate/30 to-slate/60 relative">
                <img
                  src={books[0].cover}
                  alt=""
                  className="w-full h-full object-cover mix-blend-multiply opacity-80"
                />
                <div className="absolute inset-0 bg-gradient-to-t from-black/40 to-transparent" />
              </div>
              <div className="p-6 flex flex-col gap-2">
                <p className="text-terra text-[10px] font-extrabold tracking-widest uppercase">
                  ESSENCIAIS
                </p>
                <h3 className="font-brand font-bold text-brown text-xl leading-6">
                  Clássicos do Século XVIII
                </h3>
                <p className="text-slate text-sm leading-5">
                  Uma jornada pelas mentes que moldaram o pensamento moderno europeu.
                </p>
              </div>
            </div>

            {/* Small cards */}
            <div className="col-span-6 bg-cream-book rounded-lg p-5 flex flex-col justify-between min-h-[163px]">
              <BookOpen />
              <div>
                <h4 className="font-brand font-bold text-brown text-lg leading-[22px]">Poesia Visual</h4>
                <p className="text-slate text-[10px] uppercase tracking-widest">12 TÍTULOS</p>
              </div>
            </div>
            <div className="col-span-6 bg-[#521b00] rounded-lg p-5 flex flex-col justify-between min-h-[163px]">
              <div className="text-terra text-xs font-bold">★</div>
              <div>
                <h4 className="font-brand font-bold text-white/90 text-lg leading-[22px]">
                  Manuscritos Raros
                </h4>
                <p className="text-terra text-[10px] uppercase tracking-widest">COLEÇÃO PRIVADA</p>
              </div>
            </div>
          </div>
        </section>

        {/* Recommendations mobile */}
        <section className="flex flex-col gap-6">
          <h2 className="font-brand font-bold text-brown text-2xl">Recomendado</h2>
          <div className="flex flex-col gap-6">
            {books.slice(0, 4).map((book) => (
              <div key={book.id} className="bg-cream-dark rounded-lg p-4 flex gap-6 items-center">
                <img
                  src={book.cover}
                  alt={book.title}
                  className="w-20 h-28 object-cover rounded-sm shadow-sm shrink-0 border border-white/10"
                />
                <div className="flex flex-col flex-1">
                  <h4 className="font-brand font-bold text-brown text-lg leading-[22px]">
                    {book.title}
                  </h4>
                  <p className="text-slate text-sm">{book.author}</p>
                  <div className="flex items-center gap-3 mt-3">
                    <button className="bg-brown text-cream text-[10px] font-bold tracking-widest uppercase px-4 py-1.5 rounded-xl">
                      ADICIONAR
                    </button>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </section>

        {/* Personalized suggestion */}
        <button className="flex items-center justify-center gap-3 bg-cream-border border border-white/20 rounded-xl px-8 py-4 text-brown text-xs font-bold tracking-widest uppercase w-full">
          <Plus size={20} />
          SUGESTÃO PERSONALIZADA
        </button>
      </div>

      {/* ── DESKTOP LAYOUT ── */}
      <div className="hidden md:flex flex-col px-8 pt-12 pb-48 gap-16 max-w-5xl">
        {/* Hero */}
        <section className="grid grid-cols-12 gap-8 min-h-[320px] items-end">
          <div className="col-span-8 flex flex-col gap-4">
            <p className="text-terra text-xs font-extrabold tracking-[1.2px] uppercase">
              Motor de Descoberta
            </p>
            <h1 className="font-serif font-bold text-brown text-[96px] leading-none tracking-tight">
              Expanda seu<br />Estudo<br />Particular
            </h1>
          </div>
          <div className="col-span-4 pb-4">
            <p className="text-brown-soft text-lg font-light leading-relaxed">
              Cure sua coleção de literatura atemporal e pensamento contemporâneo. Cada adição é
              um passo em direção ao seu domínio pessoal.
            </p>
          </div>
        </section>

        {/* Search */}
        <section className="bg-cream-dark rounded-lg p-12 relative overflow-hidden">
          <div className="absolute right-0 top-0 w-1/3 h-full opacity-10 pointer-events-none">
            <div className="absolute -top-20 -right-20 w-72 h-52 border-8 border-brown rounded-full" />
          </div>
          <div className="flex gap-4 items-center relative">
            <div className="flex-1 relative">
              <input
                type="text"
                placeholder="Pesquisar por título, autor ou ISBN..."
                className="w-full bg-cream border border-cream-border border-b-2 border-b-brown-soft px-4 py-7 font-serif text-2xl text-cream-border placeholder:text-cream-border outline-none pr-12"
              />
              <Search size={33} className="absolute right-4 top-1/2 -translate-y-1/2 text-slate" />
            </div>
            <button className="bg-brown text-cream font-bold text-lg px-12 py-6 rounded-xl shadow-[0_24px_40px_rgba(48,13,0,0.06)] hover:bg-brown/90 transition-colors shrink-0">
              Buscar
            </button>
          </div>
        </section>

        {/* Featured Collections */}
        <section className="flex flex-col gap-8">
          <div className="flex items-baseline justify-between">
            <h2 className="font-serif font-bold text-brown text-3xl">Curadorias em Destaque</h2>
            <button className="text-terra text-sm font-bold tracking-widest uppercase">
              VER TUDO
            </button>
          </div>

          <div className="grid grid-cols-4 gap-6">
            <div className="col-span-2 row-span-2 bg-slate rounded-lg p-8 flex flex-col justify-between min-h-[392px] relative overflow-hidden">
              <div className="absolute inset-0 bg-gradient-to-br from-slate-600 to-slate-900 opacity-20" />
              <div className="relative flex flex-col gap-4">
                <div className="size-6 text-white">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={2}>
                    <path d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253" />
                  </svg>
                </div>
                <h3 className="font-serif font-bold text-white text-3xl">Filosofia Clássica</h3>
                <p className="text-blue-light/80 text-base leading-6 max-w-xs">
                  Um caminho curado através dos pensamentos fundamentais da civilização ocidental.
                </p>
              </div>
              <button className="relative bg-cream text-brown font-bold text-sm px-6 py-3 rounded-xl flex items-center gap-2 w-fit">
                Explorar Coleção
                <ArrowRight size={12} />
              </button>
            </div>

            <div className="col-span-2 bg-cream-border rounded-lg p-8 flex gap-6 items-center h-48">
              <img src={books[1].cover} alt="" className="w-24 h-32 object-cover rounded-sm shadow-lg shrink-0" />
              <div className="flex flex-col gap-1">
                <h4 className="font-serif font-bold text-brown text-xl">Minimalismo Moderno</h4>
                <p className="text-brown-soft text-sm">12 Livros sobre design e espaço.</p>
              </div>
            </div>

            <div className="col-span-1 bg-brown rounded-lg px-6 pt-16 pb-6 flex flex-col justify-end min-h-[200px]">
              <p className="text-cream/60 text-xs mb-4 font-bold">★ ★ ★ ★ ★</p>
              <h4 className="font-serif font-bold text-white text-lg leading-7">
                Escolha dos Críticos 2024
              </h4>
            </div>

            <div className="col-span-1 bg-cream-book rounded-lg px-6 pt-32 pb-6 flex flex-col justify-end min-h-[200px]">
              <p className="text-brown-soft text-xs font-bold tracking-widest uppercase mb-2">NOVIDADES</p>
              <h4 className="font-serif font-bold text-brown text-lg leading-7">Edições Raras</h4>
            </div>
          </div>
        </section>

        {/* Recommendations */}
        <section className="flex flex-col gap-8">
          <div className="flex items-center justify-between border-b border-cream-border pb-4">
            <h2 className="font-serif font-bold text-brown text-2xl">
              Recomendado para sua Biblioteca
            </h2>
            <div className="flex gap-2">
              <button className="p-2 rounded-xl hover:bg-cream-active transition-colors">
                <ChevronLeft size={18} className="text-brown" />
              </button>
              <button className="p-2 rounded-xl opacity-40 hover:bg-cream-active transition-colors">
                <ChevronRight size={18} className="text-brown" />
              </button>
            </div>
          </div>
          <div className="grid grid-cols-5 gap-8">
            {books.slice(0, 5).map((book) => (
              <BookCard key={book.id} book={book} />
            ))}
          </div>
        </section>
      </div>
    </div>
  );
}

function BookOpen() {
  return (
    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={1.5} className="text-slate">
      <path d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253" />
    </svg>
  );
}
