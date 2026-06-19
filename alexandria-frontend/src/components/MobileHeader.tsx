"use client";

import { useState, useEffect, useRef } from "react";
import Link from "next/link";
import { Sun, Moon, Menu, X, Settings, HelpCircle } from "lucide-react";
import { useAuth } from "@/contexts/AuthContext";
import { useAuthModal } from "@/contexts/AuthModalContext";

type Theme = "light" | "dark";

export default function MobileHeader() {
  const [theme, setTheme] = useState<Theme>("light");
  const [menuOpen, setMenuOpen] = useState(false);
  const menuRef = useRef<HTMLDivElement>(null);
  const { user } = useAuth();
  const { openLoginModal } = useAuthModal();

  useEffect(() => {
    const saved = (localStorage.getItem("alexandria-theme") as Theme) ?? "light";
    applyTheme(saved);
    setTheme(saved);
  }, []);

  useEffect(() => {
    if (!menuOpen) return;
    function handleClick(e: MouseEvent) {
      if (menuRef.current && !menuRef.current.contains(e.target as Node)) {
        setMenuOpen(false);
      }
    }
    document.addEventListener("mousedown", handleClick);
    return () => document.removeEventListener("mousedown", handleClick);
  }, [menuOpen]);

  function applyTheme(t: Theme) {
    document.documentElement.classList.toggle("dark", t === "dark");
    localStorage.setItem("alexandria-theme", t);
  }

  function switchTheme(t: Theme) {
    setTheme(t);
    applyTheme(t);
  }

  return (
    <header className="sticky top-0 z-30 backdrop-blur-sm bg-cream/95 border-b border-cream-active md:hidden">
      <div className="flex items-center justify-between px-4 py-3">
        {/* Switcher de tema */}
        <div className="flex items-center gap-0.5 p-1 bg-cream-dark rounded-xl border border-cream-border">
          <button
            onClick={() => switchTheme("light")}
            title="Modo claro"
            className={`p-1.5 rounded-lg transition-colors ${
              theme === "light"
                ? "bg-cream text-brown shadow-sm"
                : "text-slate hover:bg-cream-active"
            }`}
          >
            <Sun size={14} />
          </button>

          <button
            onClick={() => switchTheme("dark")}
            title="Modo escuro"
            className={`p-1.5 rounded-lg transition-colors ${
              theme === "dark"
                ? "bg-cream text-brown shadow-sm"
                : "text-slate hover:bg-cream-active"
            }`}
          >
            <Moon size={14} />
          </button>
        </div>

        {/* Direita: Entrar + hambúrguer */}
        <div className="flex items-center gap-3">
          {!user && (
            <button
              onClick={openLoginModal}
              className="text-sm font-semibold text-brown hover:text-terra transition-colors"
            >
              Entrar
            </button>
          )}

          {/* Hambúrguer */}
          <div className="relative" ref={menuRef}>
            <button
              onClick={() => setMenuOpen((v) => !v)}
              className="p-1.5 rounded-lg text-slate hover:bg-cream-active transition-colors"
            >
              {menuOpen ? <X size={18} /> : <Menu size={18} />}
            </button>

            {menuOpen && (
              <div className="absolute right-0 top-full mt-2 w-44 bg-cream border border-cream-border rounded-xl shadow-lg overflow-hidden z-50">
                {user && (
                  <Link
                    href="/configuracoes"
                    onClick={() => setMenuOpen(false)}
                    className="flex items-center gap-3 px-4 py-3 text-sm text-brown hover:bg-cream-active transition-colors"
                  >
                    <Settings size={15} className="text-slate" />
                    Configurações
                  </Link>
                )}
                <Link
                  href="/suporte"
                  onClick={() => setMenuOpen(false)}
                  className={`flex items-center gap-3 px-4 py-3 text-sm text-brown hover:bg-cream-active transition-colors ${user ? "border-t border-cream-border" : ""}`}
                >
                  <HelpCircle size={15} className="text-slate" />
                  Suporte
                </Link>
              </div>
            )}
          </div>
        </div>
      </div>
    </header>
  );
}
