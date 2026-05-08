'use client';

import { useState, FormEvent } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { register } from '@/lib/api';

export default function RegistrarPage() {
  const router = useRouter();
  const [form, setForm] = useState({
    username: '',
    firstName: '',
    lastName: '',
    email: '',
    password: '',
    confirmPassword: '',
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  function handleChange(e: React.ChangeEvent<HTMLInputElement>) {
    setForm((prev) => ({ ...prev, [e.target.name]: e.target.value }));
  }

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setError('');

    if (form.password !== form.confirmPassword) {
      setError('As senhas não coincidem');
      return;
    }

    setLoading(true);
    try {
      await register({
        username: form.username,
        firstName: form.firstName,
        lastName: form.lastName,
        email: form.email,
        password: form.password,
      });
      router.push('/login?registered=1');
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Erro ao criar conta');
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="w-full max-w-md">
      {/* Brand */}
      <div className="mb-10 text-center">
        <h1 className="font-serif font-bold text-brown text-5xl tracking-tight">Alexandria</h1>
        <p className="text-brown-soft text-sm mt-2">Sua biblioteca digital pessoal</p>
      </div>

      <div className="bg-cream-dark rounded-2xl p-8 shadow-sm border border-cream-border">
        <h2 className="font-brand font-bold text-brown text-2xl mb-6">Criar conta</h2>

        <form onSubmit={handleSubmit} className="flex flex-col gap-4">
          {/* Nome e sobrenome lado a lado */}
          <div className="grid grid-cols-2 gap-3">
            <div className="flex flex-col gap-1.5">
              <label className="text-xs font-bold text-brown-soft uppercase tracking-widest">
                Nome
              </label>
              <input
                name="firstName"
                type="text"
                value={form.firstName}
                onChange={handleChange}
                placeholder="Nome"
                required
                className="bg-cream border border-cream-border rounded-lg px-4 py-3 text-brown placeholder:text-brown-soft/40 outline-none focus:border-brown transition-colors"
              />
            </div>
            <div className="flex flex-col gap-1.5">
              <label className="text-xs font-bold text-brown-soft uppercase tracking-widest">
                Sobrenome
              </label>
              <input
                name="lastName"
                type="text"
                value={form.lastName}
                onChange={handleChange}
                placeholder="Sobrenome"
                required
                className="bg-cream border border-cream-border rounded-lg px-4 py-3 text-brown placeholder:text-brown-soft/40 outline-none focus:border-brown transition-colors"
              />
            </div>
          </div>

          <div className="flex flex-col gap-1.5">
            <label className="text-xs font-bold text-brown-soft uppercase tracking-widest">
              Usuário
            </label>
            <input
              name="username"
              type="text"
              value={form.username}
              onChange={handleChange}
              placeholder="Escolha um nome de usuário"
              required
              autoComplete="username"
              className="bg-cream border border-cream-border rounded-lg px-4 py-3 text-brown placeholder:text-brown-soft/40 outline-none focus:border-brown transition-colors"
            />
          </div>

          <div className="flex flex-col gap-1.5">
            <label className="text-xs font-bold text-brown-soft uppercase tracking-widest">
              E-mail
            </label>
            <input
              name="email"
              type="email"
              value={form.email}
              onChange={handleChange}
              placeholder="seu@email.com"
              required
              autoComplete="email"
              className="bg-cream border border-cream-border rounded-lg px-4 py-3 text-brown placeholder:text-brown-soft/40 outline-none focus:border-brown transition-colors"
            />
          </div>

          <div className="flex flex-col gap-1.5">
            <label className="text-xs font-bold text-brown-soft uppercase tracking-widest">
              Senha
            </label>
            <input
              name="password"
              type="password"
              value={form.password}
              onChange={handleChange}
              placeholder="Mínimo 8 caracteres"
              required
              minLength={8}
              autoComplete="new-password"
              className="bg-cream border border-cream-border rounded-lg px-4 py-3 text-brown placeholder:text-brown-soft/40 outline-none focus:border-brown transition-colors"
            />
          </div>

          <div className="flex flex-col gap-1.5">
            <label className="text-xs font-bold text-brown-soft uppercase tracking-widest">
              Confirmar senha
            </label>
            <input
              name="confirmPassword"
              type="password"
              value={form.confirmPassword}
              onChange={handleChange}
              placeholder="Repita a senha"
              required
              autoComplete="new-password"
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
            {loading ? 'Criando conta...' : 'Criar conta'}
          </button>
        </form>

        <p className="text-center text-brown-soft text-sm mt-6">
          Já tem conta?{' '}
          <Link href="/login" className="text-terra font-bold hover:underline">
            Entrar
          </Link>
        </p>
      </div>

      <p className="text-center text-brown-soft/60 text-xs mt-6">
        Alexandria — Biblioteca Digital · PUC-SP
      </p>
    </div>
  );
}
