'use client';

import {
  createContext,
  useContext,
  useMemo,
  useCallback,
  useEffect,
  useState,
  useRef,
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
  const externalChange = (event: StorageEvent) => {
    if (event.key === AUTH_USER_KEY || event.key === AUTH_TOKEN_KEY || event.key === null) callback();
  };
  window.addEventListener('storage', externalChange);
  return () => {
    listeners.delete(callback);
    window.removeEventListener('storage', externalChange);
  };
}

function emitChange() {
  listeners.forEach((listener) => listener());
}

function getSnapshot(): string | null {
  const rawUser = localStorage.getItem(AUTH_USER_KEY);
  const token = localStorage.getItem(AUTH_TOKEN_KEY);
  return rawUser || token ? JSON.stringify([rawUser, token]) : null;
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

function subscriptionOwner(user: AuthUser | null, token: string | null): string | null {
  return user && token ? `${user.userId}:${token}` : null;
}

function currentSubscriptionOwner(): string | null {
  return subscriptionOwner(readUser(localStorage.getItem(AUTH_USER_KEY)), localStorage.getItem(AUTH_TOKEN_KEY));
}

function writeUser(authUser: AuthUser | null) {
  if (authUser) {
    localStorage.setItem(AUTH_USER_KEY, JSON.stringify(authUser));
  } else {
    localStorage.removeItem(AUTH_USER_KEY);
  }
  emitChange();
}

export function AuthProvider({ children }: Readonly<{ children: ReactNode }>) {
  const raw = useSyncExternalStore(subscribe, getSnapshot, getServerSnapshot);
  const [rawUser, token] = useMemo(() => raw ? JSON.parse(raw) as [string | null, string | null] : [null, null], [raw]);
  const user = useMemo(() => readUser(rawUser), [rawUser]);
  const sessionOwner = subscriptionOwner(user, token);
  const [subscriptionState, setSubscriptionState] = useState<{ owner: string | null; data: Subscription | null; loading: boolean }>({ owner: null, data: null, loading: true });
  const subscriptionVersion = useRef(0);
  // Inicia true: no primeiro frame ainda não sabemos se há assinatura, e quem
  // consome isLoading não pode assumir "sem plano" antes da resposta chegar.
  const subscription = subscriptionState.owner === sessionOwner ? subscriptionState.data : null;
  const loadingSubscription = subscriptionState.owner !== sessionOwner || subscriptionState.loading;

  const router = useRouter();

  const refreshSubscription = useCallback(async () => {
    const owner = currentSubscriptionOwner();
    const version = ++subscriptionVersion.current;
    if (!owner) {
      setSubscriptionState({ owner: null, data: null, loading: false });
      return;
    }
    setSubscriptionState(previous => ({ owner, data: previous.owner === owner ? previous.data : null, loading: true }));
    try {
      const data = await apiGetSubscription();
      if (subscriptionVersion.current === version && currentSubscriptionOwner() === owner) {
        setSubscriptionState({ owner, data, loading: false });
      }
    } catch {
      if (subscriptionVersion.current === version && currentSubscriptionOwner() === owner) {
        setSubscriptionState({ owner, data: null, loading: false });
      }
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
    subscriptionVersion.current++;
    setSubscriptionState({ owner: null, data: null, loading: false });
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
    void refreshSubscription();
  }, [sessionOwner, refreshSubscription]);

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
