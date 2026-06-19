'use client';

import { createContext, useContext, useState, useEffect, useMemo, ReactNode } from 'react';
import { useRouter } from 'next/navigation';
import { login as apiLogin, loginWithGoogle as apiLoginWithGoogle, LoginRequest, LoginResponse } from '@/lib/api';

interface AuthUser {
  userId: number;
  username: string;
}

interface AuthContextType {
  user: AuthUser | null;
  login: (data: LoginRequest) => Promise<void>;
  loginWithGoogle: (credential: string) => Promise<void>;
  logout: () => void;
  updateUsername: (username: string) => void;
  isLoading: boolean;
}

const AuthContext = createContext<AuthContextType | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const router = useRouter();

  useEffect(() => {
    const stored = localStorage.getItem('auth-user');
    if (stored) {
      setUser(JSON.parse(stored));
    }
    setIsLoading(false);
  }, []);

  function persistSession(response: LoginResponse) {
    const authUser: AuthUser = { userId: response.userId, username: response.username };
    localStorage.setItem('auth-token', response.token);
    localStorage.setItem('auth-user', JSON.stringify(authUser));
    document.cookie = `auth-token=${response.token}; path=/; max-age=${60 * 60 * 24}; SameSite=Lax`;
    setUser(authUser);
    router.push('/explorar');
    router.refresh();
  }

  async function login(data: LoginRequest) {
    const response = await apiLogin(data);
    persistSession(response);
  }

  async function loginWithGoogle(credential: string) {
    const response = await apiLoginWithGoogle(credential);
    persistSession(response);
  }

  function logout() {
    localStorage.removeItem('auth-token');
    localStorage.removeItem('auth-user');
    document.cookie = 'auth-token=; path=/; max-age=0';
    setUser(null);
    router.push('/explorar');
    router.refresh();
  }

  function updateUsername(username: string) {
    setUser((prev) => {
      if (!prev) return prev;
      const updated = { ...prev, username };
      localStorage.setItem('auth-user', JSON.stringify(updated));
      return updated;
    });
  }

  const value = useMemo(
    () => ({ user, login, loginWithGoogle, logout, updateUsername, isLoading }),
    [user, isLoading]
  );

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}
