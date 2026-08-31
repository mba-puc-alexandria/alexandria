'use client';

import {
  createContext,
  useContext,
  useMemo,
  useCallback,
  useEffect,
  useState,
  useSyncExternalStore,
  ReactNode,
} from 'react';
import { useRouter } from 'next/navigation';
import {
  login as apiLogin,
  loginWithGoogle as apiLoginWithGoogle,
  getSubscription as apiGetSubscription,
  LoginRequest,
  LoginResponse,
  Subscription,
} from '@/lib/api';

interface AuthUser {
  userId: number;
  username: string;
}

interface AuthContextType {
  user: AuthUser | null;
  subscription: Subscription | null;
  login: (data: LoginRequest) => Promise<void>;
  loginWithGoogle: (credential: string) => Promise<void>;
  logout: () => void;
  updateUsername: (username: string) => void;
  refreshSubscription: () => Promise<void>;
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
  const [subscription, setSubscription] = useState<Subscription | null>(null);
  const [loadingSubscription, setLoadingSubscription] = useState(false);

  const router = useRouter();

  const refreshSubscription = useCallback(async () => {
    const token = typeof window !== 'undefined' ? localStorage.getItem(AUTH_TOKEN_KEY) : null;
    if (!token) {
      setSubscription(null);
      return;
    }
    setLoadingSubscription(true);
    try {
      const data = await apiGetSubscription();
      setSubscription(data);
    } catch {
      setSubscription(null);
    } finally {
      setLoadingSubscription(false);
    }
  }, []);

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
      await refreshSubscription();
    },
    [persistSession, refreshSubscription]
  );

  const loginWithGoogle = useCallback(
    async (credential: string) => {
      const response = await apiLoginWithGoogle(credential);
      persistSession(response);
      await refreshSubscription();
    },
    [persistSession, refreshSubscription]
  );

  const logout = useCallback(() => {
    localStorage.removeItem(AUTH_TOKEN_KEY);
    document.cookie = 'auth-token=; path=/; max-age=0';
    writeUser(null);
    setSubscription(null);
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

  // Carrega a assinatura quando o usuário já está autenticado (ex.: refresh da página)
  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect -- fetch inicial da assinatura após montagem
    void refreshSubscription();
  }, [user, refreshSubscription]);

  const value = useMemo(
    () => ({
      user,
      subscription,
      login,
      loginWithGoogle,
      logout,
      updateUsername,
      refreshSubscription,
      isLoading: loadingSubscription,
    }),
    [user, subscription, login, loginWithGoogle, logout, updateUsername, refreshSubscription, loadingSubscription]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}
