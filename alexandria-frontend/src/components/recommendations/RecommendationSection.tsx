'use client';

import Link from 'next/link';
import { useEffect, useRef } from 'react';
import { useAuth } from '@/contexts/AuthContext';
import { useRecommendations } from '@/hooks/useRecommendations';
import { RecommendationCarousel } from './RecommendationCarousel';

export function RecommendationSection() {
  const { user, logout } = useAuth();
  const recommendations = useRecommendations(user?.userId ?? null, logout);
  const { data, loading, refreshing, error, notice, generationStatus, remainingSeconds, canRetry, pollsExhausted } = recommendations;
  const heading = useRef<HTMLHeadingElement>(null);
  const lastCount = useRef(0);
  const count = data?.items.length ?? 0;
  useEffect(() => {
    if (lastCount.current > 0 && count === 0 && document.activeElement === document.body) heading.current?.focus();
    lastCount.current = count;
  }, [count]);
  if (!user) return null;
  const title = <h2 ref={heading} tabIndex={-1} id="recommendations-heading" className="min-w-0 font-serif text-xl font-bold leading-snug text-brown">Você também pode gostar</h2>;
  const pending = data?.updatePending || data?.state === 'GENERATING';
  const emptyMessage = data?.state === 'STALE'
    ? 'A atualização está pendente. Ainda não há sugestões disponíveis.'
    : data?.emptyReason === 'NO_PROFILE'
      ? 'Ainda não encontramos interesses suficientes para recomendar. Explore o catálogo e adicione livros que quer ler.'
      : data?.emptyReason === 'NO_MATCHES'
        ? 'Ainda não há sugestões disponíveis. Explore o catálogo para descobrir outras leituras.'
        : 'Você chegou ao fim das sugestões disponíveis. Explore o catálogo para descobrir outras leituras.';
  return <section aria-labelledby="recommendations-heading" data-testid="recommendations" data-state={data?.state || (loading ? 'LOADING' : 'ERROR')} className="flex flex-col gap-4">
    {count > 0 && data ? <RecommendationCarousel items={data.items} heading={title} onRemoved={recommendations.removeConfirmed} onErrorRevalidate={() => void recommendations.refresh()} /> : title}
    {loading && !data && <p role="status" data-testid="recommendations-loading" className="text-sm text-slate">Carregando recomendações…</p>}
    {data?.state === 'GENERATING' && <p role="status" className="text-sm text-slate">Preparando suas recomendações. {pollsExhausted ? 'A preparação continua. Verifique a atualização quando quiser.' : 'Aguarde um momento.'}</p>}
    {data?.state === 'STALE' && <p className="text-sm text-slate">Atualização pendente. Você pode continuar explorando as sugestões disponíveis.</p>}
    {data && data.state !== 'GENERATING' && count === 0 && <div data-testid="recommendations-empty" className="rounded-lg bg-cream-dark p-4 text-sm text-slate"><p>{emptyMessage}</p><Link href="/explorar" className="mt-3 inline-block font-bold text-terra underline focus-visible:outline-2">Explorar catálogo</Link></div>}
    {notice && <p role="status" className="text-sm text-brown">{notice}</p>}
    {error && <p role="alert" className="text-sm text-brown">{error.message}</p>}
    {(pending || error) && <div>
      <button type="button" disabled={!canRetry} onClick={() => void recommendations.retry()} className="cursor-pointer rounded border border-brown/30 px-4 py-2 text-sm font-bold text-brown disabled:cursor-wait disabled:opacity-60 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-terra">
        {refreshing ? 'Verificando…' : generationStatus === 'FAILED' ? 'Tentar novamente' : 'Verificar atualização'}
      </button>
      {remainingSeconds > 0 && <p className="mt-2 text-xs text-slate">Aguarde um momento para tentar novamente.</p>}
    </div>}
  </section>;
}
