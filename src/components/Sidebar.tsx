"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import {
  LayoutDashboard,
  BookOpen,
  ArrowLeftRight,
  Compass,
  Settings,
  HelpCircle,
  BookMarked,
} from "lucide-react";

const navLinks = [
  { href: "/dashboard", label: "Painel", icon: LayoutDashboard },
  { href: "/biblioteca", label: "Biblioteca", icon: BookOpen },
  { href: "/emprestimos", label: "Empréstimos", icon: ArrowLeftRight },
  { href: "/explorar", label: "Explorar", icon: Compass },
];

const bottomLinks = [
  { href: "/configuracoes", label: "Configurações", icon: Settings },
  { href: "/suporte", label: "Suporte", icon: HelpCircle },
];

export default function Sidebar() {
  const pathname = usePathname();

  return (
    <aside className="flex flex-col w-64 shrink-0 h-full bg-cream px-6 py-6 border-r border-cream-border">
      <div className="mb-8">
        <Link href="/explorar" className="flex items-center gap-3">
          <BookMarked className="text-brown" size={20} />
          <div>
            <p className="font-brand font-bold text-brown text-2xl leading-none">
              Alexandria
            </p>
            <p className="text-slate text-[10px] tracking-widest uppercase opacity-70 mt-1">
              BIBLIOTECA DIGITAL
            </p>
          </div>
        </Link>
      </div>

      <nav className="flex flex-col gap-1 flex-1">
        {navLinks.map(({ href, label, icon: Icon }) => {
          const active = pathname === href || pathname.startsWith(href + "/");
          return (
            <Link
              key={href}
              href={href}
              className={`flex items-center gap-3 px-4 py-3 rounded text-[15px] transition-colors ${
                active
                  ? "bg-cream-active text-brown font-bold"
                  : "text-slate font-medium hover:bg-cream-active/60"
              }`}
            >
              <Icon size={18} />
              {label}
            </Link>
          );
        })}
      </nav>

      <div className="border-t border-cream-border pt-6 flex flex-col gap-1">
        {bottomLinks.map(({ href, label, icon: Icon }) => (
          <Link
            key={href}
            href={href}
            className="flex items-center gap-3 px-4 py-2 text-slate text-sm font-medium hover:text-brown transition-colors"
          >
            <Icon size={18} />
            {label}
          </Link>
        ))}
      </div>
    </aside>
  );
}
