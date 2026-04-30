import { books } from "@/data/books";
import { BookOpen, Clock, TrendingUp, Star, Plus } from "lucide-react";

export default function DashboardPage() {
  const readingBooks = books.filter((b) => b.status === "reading");

  return (
    <div className="px-6 md:px-8 pt-8 md:pt-12 pb-8 flex flex-col gap-8 md:gap-12 max-w-5xl">
      {/* Title */}
      <h1 className="font-brand font-bold text-brown text-4xl md:text-4xl tracking-tight">
        Visão Geral da Curadoria
      </h1>

      {/* Bento stats — mobile: 2 cols, desktop: 4 cols */}
      <section className="grid grid-cols-2 md:grid-cols-4 gap-4">
        {/* Main stat - full width on mobile, spans 2 on desktop */}
        <div className="col-span-2 bg-cream-dark rounded-lg p-6 flex flex-col justify-between min-h-[202px]">
          <div className="flex flex-col gap-2">
            <p className="text-slate text-[10px] tracking-widest uppercase">META PESSOAL</p>
            <div className="flex items-baseline gap-1">
              <span className="font-serif font-bold text-brown text-4xl">24</span>
              <span className="font-serif text-slate text-base italic">/ 50</span>
            </div>
            <p className="text-slate text-xs leading-4">
              Livros curados este ano. Você está adiantado em 3 volumes.
            </p>
          </div>
          <div className="flex flex-col gap-2 pt-6">
            <p className="text-brown text-[10px] font-bold tracking-wider uppercase">
              MINUTOS DE LEITURA
            </p>
            <div className="bg-cream-border h-1 rounded-full overflow-hidden">
              <div className="bg-terra h-full w-1/2" />
            </div>
          </div>
        </div>

        {/* Stat 2 */}
        <div className="bg-cream-border rounded-lg p-5 flex flex-col items-center justify-center gap-2 h-[134px]">
          <Clock size={22} className="text-slate mb-1" />
          <p className="font-serif font-bold text-brown text-xl">1,240</p>
          <p className="text-slate text-[9px] tracking-widest uppercase text-center">
            MINUTOS DE LEITURA
          </p>
        </div>

        {/* Stat 3 */}
        <div className="bg-cream-book rounded-lg p-5 flex flex-col items-center justify-center gap-2 h-[134px]">
          <TrendingUp size={20} className="text-slate mb-1" />
          <p className="font-serif font-bold text-brown text-xl">12</p>
          <p className="text-slate text-[9px] tracking-widest uppercase text-center">
            EMPRÉSTIMOS ATIVOS
          </p>
        </div>
      </section>

      {/* Currently Reading */}
      <section className="flex flex-col gap-6">
        <div className="flex items-end justify-between">
          <div>
            <h2 className="font-serif font-bold text-brown text-xl">Lendo Agora</h2>
            <p className="text-slate text-xs mt-0.5">Suas buscas intelectuais em andamento</p>
          </div>
          <button className="text-terra text-[10px] font-bold tracking-widest uppercase">
            VER TUDO
          </button>
        </div>

        <div className="flex flex-col gap-4">
          {readingBooks.length > 0 ? (
            readingBooks.map((book) => (
              <div key={book.id} className="bg-cream-dark rounded-lg p-4 flex gap-4">
                <img
                  src={book.cover}
                  alt={book.title}
                  className="w-24 h-36 object-cover rounded-sm shadow-sm shrink-0"
                />
                <div className="flex flex-col justify-between flex-1">
                  <div className="flex flex-col gap-1">
                    <span className="bg-slate/10 text-blue-light text-[9px] font-bold tracking-widest uppercase px-1.5 py-0.5 rounded w-fit">
                      {book.genre}
                    </span>
                    <h3 className="font-serif font-bold text-brown text-lg leading-7">
                      {book.title}
                    </h3>
                  </div>
                  <div className="flex flex-col gap-2">
                    <div className="flex justify-between text-[9px] font-bold text-slate uppercase tracking-tight">
                      <span>72% CONCLUÍDO</span>
                      <span>32 PÁG. REST.</span>
                    </div>
                    <div className="bg-cream-border h-1 rounded-full overflow-hidden">
                      <div className="bg-terra h-full" style={{ width: "72%" }} />
                    </div>
                    <button className="flex items-center gap-1.5 text-brown text-[10px] font-bold mt-2">
                      <BookOpen size={10} />
                      Retomar Leitura
                    </button>
                  </div>
                </div>
              </div>
            ))
          ) : (
            <p className="text-slate text-sm">Nenhum livro em leitura no momento.</p>
          )}
        </div>
      </section>

      {/* Curator suggestion card */}
      <section className="bg-brown rounded-2xl p-7 flex flex-col gap-4">
        <Star size={33} className="text-terra" />
        <div className="flex flex-col gap-3">
          <h3 className="font-serif font-bold text-white text-xl">Sugestão do Curador</h3>
          <p className="text-white/80 text-sm leading-[21px]">
            Com base no seu interesse em &quot;Meditações&quot;, recomendamos explorar os
            princípios estóicos em{" "}
            <span className="font-medium">Cartas de um Estóico</span> de Sêneca.
          </p>
        </div>
        <button className="bg-terra text-white font-bold text-xs tracking-widest uppercase py-3.5 rounded-xl text-center">
          ADICIONAR À BIBLIOTECA
        </button>
      </section>

      {/* Desktop: Recent additions */}
      <section className="hidden md:flex flex-col gap-6">
        <h2 className="font-serif font-bold text-brown text-2xl">Adições Recentes</h2>
        <div className="flex flex-col divide-y divide-cream-border">
          {books.slice(0, 5).map((book) => (
            <div key={book.id} className="flex items-center gap-4 py-4">
              <img src={book.cover} alt={book.title} className="w-10 h-14 object-cover rounded shrink-0" />
              <div className="flex-1">
                <p className="font-serif font-bold text-brown text-sm">{book.title}</p>
                <p className="text-slate text-xs">{book.author}</p>
              </div>
              <div className="flex items-center gap-1 text-terra text-xs font-bold">
                <Star size={10} className="fill-terra" />
                {book.rating}
              </div>
              <button className="size-8 rounded-full bg-cream-dark flex items-center justify-center text-slate hover:bg-cream-active transition-colors">
                <Plus size={14} />
              </button>
            </div>
          ))}
        </div>
      </section>
    </div>
  );
}
