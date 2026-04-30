import { Search, Plus, Star } from "lucide-react";

const mobileBooks = [
  {
    title: "O Nome da Rosa",
    author: "Umberto Eco",
    cover: "https://www.figma.com/api/mcp/asset/4ba557dc-8b46-4302-a3a4-967b02ef34a0",
    status: "reading" as const,
    progress: 75,
  },
  {
    title: "Ensaio Sobre a Cegueira",
    author: "José Saramago",
    cover: "https://www.figma.com/api/mcp/asset/532ba3eb-ef76-4f5b-be24-f5d87534982f",
    status: "toread" as const,
  },
  {
    title: "Dom Casmurro",
    author: "Machado de Assis",
    cover: "https://www.figma.com/api/mcp/asset/97e5d948-4ae0-4468-92ed-6748f55bfefa",
    status: "done" as const,
    rating: 5,
  },
  {
    title: "1984",
    author: "George Orwell",
    cover: "https://www.figma.com/api/mcp/asset/301f24e2-c594-4398-919e-91cf15957c38",
    status: "reading" as const,
    progress: 20,
  },
  {
    title: "Grande Sertão: Veredas",
    author: "Guimarães Rosa",
    cover: "https://www.figma.com/api/mcp/asset/929d3294-b561-4cd6-a1ed-f5514c93ea10",
    status: "borrowed" as const,
  },
  {
    title: "Cem Anos de Solidão",
    author: "Gabriel García Márquez",
    cover: "https://www.figma.com/api/mcp/asset/8dcc2c48-03b5-44a9-bd30-2434cce9dd6c",
    status: "done" as const,
    rating: 5,
  },
];

const filters = ["Todas as Coleções", "Lendo", "Concluído", "Para Ler", "Emprestado"];

const statusBadge = {
  reading: { label: "LENDO AGORA", color: "text-slate/60" },
  toread: { label: "PARA LER", color: "text-terra" },
  done: { label: "CONCLUÍDO", color: "text-slate/60" },
  borrowed: { label: "EMPRESTADO", color: "text-blue-light" },
};

export default function BibliotecaPage() {
  return (
    <div className="px-6 md:px-8 pt-8 md:pt-12 pb-8 flex flex-col gap-8 max-w-5xl relative">
      {/* Title + search */}
      <div className="flex flex-col gap-6">
        <h1 className="font-brand font-bold text-brown text-4xl tracking-tight">Sua Biblioteca</h1>
        <div className="relative">
          <Search size={18} className="absolute left-4 top-1/2 -translate-y-1/2 text-slate/60" />
          <input
            type="text"
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
            className={`shrink-0 px-5 py-2.5 rounded-xl text-sm font-medium transition-colors ${
              i === 0 ? "bg-brown text-cream" : "bg-cream-book text-slate hover:bg-cream-active"
            }`}
          >
            {f}
          </button>
        ))}
      </div>

      {/* Book grid — 2 cols mobile, 4 cols desktop */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-x-6 gap-y-10">
        {mobileBooks.map((book) => {
          const badge = statusBadge[book.status];
          return (
            <div key={book.title} className="flex flex-col">
              <div className="relative mb-4 overflow-hidden rounded">
                <img
                  src={book.cover}
                  alt={book.title}
                  className="w-full h-[239px] object-cover"
                />
              </div>

              <h3 className="font-brand font-bold text-brown text-lg leading-[22px]">
                {book.title}
              </h3>
              <p className="text-slate/70 text-xs mt-1">{book.author}</p>

              <div className="mt-3">
                {book.status === "reading" && book.progress !== undefined && (
                  <div className="flex flex-col gap-1.5">
                    <div className="bg-cream-border h-1 rounded-full overflow-hidden">
                      <div className="bg-terra h-full" style={{ width: `${book.progress}%` }} />
                    </div>
                    <p className="text-[10px] text-slate/60 tracking-widest uppercase">
                      {book.progress}% CONCLUÍDO
                    </p>
                  </div>
                )}
                {book.status === "toread" && (
                  <span className="bg-[#ffdbcd] text-terra text-[10px] tracking-widest uppercase font-medium px-2 py-1 rounded-sm">
                    PARA LER
                  </span>
                )}
                {book.status === "done" && book.rating !== undefined && (
                  <div className="flex items-center gap-1">
                    {Array.from({ length: book.rating }).map((_, i) => (
                      <Star key={i} size={11} className="text-terra fill-terra" />
                    ))}
                  </div>
                )}
                {book.status === "done" && !book.rating && (
                  <div className="flex items-center gap-1.5 text-slate/60">
                    <Star size={12} />
                    <p className="text-[10px] tracking-widest uppercase">CONCLUÍDO</p>
                  </div>
                )}
                {book.status === "borrowed" && (
                  <span className="bg-slate/10 text-blue-light text-[10px] tracking-widest uppercase font-medium px-2 py-1 rounded-sm">
                    EMPRESTADO
                  </span>
                )}
              </div>
            </div>
          );
        })}
      </div>

      {/* FAB */}
      <button className="fixed bottom-20 md:bottom-8 right-6 bg-brown text-cream size-14 rounded-xl flex items-center justify-center shadow-xl hover:bg-brown/90 transition-colors">
        <Plus size={14} />
      </button>
    </div>
  );
}
