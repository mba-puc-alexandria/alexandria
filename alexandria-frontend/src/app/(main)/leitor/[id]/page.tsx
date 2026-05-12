"use client";

import { use, useEffect, useState, useCallback } from "react";
import dynamic from "next/dynamic";
import Link from "next/link";
import { ArrowLeft } from "lucide-react";
import { getBookById, type BookApiResponse } from "@/lib/api";

const ReactReader = dynamic(
  () => import("react-reader").then((m) => m.ReactReader),
  { ssr: false }
);

export default function LeitorPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = use(params);
  const [book, setBook] = useState<BookApiResponse | null>(null);
  const [epubData, setEpubData] = useState<ArrayBuffer | null>(null);
  const [loading, setLoading] = useState(true);
  const [location, setLocation] = useState<string | number>(0);

  useEffect(() => {
    getBookById(Number(id))
      .then(async (b) => {
        setBook(b);
        const saved = localStorage.getItem(`epub-location-${id}`);
        if (saved) setLocation(saved);

        if (b.downloadUrl) {
          const res = await fetch(`/api/epub?url=${encodeURIComponent(b.downloadUrl)}`);
          if (res.ok) {
            setEpubData(await res.arrayBuffer());
          }
        }
      })
      .catch(console.error)
      .finally(() => setLoading(false));
  }, [id]);

  const handleLocationChange = useCallback(
    (loc: string) => {
      setLocation(loc);
      localStorage.setItem(`epub-location-${id}`, loc);
    },
    [id]
  );

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
        <Link
          href={`/explorar/${id}`}
          className="flex items-center gap-2 text-brown-soft text-sm font-bold hover:text-brown transition-colors"
        >
          <ArrowLeft size={16} />
          Voltar
        </Link>
        <h1 className="font-serif font-bold text-brown text-sm truncate flex-1">{book.title}</h1>
        <span className="text-brown-soft text-xs">{book.author}</span>
      </div>

      <div className="relative h-[calc(100vh-53px)]">
        {epubData ? (
          <ReactReader
            url={epubData}
            location={location}
            locationChanged={handleLocationChange}
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
