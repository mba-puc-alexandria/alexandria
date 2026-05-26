"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { LayoutDashboard, BookOpen, Compass, LogOut, LogIn } from "lucide-react";
import { useAuth } from "@/contexts/AuthContext";
import { useAuthModal } from "@/contexts/AuthModalContext";

const publicItems = [
  { href: "/explorar", label: "Explorar", icon: Compass },
];

const privateItems = [
  { href: "/biblioteca", label: "Biblioteca", icon: BookOpen },
  { href: "/dashboard", label: "Painel", icon: LayoutDashboard },
];

export default function BottomNav() {
  const pathname = usePathname();
  const { user, logout } = useAuth();
  const { openLoginModal } = useAuthModal();

  const items = user ? [...publicItems, ...privateItems] : publicItems;

  return (
    <nav className="md:hidden fixed bottom-0 left-0 right-0 z-30 bg-cream/95 backdrop-blur-sm border-t border-cream-border flex items-center justify-around px-2 pb-safe pt-2 h-[64px]">
      {items.map(({ href, label, icon: Icon }) => {
        const active = pathname === href || pathname.startsWith(href + "/");
        return (
          <Link
            key={href}
            href={href}
            className={`flex flex-col items-center gap-1 px-4 py-1 rounded-xl transition-colors ${
              active ? "text-brown" : "text-slate/60"
            }`}
          >
            <Icon size={20} strokeWidth={active ? 2.5 : 1.5} />
            <span className={`text-[9px] tracking-wide uppercase font-bold ${active ? "text-brown" : "text-slate/50"}`}>
              {label}
            </span>
          </Link>
        );
      })}

      {user ? (
        <button
          onClick={logout}
          className="flex flex-col items-center gap-1 px-4 py-1 rounded-xl text-terra/70 transition-colors"
        >
          <LogOut size={20} strokeWidth={1.5} />
          <span className="text-[9px] tracking-wide uppercase font-bold text-terra/50">Sair</span>
        </button>
      ) : (
        <button
          onClick={openLoginModal}
          className="flex flex-col items-center gap-1 px-4 py-1 rounded-xl text-slate/60"
        >
          <LogIn size={20} strokeWidth={1.5} />
          <span className="text-[9px] tracking-wide uppercase font-bold text-slate/50">Entrar</span>
        </button>
      )}
    </nav>
  );
}
