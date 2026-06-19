import { useState, useEffect } from "react";
import { getCachedEpub, saveEpubToCache, markBookAsAccessed } from "@/lib/epub-cache";

interface UseEpubResult {
  epubData: ArrayBuffer | null;
  loading: boolean;
  error: Error | null;
}

export function useEpub(bookId: string, downloadUrl: string | null): UseEpubResult {
  const [epubData, setEpubData] = useState<ArrayBuffer | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<Error | null>(null);

  useEffect(() => {
    if (!downloadUrl) {
      setLoading(false);
      return;
    }

    setLoading(true);

    let cancelled = false;

    async function load() {
      try {
        const cached = await getCachedEpub(bookId);
        if (cancelled) return;

        if (cached) {
          setEpubData(cached);
          markBookAsAccessed(bookId);
          setLoading(false);
          return;
        }

        const res = await fetch(`/api/epub?url=${encodeURIComponent(downloadUrl!)}`);
        if (!res.ok) throw new Error(`Falha ao baixar EPUB: ${res.status}`);

        const data = await res.arrayBuffer();
        if (cancelled) return;

        setEpubData(data);
        saveEpubToCache(bookId, data);
      } catch (err) {
        if (!cancelled) setError(err instanceof Error ? err : new Error(String(err)));
      } finally {
        if (!cancelled) setLoading(false);
      }
    }

    load();

    return () => { cancelled = true; };
  }, [bookId, downloadUrl]);

  useEffect(() => {
    if (!epubData) return;

    const heartbeatMs = 2 * 60 * 1000;
    const interval = setInterval(() => {
      markBookAsAccessed(bookId);
    }, heartbeatMs);

    return () => {
      clearInterval(interval);
      markBookAsAccessed(bookId);
    };
  }, [bookId, epubData]);

  return { epubData, loading, error };
}
