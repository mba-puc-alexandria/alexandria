"use client";

import { useState, useEffect } from "react";
import { Sun, Moon, Crown, LogOut } from "lucide-react";
import { useAuth } from "@/contexts/AuthContext";
import { useAuthModal } from "@/contexts/AuthModalContext";

type Theme = "light" | "dark";

export default function Header() {
  const [theme, setTheme] = useState<Theme>("light");
  const { user, logout } = useAuth();
  const { openLoginModal } = useAuthModal();

  useEffect(() => {
    const saved = (localStorage.getItem("alexandria-theme") as Theme) ?? "light";
    applyTheme(saved);
    setTheme(saved);
  }, []);

  function applyTheme(t: Theme) {
    document.documentElement.classList.toggle("dark", t === "dark");
    localStorage.setItem("alexandria-theme", t);
  }

  function switchTheme(t: Theme) {
    setTheme(t);
    applyTheme(t);
  }

  return (
    <header className="w-full flex items-center justify-end px-8 py-3 bg-cream border-b border-cream-border shrink-0 z-10 gap-4">
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

        <button
          title="Plano Premium"
          className="p-2 rounded-lg text-terra hover:bg-cream-active transition-colors"
        >
          <Crown size={16} />
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
