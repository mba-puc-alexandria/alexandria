"use client";

import Link from "next/link";
import {
  Sparkles,
  ShieldCheck,
  BookOpen,
  WifiOff,
  QrCode,
  CreditCard,
  BadgeCheck,
  CalendarClock,
  Settings,
  Check,
  Minus,
  HelpCircle,
  AlertCircle,
  ListChecks,
  Route,
} from "lucide-react";
import { useAuth } from "@/contexts/AuthContext";

// Preço e prazo ainda são literais aqui — a API já devolve subscription.price e
// periodDays, mas a página é pública e nem sempre há assinatura para consultar.
const PRICE = "R$ 10,00";
const TRIAL_DAYS = 15;

// O paywall cobre apenas o download do EPUB (GetBookEpubUseCase no backend).
// Explorar o acervo, a biblioteca pessoal e o progresso são gratuitos com conta.
const FEATURES = [
  { icon: BookOpen, label: "Leitura completa de todos os livros do acervo" },
  { icon: WifiOff, label: "Leitura offline: o livro fica salvo no dispositivo" },
  { icon: QrCode, label: "Renovação por PIX ou cartão, como preferir" },
  { icon: CreditCard, label: "Sem fidelidade — cancele quando quiser" },
  { icon: ShieldCheck, label: "Pagamento processado pelo Mercado Pago" },
];

const STEPS = [
  {
    title: "Crie sua conta",
    text: "Leva menos de um minuto. Já dá para explorar o acervo e montar sua biblioteca.",
  },
  {
    title: `${TRIAL_DAYS} dias de leitura liberada`,
    text: "Pedimos um cartão para iniciar o teste, mas nada é cobrado agora.",
  },
  {
    title: `Depois, ${PRICE} por mês`,
    text: "A primeira cobrança só acontece no fim do teste. Cancele antes e não paga nada.",
  },
];

const COMPARISON = [
  { label: "Explorar e buscar o acervo", free: true, premium: true },
  { label: "Ver detalhes, capa e autores", free: true, premium: true },
  { label: "Biblioteca pessoal e progresso", free: true, premium: true, note: "com conta" },
  { label: "Abrir e ler os livros", free: false, premium: true },
  { label: "Leitura offline", free: false, premium: true },
];

const FAQS = [
  {
    q: `Preciso de cartão para os ${TRIAL_DAYS} dias grátis?`,
    a: "Sim. O cartão fica registrado para garantir a continuidade da assinatura, mas nenhuma cobrança é feita durante o teste. Se você cancelar antes do fim, não paga nada.",
  },
  {
    q: "Posso pagar com PIX?",
    a: `Sim, nas renovações. O teste inicial exige cartão, mas a partir da primeira renovação você escolhe entre PIX e cartão a cada cobrança.`,
  },
  {
    q: "O PIX é cobrado automaticamente?",
    a: "Não. PIX não tem débito automático: a cada renovação é gerado um novo QR Code para você pagar.",
  },
  {
    q: "Como eu cancelo?",
    a: "Em Configurações → Assinatura → Cancelar assinatura. Sem multa e sem precisar falar com ninguém.",
  },
  {
    q: "O que acontece quando eu cancelo?",
    a: "Você continua lendo normalmente até o fim do período já pago. Depois disso, o leitor volta a pedir assinatura.",
  },
  {
    q: "Perco minha biblioteca e meu progresso?",
    a: "Não. Sua biblioteca, suas marcações de status e o ponto onde você parou em cada livro continuam salvos, e voltam a valer se você assinar de novo.",
  },
  {
    q: "E se o pagamento falhar?",
    a: "A assinatura fica com pagamento pendente e você pode regularizar ou trocar o cartão em Configurações.",
  },
];

type PlanStatus =
  | "TRIALING"
  | "ACTIVE"
  | "PAST_DUE"
  | "EXPIRED"
  | "CANCELED"
  | "NONE"
  | "ANON";

// Badge e CTA por estado. PAST_DUE, EXPIRED e CANCELED nunca oferecem teste
// grátis: esse usuário já consumiu o trial.
const VARIANTS: Record<
  PlanStatus,
  {
    badge: string;
    badgeTone: "ok" | "warn" | "offer";
    cta: { href: string; label: string; icon?: typeof Settings };
    note: string;
  }
