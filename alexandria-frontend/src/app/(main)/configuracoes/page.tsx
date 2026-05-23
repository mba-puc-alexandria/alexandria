"use client";

import { Settings, User, Bell, Moon } from "lucide-react";
import { useAuth } from "@/contexts/AuthContext";

export default function ConfiguracoesPage() {
  const { user } = useAuth();

  return (
    <div className="px-6 md:px-8 pt-8 md:pt-12 pb-8 flex flex-col gap-8 max-w-2xl">
      <div>
        <h1 className="font-serif font-bold text-brown text-2xl">Configurações</h1>
        <p className="text-slate text-sm mt-1">Gerencie sua conta e preferências</p>
      </div>

      {/* Conta */}
      <section className="flex flex-col gap-3">
        <div className="flex items-center gap-2 mb-1">
          <User size={14} className="text-terra" />
          <span className="text-brown text-xs font-bold uppercase tracking-widest">Conta</span>
        </div>

        <div className="bg-cream-dark rounded-xl p-5 flex flex-col gap-4">
          <div className="flex flex-col gap-1">
            <span className="text-xs font-bold text-slate uppercase tracking-widest">Usuário</span>
            <span className="text-brown font-medium">{user?.username ?? "—"}</span>
          </div>
          <div className="border-t border-cream-border pt-4">
            <p className="text-slate/60 text-xs">
              Alteração de senha e e-mail em breve.
            </p>
          </div>
        </div>
      </section>

      {/* Notificações */}
      <section className="flex flex-col gap-3">
        <div className="flex items-center gap-2 mb-1">
          <Bell size={14} className="text-terra" />
          <span className="text-brown text-xs font-bold uppercase tracking-widest">Notificações</span>
        </div>

        <div className="bg-cream-dark rounded-xl p-5">
          <p className="text-slate/60 text-xs">Em breve.</p>
        </div>
      </section>

      {/* Aparência */}
      <section className="flex flex-col gap-3">
        <div className="flex items-center gap-2 mb-1">
          <Moon size={14} className="text-terra" />
          <span className="text-brown text-xs font-bold uppercase tracking-widest">Aparência</span>
        </div>

        <div className="bg-cream-dark rounded-xl p-5">
          <p className="text-slate/60 text-xs">Em breve.</p>
        </div>
      </section>
    </div>
  );
}
