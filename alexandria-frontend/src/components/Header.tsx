"use client";

import { useState, useEffect, useCallback } from "react";
import Link from "next/link";
import { Sun, Moon, LogOut, Sparkles } from "lucide-react";
import { useAuth } from "@/contexts/AuthContext";
import { useAuthModal } from "@/contexts/AuthModalContext";

type Theme = "light" | "dark";
const THEME_KEY = "alexandria-theme";

function getInitialTheme(): Theme {
  if (typeof window === "undefined") return "light";
  return (localStorage.getItem(THEME_KEY) as Theme) ?? "light";
}

export default function Header() {
  const [theme, setTheme] = useState<Theme>(getInitialTheme);
  const { user, logout } = useAuth();
  const { openLoginModal } = useAuthModal();

  const applyTheme = useCallback((t: Theme) => {
    document.documentElement.classList.toggle("dark", t === "dark");
    localStorage.setItem(THEME_KEY, t);
  }, []);

  useEffect(() => {
    applyTheme(theme);
  }, [theme, applyTheme]);

  function switchTheme(t: Theme) {
    setTheme(t);
  }

  return (
    <header className="w-full flex items-center justify-end px-8 py-3 bg-cream border-b border-cream-border shrink-0 z-10 gap-4">
      {/* Link Planos */}
      <Link
        href="/planos"
        className="hidden md:flex items-center gap-2 text-brown-soft text-sm font-semibold hover:text-brown transition-colors"
      >
        <Sparkles size={16} className="text-terra" />
        Planos
      </Link>

      {/* Switcher de tema */}
      <div className="flex items-center gap-1 p-1 bg-cream-dark rounded-xl border border-cream-border">
        <button
          onClick={() => switchTheme("light")}
          title="Modo claro"
          className={`p-2 rounded-lg transition-colors ${
            theme === "light"
              ? "bg-cream text-brown shadow-sm"
              : "text-slate hover:bg-cream-active"
          }`}
        >
          <Sun size={16} />
        </button>
        <button
          onClick={() => switchTheme("dark")}
          title="Modo escuro"
          className={`p-2 rounded-lg transition-colors ${
            theme === "dark"
              ? "bg-cream text-brown shadow-sm"
              : "text-slate hover:bg-cream-active"
          }`}
        >
          <Moon size={16} />
        </button>
      </div>

      {/* Auth */}
      {user ? (
        <div className="flex items-center gap-3">
          <span className="text-brown-soft text-sm font-medium">{user.username}</span>
          <button
            onClick={logout}
            title="Sair"
            className="p-2 rounded-lg text-slate hover:bg-cream-active transition-colors"
          >
            <LogOut size={16} />
          </button>
        </div>
      ) : (
        <button
          onClick={openLoginModal}
          className="text-sm font-semibold text-brown hover:text-terra transition-colors"
        >
          Entrar
        </button>
      )}
    </header>
  );
}
