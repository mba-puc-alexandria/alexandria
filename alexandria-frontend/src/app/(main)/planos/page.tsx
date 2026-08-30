"use client";

import Link from "next/link";
import {
  Sparkles,
  ShieldCheck,
  BookOpen,
  Clock,
  QrCode,
  CreditCard,
} from "lucide-react";
import { useAuth } from "@/contexts/AuthContext";

const FEATURES = [
  { icon: BookOpen, label: "Acesso a todos os EPUBs da biblioteca" },
  { icon: Clock, label: "Leitor com progresso e biblioteca pessoal" },
  { icon: QrCode, label: "Pague como preferir: PIX ou cartão" },
  { icon: CreditCard, label: "Sem fidelidade — cancele quando quiser" },
  { icon: ShieldCheck, label: "Pagamento processado pelo Mercado Pago" },
];

export default function PlanosPage() {
  const { user } = useAuth();

  return (
    <div className="px-6 md:px-8 pt-8 md:pt-12 pb-8 flex flex-col items-center">
      {/* Cabeçalho */}
      <div className="text-center max-w-2xl mb-8">
        <div className="inline-flex items-center gap-2 bg-brown/5 text-brown rounded-full px-4 py-1.5 text-xs font-bold uppercase tracking-widest mb-5">
          <Sparkles size={14} className="text-terra" />
          Assinatura Alexandria
        </div>
        <h1 className="font-serif font-bold text-brown text-3xl md:text-4xl leading-tight">
          Continue sua leitura sem interrupções
        </h1>
        <p className="text-slate text-sm md:text-base mt-3">
          Comece com <strong className="text-brown">15 dias grátis</strong>. Depois, apenas{" "}
          <strong className="text-brown">R$ 10,00 por mês</strong> pelo acesso completo ao leitor,
          sua biblioteca e o progresso de leitura.
        </p>
      </div>

      {/* Card de preço */}
      <div className="w-full max-w-md">
        <div className="bg-cream-dark rounded-2xl p-8 border border-cream-border shadow-sm flex flex-col">
          <div className="flex items-end justify-between mb-6">
            <div>
              <span className="text-brown-soft text-xs font-bold uppercase tracking-widest">
                Alexandria Premium
              </span>
              <div className="flex items-baseline gap-1 mt-2">
                <span className="text-brown text-sm font-bold">R$</span>
                <span className="font-serif font-bold text-brown text-5xl">10,00</span>
                <span className="text-brown-soft text-sm">/mês</span>
              </div>
            </div>
            <div className="bg-terra/10 text-terra rounded-full px-3 py-1 text-[11px] font-bold uppercase tracking-wide">
              15 dias grátis
            </div>
          </div>

          <ul className="flex flex-col gap-3 mb-8">
            {FEATURES.map(({ icon: Icon, label }) => (
              <li key={label} className="flex items-start gap-3">
                <span className="bg-brown/5 rounded-lg p-1.5 mt-0.5">
                  <Icon size={16} className="text-terra" />
                </span>
                <span className="text-brown text-sm">{label}</span>
              </li>
            ))}
          </ul>

          {user ? (
            <Link
              href="/checkout"
              className="bg-brown text-cream text-center font-bold text-sm tracking-widest uppercase px-6 py-4 rounded-xl hover:bg-brown/90 transition-colors"
            >
              Começar teste grátis
            </Link>
          ) : (
            <Link
              href="/registrar"
              className="bg-brown text-cream text-center font-bold text-sm tracking-widest uppercase px-6 py-4 rounded-xl hover:bg-brown/90 transition-colors"
            >
              Criar conta grátis
            </Link>
          )}

          <p className="text-center text-brown-soft/70 text-xs mt-4">
            Sem cobrança durante o período de teste. Cancele quando quiser.
          </p>
        </div>

        <p className="text-center text-brown-soft/60 text-xs mt-6 flex items-center justify-center gap-1.5">
          <ShieldCheck size={14} />
          Pagamento seguro processado pelo Mercado Pago
        </p>
      </div>
    </div>
  );
}
