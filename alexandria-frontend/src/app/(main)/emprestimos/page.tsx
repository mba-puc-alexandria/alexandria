import { Plus } from "lucide-react";

type LoanStatus = "overdue" | "active" | "returned";

const loans = [
  {
    title: "O Nome da Rosa",
    author: "Tomás de Aquino",
    status: "overdue" as LoanStatus,
    dueLabel: "VENCEU EM",
    dueDate: "12 Out, 2023",
    borrower: "Lucas Oliveira",
  },
  {
    title: "Crítica da Razão Pura",
    author: "Immanuel Kant",
    status: "active" as LoanStatus,
    dueLabel: "ENTREGA EM",
    dueDate: "28 Out, 2023",
    borrower: "Beatriz Santos",
  },
  {
    title: "Cem Anos de Solidão",
    author: "Gabriel García Márquez",
    status: "active" as LoanStatus,
    dueLabel: "ENTREGA EM",
    dueDate: "05 Nov, 2023",
    borrower: "Ricardo Mendes",
  },
  {
    title: "The Alchemist",
    author: "Paulo Coelho",
    status: "returned" as LoanStatus,
    dueLabel: "DEVOLVIDO EM",
    dueDate: "01 Abr, 2026",
    borrower: "Maria Oliveira",
  },
];

const statusStyle: Record<LoanStatus, { badge: string; text: string; label: string }> = {
  overdue: { badge: "bg-[#ffdad6] text-[#93000a]", text: "text-[#93000a]", label: "ATRASADO" },
  active: { badge: "bg-cream-dark text-slate", text: "text-brown", label: "NO PRAZO" },
  returned: { badge: "bg-green-50 text-green-700", text: "text-brown", label: "DEVOLVIDO" },
};

export default function EmprestimosPage() {
  const active = loans.filter((l) => l.status === "active").length;
  const overdue = loans.filter((l) => l.status === "overdue").length;

  return (
    <div className="px-6 md:px-8 pt-8 md:pt-12 pb-8 flex flex-col gap-8 max-w-5xl relative">
      <h1 className="font-brand font-bold text-brown text-4xl tracking-tight">Empréstimos</h1>

      {/* Stats */}
      <div className="grid grid-cols-2 gap-4">
        <div className="bg-cream-dark rounded-lg p-5 flex flex-col justify-between h-32">
          <div className="size-4 text-slate">
            <svg viewBox="0 0 16 16" fill="currentColor">
              <rect x="2" y="2" width="5" height="12" rx="1" />
              <rect x="9" y="5" width="5" height="9" rx="1" />
            </svg>
          </div>
          <div>
            <p className="font-serif font-bold text-brown text-2xl">{active + overdue}</p>
            <p className="text-slate text-[10px] tracking-widest uppercase">ATIVOS</p>
          </div>
        </div>
        <div className="bg-cream-border rounded-lg p-5 flex flex-col justify-between h-32">
          <div className="text-[#93000a] text-lg font-bold">!</div>
          <div>
            <p className="font-serif font-bold text-[#93000a] text-2xl">0{overdue}</p>
            <p className="text-[#93000a]/70 text-[10px] tracking-widest uppercase">ATRASADOS</p>
          </div>
        </div>
      </div>

      {/* Loan list */}
      <section className="flex flex-col gap-6">
        <h3 className="text-slate/60 text-xs font-bold tracking-widest uppercase px-1">
          REGISTROS RECENTES
        </h3>

        <div className="flex flex-col gap-4">
          {loans.map((loan) => {
            const s = statusStyle[loan.status];
            return (
              <div key={loan.title} className="bg-cream-book rounded-lg p-6 flex flex-col gap-6">
                <div className="flex items-start justify-between gap-4">
                  <div className="flex flex-col gap-1 flex-1">
                    <h4 className="font-brand font-bold text-brown text-xl leading-[25px]">
                      {loan.title}
                    </h4>
                    <p className="text-slate text-sm">{loan.author}</p>
                  </div>
                  <span className={`${s.badge} text-[10px] font-bold tracking-tight uppercase px-3 py-1 rounded-full shrink-0`}>
                    {s.label}
                  </span>
                </div>

                <div className="flex gap-6 pt-1 border-t border-white/10">
                  <div>
                    <p className="text-slate/60 text-[9px] tracking-widest uppercase">{loan.dueLabel}</p>
                    <p className={`${s.text} text-sm font-medium`}>{loan.dueDate}</p>
                  </div>
                  <div>
                    <p className="text-slate/60 text-[9px] tracking-widest uppercase">PORTADOR</p>
                    <p className="text-brown text-sm font-medium">{loan.borrower}</p>
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      </section>

      {/* FAB */}
      <button className="fixed bottom-20 md:bottom-8 right-6 bg-brown text-cream size-14 rounded-xl flex items-center justify-center shadow-xl hover:bg-brown/90 transition-colors">
        <Plus size={14} />
      </button>
    </div>
  );
}
