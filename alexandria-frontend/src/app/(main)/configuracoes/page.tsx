"use client";

import { useState, useEffect, FormEvent } from "react";
import { User, Bell, Moon, Lock, Loader2 } from "lucide-react";
import { useAuth } from "@/contexts/AuthContext";
import { getProfile, updateProfile, updatePassword, type ProfileResponse } from "@/lib/api";

export default function ConfiguracoesPage() {
  const { updateUsername } = useAuth();

  const [profile, setProfile] = useState<ProfileResponse | null>(null);
  const [loading, setLoading] = useState(true);

  const [firstName, setFirstName] = useState("");
  const [lastName, setLastName] = useState("");
  const [username, setUsername] = useState("");
  const [profileError, setProfileError] = useState<string | null>(null);
  const [profileSuccess, setProfileSuccess] = useState(false);
  const [savingProfile, setSavingProfile] = useState(false);

  const [currentPassword, setCurrentPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [passwordError, setPasswordError] = useState<string | null>(null);
  const [passwordSuccess, setPasswordSuccess] = useState(false);
  const [savingPassword, setSavingPassword] = useState(false);

  useEffect(() => {
    getProfile()
      .then((data) => {
        setProfile(data);
        setFirstName(data.firstName);
        setLastName(data.lastName);
        setUsername(data.username);
      })
      .catch(() => setProfileError("Não foi possível carregar seus dados."))
      .finally(() => setLoading(false));
  }, []);

  async function handleProfileSubmit(e: FormEvent) {
    e.preventDefault();
    setProfileError(null);
    setProfileSuccess(false);

    if (!firstName.trim() || !lastName.trim() || !username.trim()) {
      setProfileError("Nome, sobrenome e nome de usuário são obrigatórios.");
      return;
    }

    setSavingProfile(true);
    try {
      const updated = await updateProfile({
        username: username.trim(),
        firstName: firstName.trim(),
        lastName: lastName.trim(),
      });
      setProfile(updated);
      updateUsername(updated.username);
      setProfileSuccess(true);
    } catch (err) {
      setProfileError(err instanceof Error ? err.message : "Falha ao atualizar perfil.");
    } finally {
      setSavingProfile(false);
    }
  }

  async function handlePasswordSubmit(e: FormEvent) {
    e.preventDefault();
    setPasswordError(null);
    setPasswordSuccess(false);

    if (!currentPassword || !newPassword || !confirmPassword) {
      setPasswordError("Preencha todos os campos de senha.");
      return;
    }
    if (newPassword.length < 8) {
      setPasswordError("A nova senha deve ter pelo menos 8 caracteres.");
      return;
    }
    if (newPassword !== confirmPassword) {
      setPasswordError("As senhas não coincidem.");
      return;
    }

    setSavingPassword(true);
    try {
      await updatePassword({ currentPassword, newPassword });
      setPasswordSuccess(true);
      setCurrentPassword("");
      setNewPassword("");
      setConfirmPassword("");
    } catch (err) {
      setPasswordError(err instanceof Error ? err.message : "Falha ao atualizar senha.");
    } finally {
      setSavingPassword(false);
    }
  }

  return (
    <div className="px-6 md:px-8 pt-8 md:pt-12 pb-8 flex flex-col gap-8 max-w-2xl">
      <div>
        <h1 className="font-serif font-bold text-brown text-2xl">Configurações</h1>
        <p className="text-slate text-sm mt-1">Gerencie sua conta e preferências</p>
      </div>

      {/* Conta */}
      <section className="flex flex-col gap-3">
        <div className="flex items-center gap-2 mb-1">
          <User size={14} className="text-terra" />
          <span className="text-brown text-xs font-bold uppercase tracking-widest">Conta</span>
        </div>

        <div className="bg-cream-dark rounded-xl p-5">
          {loading ? (
            <div className="flex items-center gap-2 text-slate/60 text-sm">
              <Loader2 size={16} className="animate-spin" />
              Carregando...
            </div>
          ) : (
            <form onSubmit={handleProfileSubmit} className="flex flex-col gap-4">
              <div className="flex flex-col gap-1">
                <span className="text-xs font-bold text-slate uppercase tracking-widest">E-mail</span>
                <span className="text-brown/70 font-medium">{profile?.email ?? "—"}</span>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <label className="flex flex-col gap-1">
                  <span className="text-xs font-bold text-slate uppercase tracking-widest">Nome</span>
                  <input
                    type="text"
                    value={firstName}
                    onChange={(e) => setFirstName(e.target.value)}
                    className="bg-cream rounded-lg px-4 py-2.5 text-brown outline-none border border-cream-border focus:border-terra"
                  />
                </label>
                <label className="flex flex-col gap-1">
                  <span className="text-xs font-bold text-slate uppercase tracking-widest">Sobrenome</span>
                  <input
                    type="text"
                    value={lastName}
                    onChange={(e) => setLastName(e.target.value)}
                    className="bg-cream rounded-lg px-4 py-2.5 text-brown outline-none border border-cream-border focus:border-terra"
                  />
                </label>
              </div>

              <label className="flex flex-col gap-1">
                <span className="text-xs font-bold text-slate uppercase tracking-widest">Nome de usuário</span>
                <input
                  type="text"
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  className="bg-cream rounded-lg px-4 py-2.5 text-brown outline-none border border-cream-border focus:border-terra"
                />
              </label>

              {profileError && <p className="text-red-600 text-sm">{profileError}</p>}
              {profileSuccess && <p className="text-green-700 text-sm">Perfil atualizado com sucesso.</p>}

              <button
                type="submit"
                disabled={savingProfile}
                className="self-start bg-brown text-cream px-5 py-2.5 rounded-lg text-sm font-medium hover:bg-brown/90 transition-colors disabled:opacity-50"
              >
                {savingProfile ? "Salvando..." : "Salvar alterações"}
              </button>
            </form>
          )}
        </div>
      </section>

      {/* Senha */}
      <section className="flex flex-col gap-3">
        <div className="flex items-center gap-2 mb-1">
          <Lock size={14} className="text-terra" />
          <span className="text-brown text-xs font-bold uppercase tracking-widest">Senha</span>
        </div>

        <div className="bg-cream-dark rounded-xl p-5">
          <form onSubmit={handlePasswordSubmit} className="flex flex-col gap-4">
            <label className="flex flex-col gap-1">
              <span className="text-xs font-bold text-slate uppercase tracking-widest">Senha atual</span>
              <input
                type="password"
                value={currentPassword}
                onChange={(e) => setCurrentPassword(e.target.value)}
                className="bg-cream rounded-lg px-4 py-2.5 text-brown outline-none border border-cream-border focus:border-terra"
              />
            </label>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <label className="flex flex-col gap-1">
                <span className="text-xs font-bold text-slate uppercase tracking-widest">Nova senha</span>
                <input
                  type="password"
                  value={newPassword}
                  onChange={(e) => setNewPassword(e.target.value)}
                  className="bg-cream rounded-lg px-4 py-2.5 text-brown outline-none border border-cream-border focus:border-terra"
                />
              </label>
              <label className="flex flex-col gap-1">
                <span className="text-xs font-bold text-slate uppercase tracking-widest">Confirmar nova senha</span>
                <input
                  type="password"
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                  className="bg-cream rounded-lg px-4 py-2.5 text-brown outline-none border border-cream-border focus:border-terra"
                />
              </label>
            </div>

            <p className="text-slate/60 text-xs">A senha deve ter no mínimo 8 caracteres.</p>

            {passwordError && <p className="text-red-600 text-sm">{passwordError}</p>}
            {passwordSuccess && <p className="text-green-700 text-sm">Senha atualizada com sucesso.</p>}

            <button
              type="submit"
              disabled={savingPassword}
              className="self-start bg-brown text-cream px-5 py-2.5 rounded-lg text-sm font-medium hover:bg-brown/90 transition-colors disabled:opacity-50"
            >
              {savingPassword ? "Salvando..." : "Alterar senha"}
            </button>
          </form>
        </div>
      </section>

      {/* Notificações */}
      <section className="flex flex-col gap-3">
        <div className="flex items-center gap-2 mb-1">
          <Bell size={14} className="text-terra" />
          <span className="text-brown text-xs font-bold uppercase tracking-widest">Notificações</span>
        </div>

        <div className="bg-cream-dark rounded-xl p-5">
          <p className="text-slate/60 text-xs">Em breve.</p>
        </div>
      </section>

      {/* Aparência */}
      <section className="flex flex-col gap-3">
        <div className="flex items-center gap-2 mb-1">
          <Moon size={14} className="text-terra" />
          <span className="text-brown text-xs font-bold uppercase tracking-widest">Aparência</span>
        </div>

        <div className="bg-cream-dark rounded-xl p-5">
          <p className="text-slate/60 text-xs">Em breve.</p>
        </div>
      </section>
    </div>
  );
}
