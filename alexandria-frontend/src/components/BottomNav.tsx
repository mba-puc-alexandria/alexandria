"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { LayoutDashboard, BookOpen, ArrowLeftRight, Compass } from "lucide-react";

const navItems = [
  { href: "/dashboard", label: "Painel", icon: LayoutDashboard },
  { href: "/biblioteca", label: "Biblioteca", icon: BookOpen },
  { href: "/emprestimos", label: "Empréstimos", icon: ArrowLeftRight },
  { href: "/explorar", label: "Explorar", icon: Compass },
];

export default function BottomNav() {
  const pathname = usePathname();

  return (
    <nav className="md:hidden fixed bottom-0 left-0 right-0 z-30 bg-cream/95 backdrop-blur-sm border-t border-cream-border flex items-center justify-around px-2 pb-safe pt-2 h-[64px]">
      {navItems.map(({ href, label, icon: Icon }) => {
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
    </nav>
  );
}
