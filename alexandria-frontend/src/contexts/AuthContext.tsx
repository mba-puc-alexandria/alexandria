'use client';

import { createContext, useContext, useState, useEffect, useMemo, ReactNode } from 'react';
import { useRouter } from 'next/navigation';
import { login as apiLogin, LoginRequest } from '@/lib/api';

interface AuthUser {
  userId: number;
  username: string;
}

interface AuthContextType {
  user: AuthUser | null;
  login: (data: LoginRequest) => Promise<void>;
  logout: () => void;
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

  async function login(data: LoginRequest) {
    const response = await apiLogin(data);

    const authUser: AuthUser = { userId: response.userId, username: response.username };

    localStorage.setItem('auth-token', response.token);
    localStorage.setItem('auth-user', JSON.stringify(authUser));
    document.cookie = `auth-token=${response.token}; path=/; max-age=${60 * 60 * 24}; SameSite=Lax`;

    setUser(authUser);
    router.push('/explorar');
  }

  function logout() {
    localStorage.removeItem('auth-token');
    localStorage.removeItem('auth-user');
    document.cookie = 'auth-token=; path=/; max-age=0';
    setUser(null);
    router.push('/explorar');
  }

  const value = useMemo(() => ({ user, login, logout, isLoading }), [user, isLoading]);

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
