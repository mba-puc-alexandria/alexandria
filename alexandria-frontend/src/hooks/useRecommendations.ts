'use client';

import { useCallback, useEffect, useRef, useState } from 'react';
import {
  ApiError, getRecommendations, requestRecommendationGeneration,
  type RecommendationResponse, type RecommendationGenerationStatus,
} from '@/lib/api';
import { subscribeLibraryChanges } from '@/lib/library-events';

type Removal = 'added' | 'already_added' | 'unavailable';
interface Failure { message: string; status?: number; generationStatus?: RecommendationGenerationStatus }
interface State {
  owner: number | null;
  data: RecommendationResponse | null;
  loading: boolean;
  refreshing: boolean;
  error: Failure | null;
  notice: string | null;
  cooldownUntil: number;
  now: number;
  pollsExhausted: boolean;
}
const empty = (owner: number | null): State => ({ owner, data: null, loading: owner !== null,
  refreshing: false, error: null, notice: null, cooldownUntil: 0, now: Date.now(), pollsExhausted: false });
interface Actions {
  owner: number | null;
  refresh: () => Promise<void>;
  retry: () => Promise<void>;
  remove: (bookId: number, reason: Removal) => void;
}

export function useRecommendations(userId: number | null, onUnauthorized?: () => void) {
  const [state, setState] = useState<State>(() => empty(userId));
  const unauthorized = useRef(onUnauthorized);
  useEffect(() => { unauthorized.current = onUnauthorized; }, [onUnauthorized]);
  const actions = useRef<Actions>({ owner: null, refresh: async () => {}, retry: async () => {}, remove: () => {} });

  useEffect(() => {
    let disposed = false;
    let version = 0;
    let controller: AbortController | undefined;
    let pollTimer: ReturnType<typeof setTimeout> | undefined;
    let automaticReads = 1;
    let currentData: RecommendationResponse | null = null;
    let currentFailure: Failure | null = null;
    let cooldownUntil = 0;
    const exclusions = new Set<number>();
    const confirmedMutations = new Set<number>();
    const publish = (patch: Partial<State>) => {
      if (!disposed) setState(previous => ({ ...(previous.owner === userId ? previous : empty(userId)), ...patch }));
    };
    const cancelPoll = () => {
      if (pollTimer) clearTimeout(pollTimer);
      pollTimer = undefined;
    };
    const active = (requestVersion: number) => !disposed && version === requestVersion;
    const fail = (error: unknown) => {
      currentFailure = error instanceof ApiError
        ? { message: error.message, status: error.status, generationStatus: error.generationStatus }
        : { message: 'Não foi possível atualizar suas recomendações. Tente verificar novamente.' };
      cooldownUntil = error instanceof ApiError && error.generationStatus === 'FAILED'
        ? Date.now() + (error.retryAfterSeconds ?? 0) * 1_000 : 0;
      if (error instanceof ApiError && error.status === 401) {
        currentData = null;
        exclusions.clear();
        unauthorized.current?.();
      }
      publish({ data: currentData, error: currentFailure, loading: false, refreshing: false, cooldownUntil, now: Date.now() });
    };
    async function load(): Promise<void> {
      if (disposed || userId === null) return;
      cancelPoll();
      controller?.abort();
      controller = new AbortController();
      const requestVersion = ++version;
      publish({ loading: currentData === null, refreshing: true });
      try {
        const response = await getRecommendations(controller.signal);
        if (!active(requestVersion)) return;
        const items = response.data.items.filter(item => !exclusions.has(item.book.id));
        currentData = { ...response.data, items };
        if (currentData.state === 'READY' && !items.length) {
          currentData = { ...currentData, state: 'EMPTY', emptyReason: currentData.selection?.selectedCount === 0 ? 'NO_MATCHES' : 'ALL_ADDED_OR_UNAVAILABLE' };
        }
        // This response began after every confirmed local mutation. Keep its filtered
        // result, then let a later authoritative read reflect explicit removals.
        exclusions.clear();
        currentFailure = null;
        cooldownUntil = currentData.generationStatus === 'FAILED'
          ? Date.now() + (currentData.retryAfterSeconds ?? 0) * 1_000 : 0;
        publish({ data: currentData, error: null, loading: false, refreshing: false, cooldownUntil, now: Date.now(), pollsExhausted: currentData.state === 'GENERATING' && automaticReads >= 10 });
        if (currentData.state === 'GENERATING' && automaticReads < 10) {
          const delay = Math.max(1, response.retryAfterSeconds ?? currentData.retryAfterSeconds ?? 3);
          pollTimer = setTimeout(() => { automaticReads++; void load(); }, delay * 1_000);
        }
      } catch (error) {
        if (active(requestVersion) && !(error instanceof DOMException && error.name === 'AbortError')) fail(error);
      }
    }
    async function retry(): Promise<void> {
      if (disposed || userId === null) return;
      const status = currentFailure ? currentFailure.generationStatus : currentData?.generationStatus;
      if (status !== 'FAILED') return load();
      if (Date.now() < cooldownUntil) return;
      cancelPoll();
      controller?.abort();
      controller = new AbortController();
      const requestVersion = ++version;
      publish({ refreshing: true, error: null });
      try {
        await requestRecommendationGeneration(controller.signal);
        if (active(requestVersion)) await load();
      } catch (error) {
        if (active(requestVersion) && !(error instanceof DOMException && error.name === 'AbortError')) fail(error);
      }
    }
    function remove(bookId: number, reason: Removal) {
      if (disposed || userId === null) return;
      if (confirmedMutations.has(bookId)) return;
      confirmedMutations.add(bookId);
      exclusions.add(bookId);
      controller?.abort();
      version++;
      cancelPoll();
      if (currentData) {
        const items = currentData.items.filter(item => item.book.id !== bookId);
        currentData = { ...currentData, items,
          ...(currentData.state === 'READY' && !items.length ? { state: 'EMPTY' as const, emptyReason: 'ALL_ADDED_OR_UNAVAILABLE' as const } : {}) };
      }
      const notices: Record<Removal, string> = {
        unavailable: 'Este livro não está mais disponível.',
        already_added: 'Este livro já está na sua biblioteca.',
        added: 'Livro adicionado à biblioteca.',
      };
      publish({ data: currentData, notice: notices[reason] });
      void load();
    }
    actions.current = { owner: userId, refresh: load, retry, remove };
    setState(empty(userId));
    if (userId === null) return () => { disposed = true; };
    void load();
    const onFocus = () => { exclusions.clear(); confirmedMutations.clear(); void load(); };
    const onVisibility = () => { if (document.visibilityState === 'visible') onFocus(); };
    window.addEventListener('focus', onFocus);
    document.addEventListener('visibilitychange', onVisibility);
    const unsubscribe = subscribeLibraryChanges(change => {
      if (change.accountId !== userId) return;
      if ((change.kind === 'added' || change.kind === 'already_added') && change.bookId !== undefined) remove(change.bookId, change.kind);
      else { exclusions.clear(); confirmedMutations.clear(); void load(); }
    });
    return () => {
      disposed = true;
      version++;
      controller?.abort();
      cancelPoll();
      unsubscribe();
      window.removeEventListener('focus', onFocus);
      document.removeEventListener('visibilitychange', onVisibility);
    };
  }, [userId]);

  useEffect(() => {
    if (!state.cooldownUntil || state.cooldownUntil <= Date.now()) return;
    const timer = setInterval(() => {
      const now = Date.now();
      setState(previous => ({ ...previous, now }));
      if (now >= state.cooldownUntil) clearInterval(timer);
    }, 250);
    return () => clearInterval(timer);
  }, [state.cooldownUntil]);

  const refresh = useCallback(async () => { if (actions.current.owner === userId) await actions.current.refresh(); }, [userId]);
  const retry = useCallback(async () => { if (actions.current.owner === userId) await actions.current.retry(); }, [userId]);
  const removeConfirmed = useCallback((bookId: number, reason: Removal) => {
    if (actions.current.owner === userId) actions.current.remove(bookId, reason);
  }, [userId]);
  // A different account can never render the previous account's snapshot, even
  // during the frame before the effect cleanup runs.
  const visible = state.owner === userId ? state : empty(userId);
  const remainingSeconds = Math.max(0, Math.ceil((visible.cooldownUntil - visible.now) / 1_000));
  const generationStatus = visible.error ? visible.error.generationStatus : visible.data?.generationStatus;
  return { ...visible, refresh, retry, removeConfirmed, remainingSeconds, generationStatus,
    canRetry: !visible.loading && !visible.refreshing && remainingSeconds === 0 };
}
