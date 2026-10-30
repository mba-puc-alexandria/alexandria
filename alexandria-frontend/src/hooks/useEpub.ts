import { useState, useEffect } from "react";
import { getCachedEpub, saveEpubToCache, markBookAsAccessed } from "@/lib/epub-cache";
import { libraryAccountId } from "@/lib/library-events";

interface UseEpubResult {
  epubData: ArrayBuffer | null;
  loading: boolean;
  error: Error | null;
}

interface EpubState extends UseEpubResult {
  ownerId: number | null;
  bookId: string;
}

export function useEpub(bookId: string, ownerId: number | null): UseEpubResult {
  const [state, setState] = useState<EpubState>({ ownerId: null, bookId, epubData: null, loading: true, error: null });
  const cacheKey = ownerId === null ? null : `${ownerId}:${bookId}`;

  useEffect(() => {
    let cancelled = false;
    const controller = new AbortController();
    setState({ ownerId, bookId, epubData: null, loading: true, error: null });

    async function load() {
      try {
        if (ownerId === null || cacheKey === null || libraryAccountId() !== ownerId) {
          throw new Error("subscription_required");
        }
        const cached = await getCachedEpub(cacheKey);
        if (cancelled || libraryAccountId() !== ownerId) return;

        if (cached && !navigator.onLine) {
          setState({ ownerId, bookId, epubData: cached, loading: false, error: null });
          void markBookAsAccessed(cacheKey);
          return;
        }

        const url = `/api/epub?bookId=${encodeURIComponent(bookId)}`;
        // Cached bytes are never shown until the server rechecks this account's access.
        let res: Response;
        try {
          res = await fetch(url, { method: cached ? "HEAD" : "GET", cache: "no-store", signal: controller.signal });
        } catch (error) {
          // A transport failure can happen while navigator.onLine still reports true.
          if (cached && error instanceof TypeError && !cancelled && !controller.signal.aborted && libraryAccountId() === ownerId) {
            setState({ ownerId, bookId, epubData: cached, loading: false, error: null });
            void markBookAsAccessed(cacheKey);
            return;
          }
          throw error;
        }
        if (res.status === 402 || res.status === 403 || res.status === 401) {
          throw new Error("subscription_required");
        }
        if (!res.ok) throw new Error(`Falha ao baixar EPUB: ${res.status}`);
        if (res.headers.get("X-Authenticated-User-Id") !== String(ownerId)) {
          throw new Error("A sessão mudou durante a leitura. Recarregue o livro.");
        }
        if (cancelled || libraryAccountId() !== ownerId) return;
        if (cached) {
          setState({ ownerId, bookId, epubData: cached, loading: false, error: null });
          void markBookAsAccessed(cacheKey);
          return;
        }

        const data = await res.arrayBuffer();
        if (cancelled || libraryAccountId() !== ownerId) return;
        setState({ ownerId, bookId, epubData: data, loading: false, error: null });
        void saveEpubToCache(cacheKey, data);
      } catch (err) {
        if (!cancelled) setState({ ownerId, bookId, epubData: null, loading: false, error: err instanceof Error ? err : new Error(String(err)) });
      }
    }

    void load();
    return () => { cancelled = true; controller.abort(); };
  }, [bookId, ownerId, cacheKey]);

  const owned = (ownerId === null || libraryAccountId() === ownerId) && state.ownerId === ownerId && state.bookId === bookId;
  const epubData = owned ? state.epubData : null;
  useEffect(() => {
    if (!epubData || cacheKey === null) return;

    const heartbeatMs = 2 * 60 * 1000;
    const interval = setInterval(() => {
      void markBookAsAccessed(cacheKey);
    }, heartbeatMs);

    return () => {
      clearInterval(interval);
      void markBookAsAccessed(cacheKey);
    };
  }, [cacheKey, epubData]);

  return { epubData, loading: !owned || state.loading, error: owned ? state.error : null };
}
