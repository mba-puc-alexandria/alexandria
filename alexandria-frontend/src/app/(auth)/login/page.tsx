'use client';

import { useState, FormEvent, Suspense, useEffect } from 'react';
import Link from 'next/link';
import { useSearchParams, useRouter } from 'next/navigation';
import { useAuth } from '@/contexts/AuthContext';

function LoginForm() {
  const { login, user, isLoading } = useAuth();
  const searchParams = useSearchParams();
  const router = useRouter();

  useEffect(() => {
    if (!isLoading && user) {
      router.replace('/explorar');
    }
  }, [user, isLoading, router]);
  const justRegistered = searchParams.get('registered') === '1';

  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      await login({ username, password });
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Erro ao fazer login');
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="w-full max-w-md">
      {/* Logo / Brand */}
      <div className="mb-10 text-center">
        <h1 className="font-serif font-bold text-brown text-5xl tracking-tight">Alexandria</h1>
        <p className="text-brown-soft text-sm mt-2">Sua biblioteca digital pessoal</p>
      </div>

      <div className="bg-cream-dark rounded-2xl p-8 shadow-sm border border-cream-border">
        <h2 className="font-brand font-bold text-brown text-2xl mb-6">Entrar</h2>

        {justRegistered && (
          <p className="text-sm bg-brown/10 text-brown rounded-lg px-4 py-3 mb-5">
            Conta criada com sucesso! Faça login para continuar.
          </p>
        )}

        <form onSubmit={handleSubmit} className="flex flex-col gap-5">
          <div className="flex flex-col gap-1.5">
            <label htmlFor="username" className="text-xs font-bold text-brown-soft uppercase tracking-widest">
              Usuário
            </label>
            <input
              id="username"
              type="text"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              placeholder="Seu nome de usuário"
              required
              autoComplete="username"
              className="bg-cream border border-cream-border rounded-lg px-4 py-3 text-brown placeholder:text-brown-soft/40 outline-none focus:border-brown transition-colors"
            />
          </div>

          <div className="flex flex-col gap-1.5">
            <label htmlFor="password" className="text-xs font-bold text-brown-soft uppercase tracking-widest">
              Senha
            </label>
            <input
              id="password"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="Sua senha"
              required
              autoComplete="current-password"
              className="bg-cream border border-cream-border rounded-lg px-4 py-3 text-brown placeholder:text-brown-soft/40 outline-none focus:border-brown transition-colors"
            />
          </div>

          {error && (
            <p className="text-terra text-sm bg-terra/10 rounded-lg px-4 py-3">{error}</p>
          )}

          <button
            type="submit"
            disabled={loading}
            className="bg-brown text-cream font-bold text-sm tracking-widest uppercase px-6 py-4 rounded-xl hover:bg-brown/90 transition-colors disabled:opacity-50 disabled:cursor-not-allowed mt-1"
          >
            {loading ? 'Entrando...' : 'Entrar'}
          </button>
        </form>

        <p className="text-center text-brown-soft text-sm mt-6">
          Não tem conta?{' '}
          <Link href="/registrar" className="text-terra font-bold hover:underline">
            Criar conta
          </Link>
        </p>
      </div>

      <p className="text-center text-brown-soft/60 text-xs mt-6">
        Alexandria — Biblioteca Digital · PUC-SP
      </p>
    </div>
  );
}

export default function LoginPage() {
  return (
    <Suspense>
      <LoginForm />
    </Suspense>
  );
}
