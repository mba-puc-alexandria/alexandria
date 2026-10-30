"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { useEffect, useState } from "react";
import {
  LayoutDashboard,
  BookOpen,
  Compass,
  Settings,
  HelpCircle,
  BookMarked,
  LogOut,
  Sparkles,
  PanelLeftClose,
  PanelLeftOpen,
} from "lucide-react";
import { useAuth } from "@/contexts/AuthContext";

const publicLinks = [
  { href: "/explorar", label: "Explorar", icon: Compass },
];

const privateLinks = [
  { href: "/biblioteca", label: "Biblioteca", icon: BookOpen },
  { href: "/dashboard", label: "Painel", icon: LayoutDashboard },
];

const privateBottomLinks = [
  { href: "/configuracoes", label: "Configurações", icon: Settings },
];

const bottomLinks = [
  { href: "/planos", label: "Planos", icon: Sparkles },
  { href: "/suporte", label: "Suporte", icon: HelpCircle },
];

export default function Sidebar() {
  const pathname = usePathname();
  const { logout, user } = useAuth();
  const [collapsed, setCollapsed] = useState(false);

  useEffect(() => {
    try {
      setCollapsed(localStorage.getItem("sidebar-collapsed") === "1");
    } catch {}
  }, []);

  function toggleCollapsed() {
    const next = !collapsed;
    setCollapsed(next);
    try {
      localStorage.setItem("sidebar-collapsed", next ? "1" : "0");
    } catch {}
    // O leitor EPUB só refaz o layout no resize da janela.
    setTimeout(() => window.dispatchEvent(new Event("resize")), 220);
  }

  return (
    <aside
      className={`flex flex-col shrink-0 h-full bg-cream py-6 border-r border-cream-border overflow-hidden transition-[width] duration-200 ${
        collapsed ? "w-16 px-2" : "w-64 px-6"
      }`}
    >
      <div className="mb-8">
        <Link href="/explorar" title="Alexandria" className={`flex items-center gap-3 ${collapsed ? "justify-center" : ""}`}>
          <BookMarked className="text-brown shrink-0" size={20} />
          <div className={collapsed ? "hidden" : ""}>
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
        {[...publicLinks, ...(user ? privateLinks : [])].map(({ href, label, icon: Icon }) => {
          const active = pathname === href || pathname.startsWith(href + "/");
          return (
            <Link
              key={href}
              href={href}
              title={label}
              aria-label={label}
              className={`flex items-center gap-3 py-3 rounded text-[15px] transition-colors ${
                collapsed ? "justify-center px-0" : "px-4"
              } ${
                active
                  ? "bg-cream-active text-brown font-bold"
                  : "text-slate font-medium hover:bg-cream-active/60"
              }`}
            >
              <Icon size={18} className="shrink-0" />
              {!collapsed && label}
            </Link>
          );
        })}
      </nav>

      <div className="border-t border-cream-border pt-6 flex flex-col gap-1">
        {[...(user ? privateBottomLinks : []), ...bottomLinks].map(({ href, label, icon: Icon }) => (
          <Link
            key={href}
            href={href}
            title={label}
            aria-label={label}
            className={`flex items-center gap-3 py-2 text-slate text-sm font-medium hover:text-brown transition-colors ${
              collapsed ? "justify-center px-0" : "px-4"
            }`}
          >
            <Icon size={18} className="shrink-0" />
            {!collapsed && label}
          </Link>
        ))}

        {user && (
          <div className="mt-4 pt-4 border-t border-cream-border">
            {!collapsed && (
              <p className="px-4 text-[11px] text-slate/50 uppercase tracking-widest font-bold mb-2">
                {user.username}
              </p>
            )}
            <button type="button"
              onClick={logout}
              title="Sair"
              aria-label="Sair"
              className={`flex items-center gap-3 py-2 w-full text-terra text-sm font-medium hover:bg-terra/10 rounded transition-colors ${
                collapsed ? "justify-center px-0" : "px-4"
              }`}
            >
              <LogOut size={18} className="shrink-0" />
              {!collapsed && "Sair"}
            </button>
          </div>
        )}

        <button type="button"
          onClick={toggleCollapsed}
          title={collapsed ? "Expandir menu" : "Recolher menu"}
          aria-label={collapsed ? "Expandir menu" : "Recolher menu"}
          aria-expanded={!collapsed}
          className={`mt-4 flex items-center gap-3 py-2 w-full text-slate text-sm font-medium hover:text-brown rounded transition-colors ${
            collapsed ? "justify-center px-0" : "px-4"
          }`}
        >
          {collapsed ? <PanelLeftOpen size={18} className="shrink-0" /> : <PanelLeftClose size={18} className="shrink-0" />}
          {!collapsed && "Recolher menu"}
        </button>
      </div>
    </aside>
  );
}
