'use client';

import {
  createContext,
  useContext,
  useMemo,
  useCallback,
  useSyncExternalStore,
  ReactNode,
} from 'react';
import { useRouter } from 'next/navigation';
import {
  login as apiLogin,
  loginWithGoogle as apiLoginWithGoogle,
  LoginRequest,
  LoginResponse,
} from '@/lib/api';

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

const AUTH_USER_KEY = 'auth-user';
const AUTH_TOKEN_KEY = 'auth-token';

// --- external store baseado em localStorage ---
const listeners = new Set<() => void>();

function subscribe(callback: () => void) {
  listeners.add(callback);
  return () => listeners.delete(callback);
}

function emitChange() {
  listeners.forEach((listener) => listener());
}

function getSnapshot(): string | null {
  return localStorage.getItem(AUTH_USER_KEY);
}

function getServerSnapshot(): string | null {
  return null;
}

function readUser(raw: string | null): AuthUser | null {
  if (!raw) return null;
  try {
    return JSON.parse(raw) as AuthUser;
  } catch {
    return null;
  }
}

function writeUser(authUser: AuthUser | null) {
  if (authUser) {
    localStorage.setItem(AUTH_USER_KEY, JSON.stringify(authUser));
  } else {
    localStorage.removeItem(AUTH_USER_KEY);
  }
  emitChange();
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const raw = useSyncExternalStore(subscribe, getSnapshot, getServerSnapshot);
  const user = useMemo(() => readUser(raw), [raw]);
  const isLoading = false;

  const router = useRouter();

  const persistSession = useCallback(
    (response: LoginResponse) => {
      const authUser: AuthUser = {
        userId: response.userId,
        username: response.username,
      };
      localStorage.setItem(AUTH_TOKEN_KEY, response.token);
      document.cookie = `auth-token=${response.token}; path=/; max-age=${60 * 60 * 24}; SameSite=Lax`;
      writeUser(authUser);
      router.push('/explorar');
      router.refresh();
    },
    [router]
  );

  const login = useCallback(
    async (data: LoginRequest) => {
      const response = await apiLogin(data);
      persistSession(response);
    },
    [persistSession]
  );

  const loginWithGoogle = useCallback(
    async (credential: string) => {
      const response = await apiLoginWithGoogle(credential);
      persistSession(response);
    },
    [persistSession]
  );

  const logout = useCallback(() => {
    localStorage.removeItem(AUTH_TOKEN_KEY);
    document.cookie = 'auth-token=; path=/; max-age=0';
    writeUser(null);
    router.push('/explorar');
    router.refresh();
  }, [router]);

  const updateUsername = useCallback(
    (username: string) => {
      if (!user) return;
      writeUser({ ...user, username });
    },
    [user]
  );

  const value = useMemo(
    () => ({ user, login, loginWithGoogle, logout, updateUsername, isLoading }),
    [user, login, loginWithGoogle, logout, updateUsername, isLoading]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}