> = {
  TRIALING: {
    badge: "Teste ativo",
    badgeTone: "ok",
    cta: { href: "/configuracoes", label: "Gerenciar assinatura", icon: Settings },
    note: "Seu cartão está salvo com segurança. A primeira cobrança acontece apenas após o teste.",
  },
  ACTIVE: {
    badge: "Plano ativo",
    badgeTone: "ok",
    cta: { href: "/configuracoes", label: "Gerenciar assinatura", icon: Settings },
    note: "Cancele quando quiser. Você mantém o acesso até o fim do período pago.",
  },
  PAST_DUE: {
    badge: "Pagamento pendente",
    badgeTone: "warn",
    cta: { href: "/checkout", label: "Regularizar pagamento" },
    note: "Enquanto o pagamento não é confirmado, a leitura pode ser bloqueada.",
  },
  EXPIRED: {
    badge: "Assinatura encerrada",
    badgeTone: "warn",
    cta: { href: "/checkout", label: "Reativar assinatura" },
    note: "Sua biblioteca e seu progresso continuam salvos.",
  },
  CANCELED: {
    badge: "Assinatura encerrada",
    badgeTone: "warn",
    cta: { href: "/checkout", label: "Reativar assinatura" },
    note: "Sua biblioteca e seu progresso continuam salvos.",
  },
  NONE: {
    badge: `${TRIAL_DAYS} dias grátis`,
    badgeTone: "offer",
    cta: { href: "/checkout", label: "Começar teste grátis" },
    note: "Sem cobrança durante o período de teste. Cancele quando quiser.",
  },
  ANON: {
    badge: `${TRIAL_DAYS} dias grátis`,
    badgeTone: "offer",
    cta: { href: "/registrar", label: "Criar conta grátis" },
    note: "Sem cobrança durante o período de teste. Cancele quando quiser.",
  },
};

const BADGE_TONE = {
  ok: "bg-success/10 text-success",
  warn: "bg-danger/10 text-danger",
  offer: "bg-terra/10 text-terra",
};

function formatDate(iso: string | null | undefined) {
  return iso ? new Date(iso).toLocaleDateString("pt-BR") : null;
}

function daysLeft(iso: string | null | undefined) {
  if (!iso) return null;
  const diff = new Date(iso).getTime() - Date.now();
  return Math.max(0, Math.ceil(diff / 86_400_000));
}

