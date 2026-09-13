"use client";

import Link from "next/link";
import { Lock, Sparkles, X } from "lucide-react";

interface PaywallModalProps {
  open: boolean;
  onClose: () => void;
}

export default function PaywallModal({ open, onClose }: PaywallModalProps) {
  if (!open) return null;

  return (
    <div
      className="modal-overlay fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4"
      onClick={onClose}
    >
      <div
        className="modal-card bg-cream rounded-2xl max-w-md w-full p-8 shadow-2xl relative"
        onClick={(e) => e.stopPropagation()}
      >
        <button
          onClick={onClose}
          className="absolute top-4 right-4 p-2 rounded-lg text-brown-soft hover:bg-cream-active transition-colors"
          aria-label="Fechar"
        >
          <X size={18} />
        </button>

        <div className="flex flex-col items-center text-center">
          <div className="inline-flex items-center justify-center bg-terra/10 rounded-full p-5 mb-5">
            <Lock size={28} className="text-terra" />
          </div>

          <h2 className="font-serif font-bold text-brown text-2xl mb-2">
            Alexandria Premium
          </h2>
          <p className="text-slate text-sm mb-6">
            Seu período de teste terminou. Assine o Alexandria Premium para continuar
            lendo seus livros sem interrupções.
          </p>

          <div className="bg-cream-dark rounded-xl p-5 border border-cream-border w-full mb-6">
            <div className="flex items-baseline justify-center gap-1">
              <span className="text-brown text-sm font-bold">R$</span>
              <span className="font-serif font-bold text-brown text-4xl">10,00</span>
              <span className="text-brown-soft text-sm">/mês</span>
            </div>
            <p className="text-brown-soft/70 text-xs mt-2 flex items-center justify-center gap-1.5">
              <Sparkles size={12} />
              15 dias grátis para novos assinantes
            </p>
          </div>

          <Link
            href="/checkout"
            className="bg-brown text-cream text-center font-bold text-sm tracking-widest uppercase px-6 py-4 rounded-xl hover:bg-brown/90 transition-colors w-full"
          >
            Assinar agora
          </Link>
          <Link
            href="/planos"
            className="text-terra text-sm font-bold hover:underline mt-4"
          >
            Ver planos
          </Link>
        </div>
      </div>
    </div>
  );
}
