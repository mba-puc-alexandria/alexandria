"use client";

import { use, useEffect, useState, useCallback, useRef } from "react";
import dynamic from "next/dynamic";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { ArrowLeft } from "lucide-react";
import { getBookById, getUserBooks, updateUserBook, getAuthorDisplay, type BookApiResponse } from "@/lib/api";

const ReactReader = dynamic(
  () => import("react-reader").then((m) => m.ReactReader),
  { ssr: false }
);

// ~250 WPM, ~320 palavras por location (1600 chars / 5)
const MINUTES_PER_LOCATION = 320 / 250;

function formatMinutes(minutes: number): string {
  if (minutes < 1) return "< 1 min";
  if (minutes < 60) return `~${minutes} min`;
  const h = Math.floor(minutes / 60);
  const m = minutes % 60;
  return m > 0 ? `~${h}h ${m}min` : `~${h}h`;
}

type DisplayMode = 'percent' | 'minutes' | 'pages';

export default function LeitorPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = use(params);
  const router = useRouter();
  const [book, setBook] = useState<BookApiResponse | null>(null);
  const [epubData, setEpubData] = useState<ArrayBuffer | null>(null);
  const [loading, setLoading] = useState(true);
  const [location, setLocation] = useState<string | number>(0);
  const [progress, setProgress] = useState<number>(0);
  const [minutesLeft, setMinutesLeft] = useState<number | null>(null);
  const [pagesLeft, setPagesLeft] = useState<number | null>(null);
  const [displayMode, setDisplayMode] = useState<DisplayMode>('percent');

  const userBookIdRef = useRef<number | null>(null);
  const lastSavedProgressRef = useRef<number>(0);
  const saveTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const locationsReadyRef = useRef(false);
  const totalLocationsRef = useRef<number>(0);
  const sessionStartTimeRef = useRef<number | null>(null);
  const sessionStartProgressRef = useRef<number | null>(null);

  useEffect(() => {
    async function init() {
      const [b, userBooks] = await Promise.all([
        getBookById(Number(id)),
        getUserBooks().catch(() => []),
      ]);

      setBook(b);

      const userBook = userBooks.find((ub) => ub.book.id === Number(id));
      if (userBook) {
        userBookIdRef.current = userBook.id;
        const savedProgress = userBook.progress ?? 0;
        lastSavedProgressRef.current = savedProgress;
        setProgress(savedProgress);

        if (userBook.status === 'toread') {
          updateUserBook(userBook.id, { status: 'reading', progress: 0 }).catch(() => {});
        }
      }

      const saved = localStorage.getItem(`epub-location-${id}`);
      if (saved) setLocation(saved);

      if (b.downloadUrl) {
        const res = await fetch(`/api/epub?url=${encodeURIComponent(b.downloadUrl)}`);
        if (res.ok) setEpubData(await res.arrayBuffer());
      }
    }

    init().catch(console.error).finally(() => setLoading(false));

    return () => {
      if (saveTimerRef.current) clearTimeout(saveTimerRef.current);
    };
  }, [id]);

  const handleLocationChange = useCallback(
    (loc: string) => {
      setLocation(loc);
      localStorage.setItem(`epub-location-${id}`, loc);
    },
    [id]
  );

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const handleRendition = useCallback((rendition: any) => {
    rendition.book.ready.then(() => {
      rendition.book.locations.generate(1600).then(() => {
        locationsReadyRef.current = true;
        totalLocationsRef.current = rendition.book.locations.length();
      });
    });

    rendition.on('relocated', (loc: any) => {
      let percent = 0;

      if (locationsReadyRef.current) {
        percent = Math.round(
          rendition.book.locations.percentageFromCfi(loc.start.cfi) * 100
        );

        // páginas restantes via locations
        const currentLocation = rendition.book.locations.locationFromCfi(loc.start.cfi);
        const total = totalLocationsRef.current;
        const remaining = Math.max(total - currentLocation, 0);
        setPagesLeft(remaining);

        // minutos via velocidade real ou fallback de velocidade média
        const now = Date.now();
        if (sessionStartTimeRef.current === null) {
          sessionStartTimeRef.current = now;
          sessionStartProgressRef.current = percent;
          // fallback: estimativa por velocidade média de leitura
          setMinutesLeft(Math.round(remaining * MINUTES_PER_LOCATION));
        } else {
          const elapsedMin = (now - sessionStartTimeRef.current) / 60000;
          const progressDelta = percent - (sessionStartProgressRef.current ?? percent);

          if (elapsedMin >= 1 && progressDelta > 0) {
            const speedPerMin = progressDelta / elapsedMin;
            setMinutesLeft(Math.round((100 - percent) / speedPerMin));
          } else {
            // ainda aguardando dados suficientes: usa velocidade média
            setMinutesLeft(Math.round(remaining * MINUTES_PER_LOCATION));
          }
        }
      } else if (loc.start.percentage > 0) {
        percent = Math.round(loc.start.percentage * 100);
      } else {
        const total = rendition.book.spine?.items?.length ?? 1;
        percent = Math.round(((loc.start.index ?? 0) / Math.max(total - 1, 1)) * 100);
      }

      setProgress(percent);

      if (!userBookIdRef.current || percent === lastSavedProgressRef.current) return;

      if (saveTimerRef.current) clearTimeout(saveTimerRef.current);
      saveTimerRef.current = setTimeout(() => {
        lastSavedProgressRef.current = percent;
        updateUserBook(userBookIdRef.current!, {
          status: 'reading',
          progress: percent,
        }).catch(() => {});
      }, 2000);
    });
  }, []);

  function cycleDisplayMode() {
    setDisplayMode((prev) => {
      if (prev === 'percent') return 'minutes';
      if (prev === 'minutes') return 'pages';
      return 'percent';
    });
  }

  function renderReadingInfo() {
    if (!userBookIdRef.current) return null;

    if (displayMode === 'minutes' && minutesLeft !== null) {
      return (
        <button
          onClick={cycleDisplayMode}
          className="text-terra text-xs font-bold shrink-0 hover:opacity-70 transition-opacity"
        >
          {formatMinutes(minutesLeft)}
        </button>
      );
    }

    if (displayMode === 'pages' && pagesLeft !== null) {
      return (
        <button
          onClick={cycleDisplayMode}
          className="text-terra text-xs font-bold shrink-0 hover:opacity-70 transition-opacity"
        >
          ~{pagesLeft} pág. restantes
        </button>
      );
    }

    return (
      <button
        onClick={cycleDisplayMode}
        className="text-terra text-xs font-bold shrink-0 hover:opacity-70 transition-opacity"
      >
        {progress}%
      </button>
    );
  }

  if (loading) {
    return (
      <div className="flex items-center justify-center h-full">
        <p className="text-brown-soft text-sm">Carregando leitor...</p>
      </div>
    );
  }

  if (!book || !book.downloadUrl) {
    return (
      <div className="flex flex-col items-center justify-center h-full gap-4">
        <p className="text-brown font-serif text-xl">Arquivo de leitura indisponível.</p>
        <Link href={`/explorar/${id}`} className="text-terra text-sm font-bold underline">
          Voltar para o livro
        </Link>
      </div>
    );
  }

  return (
    <div className="flex flex-col h-full">
      <div className="flex items-center gap-4 px-6 py-3 border-b border-cream-border bg-cream shrink-0">
        <button
          onClick={() => router.back()}
          className="flex items-center gap-2 text-brown-soft text-sm font-bold hover:text-brown transition-colors"
        >
          <ArrowLeft size={16} />
          Voltar
        </button>
        <h1 className="font-serif font-bold text-brown text-sm truncate flex-1">{book.title}</h1>
        <span className="text-brown-soft text-xs shrink-0">{getAuthorDisplay(book)}</span>
        {renderReadingInfo()}
      </div>

      {userBookIdRef.current && (
        <div className="h-0.5 bg-cream-border shrink-0">
          <div
            className="h-full bg-terra transition-all duration-500"
            style={{ width: `${progress}%` }}
          />
        </div>
      )}

      <div className="relative h-[calc(100vh-57px)]">
        {epubData ? (
          <ReactReader
            url={epubData}
            location={location}
            locationChanged={handleLocationChange}
            getRendition={handleRendition}
          />
        ) : (
          <div className="flex items-center justify-center h-full">
            <p className="text-brown-soft text-sm">Carregando livro...</p>
          </div>
        )}
      </div>
    </div>
  );
}