export default function PlanosPage() {
  const { user, subscription, isLoading } = useAuth();

  // Sem usuário não há assinatura para esperar: renderiza a oferta direto, para que
  // a página pública não sirva um esqueleto no HTML inicial.
  if (isLoading && user) return <PlanosSkeleton />;

  const status: PlanStatus = subscription?.status ?? (user ? "NONE" : "ANON");
  const variant = VARIANTS[status];
  const isTrialing = status === "TRIALING";
  const hasAccess = isTrialing || status === "ACTIVE";
  const needsTrialNotice = status === "NONE" || status === "ANON";

  const trialEndsAt = formatDate(subscription?.trialEndsAt);
  const renewalDate = formatDate(subscription?.currentPeriodEndsAt);
  const trialDaysLeft = daysLeft(subscription?.trialEndsAt);

  const title = hasAccess
    ? isTrialing
      ? "Seu teste Alexandria Premium está ativo"
      : "Seu Alexandria Premium está ativo"
    : status === "PAST_DUE"
      ? "Regularize seu pagamento para continuar lendo"
      : status === "EXPIRED" || status === "CANCELED"
        ? "Sua assinatura terminou"
        : "Continue sua leitura sem interrupções";

  return (
    <div className="px-6 md:px-8 pt-8 md:pt-12 pb-8 flex flex-col items-center gap-12">
      {/* Cabeçalho */}
      <div className="text-center max-w-2xl">
        <div className="inline-flex items-center gap-2 bg-brown/5 text-brown rounded-full px-4 py-1.5 text-xs font-bold uppercase tracking-widest mb-5">
          <Sparkles size={14} className="text-terra" />
          Assinatura Alexandria
        </div>
        <h1 className="font-serif font-bold text-brown text-3xl md:text-4xl leading-tight">
          {title}
        </h1>
        <p className="text-slate text-sm md:text-base mt-3">
          {hasAccess ? (
            <>
              Você tem acesso completo à leitura de todo o acervo.
              {isTrialing && trialDaysLeft !== null ? (
                <>
                  {" "}
                  {trialDaysLeft === 0 ? (
                    <strong className="text-brown">Seu teste termina hoje.</strong>
                  ) : (
                    <>
                      Restam{" "}
                      <strong className="text-brown">
                        {trialDaysLeft} {trialDaysLeft === 1 ? "dia" : "dias"}
                      </strong>{" "}
                      de teste{trialEndsAt ? `, até ${trialEndsAt}` : ""}.
                    </>
                  )}
                </>
              ) : (
                renewalDate && (
                  <>
                    {" "}
                    Sua próxima renovação é em{" "}
                    <strong className="text-brown">{renewalDate}</strong>.
                  </>
                )
              )}
            </>
          ) : status === "PAST_DUE" ? (
            <>
              Não conseguimos confirmar sua última cobrança. Atualize o pagamento para
              manter o acesso à leitura — sua biblioteca segue intacta.
            </>
          ) : status === "EXPIRED" || status === "CANCELED" ? (
            <>
              Reative quando quiser por <strong className="text-brown">{PRICE} por mês</strong>.
              Sua biblioteca e seu progresso de leitura continuam salvos.
            </>
          ) : (
            <>
              Explorar o acervo é grátis. A leitura dos livros é o que o Premium libera —
              comece com <strong className="text-brown">{TRIAL_DAYS} dias grátis</strong> e
              depois apenas <strong className="text-brown">{PRICE} por mês</strong>.
            </>
          )}
        </p>
      </div>

      {/* Card de preço */}
      <div className="w-full max-w-md">
        <div className="bg-cream-dark rounded-2xl p-8 border border-cream-border shadow-sm flex flex-col">
          <div className="flex items-end justify-between mb-6">
            <div>
              <span className="text-brown-soft text-xs font-bold uppercase tracking-widest">
                Alexandria Premium
              </span>
              <div className="flex items-baseline gap-1 mt-2">
                <span className="text-brown text-sm font-bold">R$</span>
                <span className="font-serif font-bold text-brown text-5xl">10,00</span>
                <span className="text-brown-soft text-sm">/mês</span>
              </div>
            </div>
            <div
              className={`${BADGE_TONE[variant.badgeTone]} rounded-full px-3 py-1 text-[11px] font-bold uppercase tracking-wide flex items-center gap-1`}
            >
              {variant.badgeTone === "ok" && <BadgeCheck size={13} />}
              {variant.badgeTone === "warn" && <AlertCircle size={13} />}
              {variant.badge}
            </div>
          </div>

          {hasAccess && (
            <div className="mb-6 flex items-center gap-2 rounded-xl border border-cream-border bg-cream px-3 py-2.5 text-sm text-brown">
              <CalendarClock size={17} className="text-terra shrink-0" />
              {isTrialing
                ? trialEndsAt
                  ? `Teste gratuito até ${trialEndsAt}`
                  : "Teste gratuito ativo"
                : renewalDate
                  ? `Renovação mensal em ${renewalDate}`
                  : "Renovação mensal ativa"}
            </div>
          )}

          <ul className="flex flex-col gap-3 mb-8">
            {FEATURES.map(({ icon: Icon, label }) => (
              <li key={label} className="flex items-start gap-3">
                <span className="bg-brown/5 rounded-lg p-1.5 mt-0.5">
                  <Icon size={16} className="text-terra" />
                </span>
                <span className="text-brown text-sm">{label}</span>
              </li>
            ))}
          </ul>

          {/* O checkout aceita somente cartão durante o teste — avisar antes do clique */}
          {needsTrialNotice && (
            <div className="mb-6 flex items-start gap-3 rounded-xl border border-cream-border bg-cream px-4 py-3">
              <CreditCard size={16} className="text-terra shrink-0 mt-0.5" />
              <p className="text-brown-soft text-xs leading-relaxed">
                Para iniciar o teste pedimos um{" "}
                <strong className="text-brown">cartão de crédito</strong>, mas{" "}
                <strong className="text-brown">nada é cobrado agora</strong>. A primeira
                cobrança acontece só no fim dos {TRIAL_DAYS} dias — e a partir daí você pode
                pagar por PIX.
              </p>
            </div>
          )}

          <Link
            href={variant.cta.href}
            className="bg-brown text-cream text-center font-bold text-sm tracking-widest uppercase px-6 py-4 rounded-xl hover:bg-brown/90 transition-colors flex items-center justify-center gap-2"
          >
            {variant.cta.icon && <variant.cta.icon size={16} />}
            {variant.cta.label}
          </Link>

          <p className="text-center text-brown-soft/70 text-xs mt-4">{variant.note}</p>
        </div>

        <p className="text-center text-brown-soft/60 text-xs mt-6 flex items-center justify-center gap-1.5">
          <ShieldCheck size={14} />
          Pagamento seguro processado pelo Mercado Pago
        </p>
      </div>

      {/* Como funciona */}
      <section className="w-full max-w-3xl flex flex-col gap-4">
        <div className="flex items-center gap-2">
          <Route size={14} className="text-terra" />
          <span className="text-brown text-xs font-bold uppercase tracking-widest">
            Como funciona
          </span>
        </div>
        <ol className="grid gap-3 md:grid-cols-3">
          {STEPS.map(({ title: stepTitle, text }, i) => (
            <li
              key={stepTitle}
              className="bg-cream-dark rounded-xl p-5 border border-cream-border flex flex-col gap-2"
            >
              <span className="bg-terra/10 text-terra w-7 h-7 rounded-full flex items-center justify-center text-xs font-bold">
                {i + 1}
              </span>
              <p className="text-brown font-bold text-sm">{stepTitle}</p>
              <p className="text-slate text-sm leading-relaxed">{text}</p>
            </li>
          ))}
        </ol>
      </section>

      {/* Comparativo */}
      <section className="w-full max-w-3xl flex flex-col gap-4">
        <div className="flex items-center gap-2">
          <ListChecks size={14} className="text-terra" />
          <span className="text-brown text-xs font-bold uppercase tracking-widest">
            O que muda com o Premium
          </span>
        </div>
        <div className="bg-cream-dark rounded-xl border border-cream-border overflow-hidden">
          <div className="grid grid-cols-[1fr_auto_auto] items-center gap-x-6 px-5 py-3 border-b border-cream-border">
            <span className="text-brown-soft text-[11px] font-bold uppercase tracking-widest">
              Recurso
            </span>
            <span className="text-brown-soft text-[11px] font-bold uppercase tracking-widest w-16 text-center">
              Grátis
            </span>
            <span className="text-terra text-[11px] font-bold uppercase tracking-widest w-16 text-center">
              Premium
            </span>
          </div>
          {COMPARISON.map(({ label, free, premium, note }) => (
            <div
              key={label}
              className="grid grid-cols-[1fr_auto_auto] items-center gap-x-6 px-5 py-3.5 border-b border-cream-border last:border-b-0"
            >
              <span className="text-brown text-sm">
                {label}
                {note && <span className="text-brown-soft/70 text-xs"> ({note})</span>}
              </span>
              <span className="w-16 flex justify-center">
                {free ? (
                  <Check size={16} className="text-success" />
                ) : (
                  <Minus size={16} className="text-brown-soft/40" />
                )}
              </span>
              <span className="w-16 flex justify-center">
                {premium ? (
                  <Check size={16} className="text-success" />
                ) : (
                  <Minus size={16} className="text-brown-soft/40" />
                )}
              </span>
            </div>
          ))}
        </div>
      </section>

      {/* FAQ */}
      <section className="w-full max-w-3xl flex flex-col gap-4">
        <div className="flex items-center gap-2">
          <HelpCircle size={14} className="text-terra" />
          <span className="text-brown text-xs font-bold uppercase tracking-widest">
            Dúvidas frequentes
          </span>
        </div>
        <div className="flex flex-col gap-3">
          {FAQS.map(({ q, a }) => (
            <details
              key={q}
              className="bg-cream-dark rounded-xl border border-cream-border px-5 py-4 group"
            >
              <summary className="cursor-pointer text-brown font-bold text-sm marker:text-terra">
                {q}
              </summary>
              <p className="text-slate text-sm leading-relaxed mt-2">{a}</p>
            </details>
          ))}
        </div>
      </section>
    </div>
  );
}

function PlanosSkeleton() {
  return (
    <div className="px-6 md:px-8 pt-8 md:pt-12 pb-8 flex flex-col items-center gap-8">
      <div className="flex flex-col items-center gap-3 w-full max-w-2xl">
        <div className="h-6 w-48 bg-cream-book rounded-full animate-pulse" />
        <div className="h-9 w-full max-w-lg bg-cream-book rounded animate-pulse" />
        <div className="h-4 w-3/4 bg-cream-book rounded animate-pulse" />
      </div>
      <div className="w-full max-w-md h-96 bg-cream-book rounded-2xl animate-pulse" />
    </div>
  );
}
