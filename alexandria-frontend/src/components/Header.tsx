"use client";

import { Search, Bell } from "lucide-react";

export default function Header() {
  return (
    <header className="flex items-center justify-between px-8 py-4 bg-cream border-b border-cream-border shrink-0 z-10">
      <div className="w-48" />

      <div className="flex items-center gap-4">
        <div className="relative">
          <Search
            size={16}
            className="absolute left-3 top-1/2 -translate-y-1/2 text-slate opacity-60"
          />
          <input
            type="text"
            placeholder="Pesquisar clássicos, ensaios..."
            className="bg-cream-dark text-sm text-slate placeholder:text-slate/60 rounded-xl pl-9 pr-4 py-2 w-64 outline-none border border-transparent focus:border-cream-border transition-colors"
          />
        </div>

        <button className="p-2 rounded-xl text-slate hover:bg-cream-active transition-colors">
          <Bell size={18} />
        </button>

        <div className="size-8 rounded-xl bg-cream-border overflow-hidden shrink-0">
          <div className="size-full bg-gradient-to-br from-slate to-brown opacity-60 rounded-xl" />
        </div>
      </div>
    </header>
  );
}
