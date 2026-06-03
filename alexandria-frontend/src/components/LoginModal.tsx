"use client";

import { useState, useEffect, FormEvent } from "react";
import Link from "next/link";
import { X } from "lucide-react";
import { useAuth } from "@/contexts/AuthContext";
import { useAuthModal } from "@/contexts/AuthModalContext";

export default function LoginModal() {
  const { isOpen, closeLoginModal } = useAuthModal();
  const { login } = useAuth();

  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  // Fecha com Escape
  useEffect(() => {
    if (!isOpen) return;
    function onKey(e: KeyboardEvent) {
      if (e.key === "Escape") closeLoginModal();
    }
    document.addEventListener("keydown", onKey);
    return () => document.removeEventListener("keydown", onKey);
  }, [isOpen, closeLoginModal]);

  // Bloqueia scroll do body enquanto aberto
  useEffect(() => {
    document.body.style.overflow = isOpen ? "hidden" : "";
    return () => { document.body.style.overflow = ""; };
  }, [isOpen]);

  if (!isOpen) return null;

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setError("");
    setLoading(true);
    try {
      await login({ username, password });
      closeLoginModal();
      setUsername("");
      setPassword("");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Usuário ou senha incorretos");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div
      className="modal-overlay fixed inset-0 z-50 flex items-center justify-center"
      onClick={closeLoginModal}
    >
      {/* Overlay */}
      <div className="absolute inset-0 bg-brown/60 backdrop-blur-sm" />

      {/* Card */}
      <div
        className="modal-card relative z-10 w-full max-w-sm mx-4 bg-cream rounded-2xl shadow-xl border border-cream-border p-8 flex flex-col gap-6"
        onClick={(e) => e.stopPropagation()}
      >
        {/* Fechar */}
        <button
          onClick={closeLoginModal}
          className="absolute top-4 right-4 text-slate hover:text-brown transition-colors"
        >
          <X size={18} />
        </button>

        {/* Título */}
        <div className="flex flex-col gap-1">
          <h2 className="font-brand font-bold text-brown text-2xl">Entrar</h2>
          <p className="text-brown-soft text-sm">Acesse sua biblioteca pessoal</p>
        </div>

        {/* Formulário */}
        <form onSubmit={handleSubmit} className="flex flex-col gap-4">
          <div className="flex flex-col gap-1.5">
            <label className="text-xs font-bold text-brown-soft uppercase tracking-widest">
              Usuário
            </label>
            <input
              type="text"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              placeholder="Seu nome de usuário"
              required
              autoComplete="username"
              className="bg-cream-dark border border-cream-border rounded-xl px-4 py-3 text-sm text-brown placeholder:text-brown-soft/40 outline-none focus:border-brown-soft transition-colors"
            />
          </div>

          <div className="flex flex-col gap-1.5">
            <label className="text-xs font-bold text-brown-soft uppercase tracking-widest">
              Senha
            </label>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="Sua senha"
              required
              autoComplete="current-password"
              className="bg-cream-dark border border-cream-border rounded-xl px-4 py-3 text-sm text-brown placeholder:text-brown-soft/40 outline-none focus:border-brown-soft transition-colors"
            />
          </div>

          {error && (
            <p className="text-terra text-sm bg-terra/10 rounded-xl px-4 py-3">{error}</p>
          )}

          <button
            type="submit"
            disabled={loading}
            className="bg-brown text-cream font-bold text-sm tracking-widest uppercase px-6 py-4 rounded-xl hover:bg-brown/90 transition-colors disabled:opacity-50 disabled:cursor-not-allowed mt-1"
          >
            {loading ? "Entrando..." : "Entrar"}
          </button>
        </form>

        <p className="text-center text-brown-soft text-sm">
          Não tem conta?{" "}
          <Link
            href="/registrar"
            onClick={closeLoginModal}
            className="text-terra font-bold hover:underline"
          >
            Criar conta
          </Link>
        </p>
      </div>
    </div>
  );
}
