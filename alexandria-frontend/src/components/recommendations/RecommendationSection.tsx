'use client';

import Link from 'next/link';
import { useEffect, useRef } from 'react';
import { useAuth } from '@/contexts/AuthContext';
import { useRecommendations } from '@/hooks/useRecommendations';
import { RecommendationCarousel } from './RecommendationCarousel';
import { selectionDate } from './RecommendationCard';

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
  const pending = data?.updatePending || data?.state === 'GENERATING';
  const emptyMessage = data?.state === 'STALE'
    ? 'A atualização de hoje está pendente. Ainda não há sugestões disponíveis.'
    : data?.emptyReason === 'NO_PROFILE'
      ? 'Ainda não encontramos interesses suficientes para recomendar. Explore o catálogo e adicione livros que quer ler.'
      : data?.emptyReason === 'NO_MATCHES'
        ? 'Não encontramos sugestões compatíveis na seleção diária. Explore o catálogo para descobrir outras leituras.'
        : 'Você chegou ao fim das sugestões disponíveis. A próxima seleção será preparada na atualização diária.';
  return <section aria-labelledby="recommendations-heading" data-testid="recommendations" data-state={data?.state || (loading ? 'LOADING' : 'ERROR')} className="flex flex-col gap-4">
    <div>
      <h2 ref={heading} tabIndex={-1} id="recommendations-heading" className="font-serif text-xl font-bold text-brown">Recomendados para você</h2>
      {data?.selection && <p className="mt-1 text-xs text-slate">Seleção de {selectionDate(data.selection.referenceDate)} · {count} {count === 1 ? 'sugestão disponível' : 'sugestões disponíveis'}</p>}
    </div>
    {loading && !data && <p role="status" data-testid="recommendations-loading" className="text-sm text-slate">Carregando recomendações…</p>}
    {data?.state === 'GENERATING' && <p role="status" className="text-sm text-slate">Preparando suas recomendações. {pollsExhausted ? 'A preparação continua. Verifique a atualização quando quiser.' : 'Aguarde um momento.'}</p>}
    {data?.state === 'STALE' && <p className="rounded bg-cream-dark p-3 text-sm text-brown">Atualização pendente — mostrando a última seleção disponível.</p>}
    {data && data.state !== 'GENERATING' && count === 0 && <div data-testid="recommendations-empty" className="rounded-lg bg-cream-dark p-4 text-sm text-slate"><p>{emptyMessage}</p><Link href="/explorar" className="mt-3 inline-block font-bold text-terra underline focus-visible:outline-2">Explorar catálogo</Link></div>}
    {data?.selection && count > 0 && <RecommendationCarousel items={data.items} referenceDate={data.selection.referenceDate} onRemoved={recommendations.removeConfirmed} onErrorRevalidate={() => void recommendations.refresh()} />}
    {notice && <p role="status" className="text-sm text-brown">{notice}</p>}
    {error && <p role="alert" className="text-sm text-brown">{error.message}</p>}
    {(pending || error) && <div>
      <button type="button" disabled={!canRetry} onClick={() => void recommendations.retry()} className="rounded border border-brown/30 px-4 py-2 text-sm font-bold text-brown disabled:opacity-60 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-terra">
        {refreshing ? 'Verificando…' : generationStatus === 'FAILED' ? 'Tentar novamente' : 'Verificar atualização'}
      </button>
      {remainingSeconds > 0 && <p className="mt-2 text-xs text-slate">Nova tentativa em {remainingSeconds}s.</p>}
    </div>}
  </section>;
}
