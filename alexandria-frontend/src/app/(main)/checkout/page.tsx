"use client";

import { useState } from "react";
import Link from "next/link";
import {
  QrCode,
  CreditCard,
  Copy,
  Check,
  ShieldCheck,
  ArrowLeft,
  Loader2,
  BadgeCheck,
  BookOpen,
  WifiOff,
  CalendarClock,
  ExternalLink,
} from "lucide-react";
import { useAuth } from "@/contexts/AuthContext";
import { createCheckout, type CheckoutResponse } from "@/lib/api";
import MercadoPagoCardForm, { type MercadoPagoCardData } from "@/components/MercadoPagoCardForm";

type Method = "pix" | "card";

const BENEFITS = [
  { icon: BookOpen, label: "Leitura completa de todo o acervo" },
  { icon: WifiOff, label: "Leitura offline no dispositivo" },
  { icon: CalendarClock, label: "Sem fidelidade — cancele quando quiser" },
];

function formatMoney(value: number, currency = "BRL") {
  return new Intl.NumberFormat("pt-BR", { style: "currency", currency }).format(value);
}

function formatDate(iso: string | null | undefined) {
  return iso ? new Date(iso).toLocaleDateString("pt-BR") : null;
}

export default function CheckoutPage() {
  const { subscription } = useAuth();

  // Durante o trial, só cartão. Após o trial, PIX é o destaque.
  const status = subscription?.status;
  const inTrial = status === "TRIALING";
  const [method, setMethod] = useState<Method>(inTrial ? "card" : "pix");

  const price = formatMoney(subscription?.price ?? 10, subscription?.currency);
  const periodDays = subscription?.periodDays ?? 30;
  const trialEndsAt = formatDate(subscription?.trialEndsAt);

  const [copied, setCopied] = useState(false);
  const [processing, setProcessing] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Resultado do checkout (PIX ou cartão)
  const [result, setResult] = useState<CheckoutResponse | null>(null);
  const [pixCode, setPixCode] = useState<string | null>(null);
  const [pixImage, setPixImage] = useState<string | null>(null);

  const [payerEmail, setPayerEmail] = useState("");
  const payerDocumentType = "CPF";
  const payerDocumentNumber = "";

  async function handlePix() {
    setError(null);
    setProcessing(true);
    try {
      const res = await createCheckout({
        paymentMethod: "PIX",
        payerEmail: payerEmail || undefined,
        payerDocumentType: payerDocumentType || undefined,
        payerDocumentNumber: payerDocumentNumber || undefined,
      });
      setResult(res);
      setPixCode(res.qrCode);
      setPixImage(res.qrCodeBase64 ? `data:image/png;base64,${res.qrCodeBase64}` : null);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Falha ao gerar PIX");
    } finally {
      setProcessing(false);
    }
  }

  async function handleCard(data: MercadoPagoCardData) {
    setError(null);
    setProcessing(true);
    try {
      const res = await createCheckout({
        paymentMethod: "CARD",
        cardToken: data.token,
        cardBrand: data.paymentMethodId,
        installments: data.installments,
        payerEmail: data.cardholderEmail,
        payerDocumentType: data.identificationType,
        payerDocumentNumber: data.identificationNumber,
      });
      setResult(res);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Falha ao processar cartão");
    } finally {
      setProcessing(false);
    }
  }

  function copyPix() {
    if (!pixCode) return;
    navigator.clipboard?.writeText(pixCode).catch(() => {});
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  }

  // Estado de sucesso
  if (result) {
    const isPix = method === "pix";
    const scheduled = inTrial && !isPix && result.subscriptionStatus !== "ACTIVE";

    let title = "Assinatura ativada!";
    let message = "Seu pagamento foi aprovado e sua assinatura está ativa.";
    let highlight: string | null = null;

    if (scheduled) {
      title = "Assinatura confirmada!";
      message = trialEndsAt
        ? `Você não foi cobrado agora. A primeira cobrança de ${price} acontece em ${trialEndsAt}.`
        : "Você não foi cobrado agora. A cobrança acontece quando seu período de teste terminar.";
      highlight = "Seu período de teste continua até o fim.";
    } else if (isPix) {
      title = "Falta pouco: pague o PIX";
      message = "Escaneie o QR Code ou use o código copia e cola no app do seu banco. O acesso é liberado assim que o pagamento for confirmado.";
      highlight = result.message || null;
    } else {
      message = result.message || message;
    }

    const Icon = isPix && !scheduled ? QrCode : BadgeCheck;

    return (
      <div className="px-6 md:px-8 pt-8 md:pt-12 pb-8 flex flex-col items-center">
        <div className="max-w-md w-full text-center">
          <div className="inline-flex items-center justify-center bg-terra/10 rounded-full p-5 mb-6">
            <Icon size={40} className="text-terra" />
          </div>
          <h1 className="font-serif font-bold text-brown text-3xl mb-3">{title}</h1>
          <p className="text-slate text-sm mb-8">{message}</p>

          {highlight && (
            <div className="bg-terra/10 text-terra rounded-xl px-5 py-3 text-sm font-bold mb-6">
              {highlight}
            </div>
          )}

          {isPix && (pixCode || pixImage) && (
            <div className="bg-cream-dark rounded-xl p-5 border border-cream-border text-left mb-8 flex flex-col gap-3">
              {pixImage && (
                <img src={pixImage} alt="QR Code PIX" className="w-48 h-48 self-center rounded-lg" />
              )}
              <span className="text-brown-soft text-xs uppercase tracking-widest font-bold">
                PIX copia e cola
              </span>
              {pixCode && <div className="flex items-center gap-2">
                <code className="flex-1 bg-cream rounded-lg px-3 py-2 text-xs text-brown break-all">
                  {pixCode}
                </code>
                <button
                  onClick={copyPix}
                  aria-label="Copiar código PIX"
                  className="p-2 rounded-lg bg-cream border border-cream-border text-brown-soft hover:text-brown"
                >
                  {copied ? <Check size={16} className="text-success" /> : <Copy size={16} />}
                </button>
              </div>}
              {result.ticketUrl && (
                <a
                  href={result.ticketUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="inline-flex items-center gap-1.5 text-terra text-xs font-bold hover:underline self-center"
                >
                  Abrir pagamento no Mercado Pago
                  <ExternalLink size={12} />
                </a>
              )}
            </div>
          )}

          <div className="bg-cream-dark rounded-xl p-5 border border-cream-border text-left mb-8">
            <div className="flex justify-between py-1">
              <span className="text-brown-soft text-sm">Plano</span>
              <span className="text-brown text-sm font-bold">Alexandria Premium</span>
            </div>
            <div className="flex justify-between py-1">
              <span className="text-brown-soft text-sm">Valor</span>
              <span className="text-brown text-sm font-bold">{price}/mês</span>
            </div>
            <div className="flex justify-between py-1">
              <span className="text-brown-soft text-sm">Método</span>
              <span className="text-brown text-sm font-bold">{isPix ? "PIX" : "Cartão"}</span>
            </div>
            {scheduled && (
              <div className="flex justify-between py-1">
                <span className="text-brown-soft text-sm">Primeira cobrança</span>
                <span className="text-brown text-sm font-bold">{trialEndsAt ?? "Ao fim do teste"}</span>
              </div>
            )}
          </div>

          <Link
            href="/explorar"
            className="bg-brown text-cream font-bold text-sm tracking-widest uppercase px-6 py-4 rounded-xl hover:bg-brown/90 transition-colors inline-block"
          >
            {isPix && !scheduled ? "Já paguei, ir para a biblioteca" : "Ir para a biblioteca"}
          </Link>
        </div>
      </div>
    );
  }

  const subtitle =
    status === "PAST_DUE"
      ? "Regularize o pagamento para continuar lendo."
      : status === "EXPIRED" || status === "CANCELED"
        ? "Reative sua assinatura e volte a ler de onde parou."
        : inTrial
          ? "Salve seu cartão para continuar lendo após o teste."
          : "Assine o Alexandria Premium.";

  return (
    <div className="px-6 md:px-8 pt-8 md:pt-12 pb-8 flex flex-col items-center">
      <div className="max-w-4xl w-full">
        <Link
          href="/planos"
          className="inline-flex items-center gap-2 text-brown-soft text-sm font-bold hover:text-brown transition-colors mb-6"
        >
          <ArrowLeft size={16} />
          Voltar para planos
        </Link>

        <h1 className="font-serif font-bold text-brown text-2xl mb-1">Checkout</h1>
        <p className="text-slate text-sm mb-8">{subtitle}</p>

        <div className="grid gap-6 md:grid-cols-[1fr_320px] md:items-start">
          {/* Resumo: primeiro no mobile, coluna lateral fixa no desktop */}
          <aside className="md:order-2 md:sticky md:top-8 bg-cream-dark rounded-2xl p-6 border border-cream-border flex flex-col gap-5">
            <div>
              <span className="text-brown-soft text-xs uppercase tracking-widest font-bold">
                Alexandria Premium
              </span>
              <div className="mt-1">
                <span className="font-serif font-bold text-brown text-3xl">{price}</span>
                <span className="text-brown-soft text-sm">/mês</span>
              </div>
            </div>

            <ul className="flex flex-col gap-2.5">
              {BENEFITS.map(({ icon: BenefitIcon, label }) => (
                <li key={label} className="flex items-center gap-2.5 text-brown text-sm">
                  <BenefitIcon size={16} className="text-terra shrink-0" />
                  {label}
                </li>
              ))}
            </ul>

            <div className="border-t border-cream-border pt-4 flex flex-col gap-1.5 text-sm">
              <div className="flex justify-between">
                <span className="text-brown-soft">Total hoje</span>
                <span className="text-brown font-bold">{inTrial ? formatMoney(0) : price}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-brown-soft">{inTrial ? "Primeira cobrança" : "Período"}</span>
                <span className="text-brown font-bold">
                  {inTrial ? (trialEndsAt ?? "Fim do teste") : `${periodDays} dias`}
                </span>
              </div>
            </div>

            {inTrial && (
              <p className="text-terra text-xs font-bold">
                Nada é cobrado agora. Cancele antes{trialEndsAt ? ` de ${trialEndsAt}` : " do fim do teste"} e
                você não paga nada.
              </p>
            )}

            <p className="text-brown-soft/70 text-[11px] flex items-start gap-1.5">
              <ShieldCheck size={13} className="shrink-0 mt-px" />
              Pagamento processado pelo Mercado Pago. Os dados do cartão não passam pelo Alexandria.
            </p>
          </aside>

          <div className="md:order-1 flex flex-col">
            {/* Seleção de método */}
            {inTrial ? (
              <div className="flex items-center gap-2 rounded-xl px-4 py-3.5 text-sm font-bold bg-brown text-cream border border-brown mb-6">
                <CreditCard size={18} />
                Cartão de crédito
                <span className="ml-auto text-cream/70 text-xs font-medium">PIX disponível nas renovações</span>
              </div>
            ) : (
              <div className="grid grid-cols-2 gap-3 mb-6">
                {([
                  { id: "pix", icon: QrCode, label: "PIX", hint: "Pague no app do banco" },
                  { id: "card", icon: CreditCard, label: "Cartão", hint: "Renova automaticamente" },
                ] as const).map(({ id, icon: MethodIcon, label, hint }) => (
                  <button
                    key={id}
                    onClick={() => setMethod(id)}
                    aria-pressed={method === id}
                    className={`flex flex-col items-center justify-center gap-0.5 rounded-xl px-4 py-3 text-sm font-bold transition-colors border ${
                      method === id
                        ? "bg-brown text-cream border-brown"
                        : "bg-cream-dark text-brown-soft border-cream-border hover:bg-cream-active"
                    }`}
                  >
                    <span className="flex items-center gap-2">
                      <MethodIcon size={18} />
                      {label}
                    </span>
                    <span className="text-[11px] font-medium opacity-70">{hint}</span>
                  </button>
                ))}
              </div>
            )}

            {error && (
              <p className="text-danger text-sm bg-danger/10 rounded-lg px-4 py-3 mb-4">{error}</p>
            )}

            {/* Painel PIX */}
            {method === "pix" && !inTrial && (
              <div className="bg-cream-dark rounded-2xl p-6 border border-cream-border flex flex-col gap-4">
                <div className="flex items-center gap-2">
                  <QrCode size={18} className="text-terra" />
                  <span className="text-brown font-bold text-sm">Pagamento via PIX</span>
                </div>
                <p className="text-brown-soft text-xs">
                  Geramos um QR Code para você pagar no app do banco. PIX não renova sozinho: a cada
                  mês você recebe um novo código.
                </p>

                <label className="flex flex-col gap-1">
                  <span className="text-xs font-bold text-brown-soft uppercase tracking-widest">
                    E-mail do pagador <span className="normal-case font-medium tracking-normal">(opcional)</span>
                  </span>
                  <input
                    type="email"
                    value={payerEmail}
                    onChange={(e) => setPayerEmail(e.target.value)}
                    placeholder="voce@email.com"
                    className="bg-cream rounded-lg px-4 py-3 text-brown outline-none border border-cream-border focus:border-terra"
                  />
                </label>

                <button
                  onClick={handlePix}
                  disabled={processing}
                  className="mt-2 bg-brown text-cream font-bold text-sm tracking-widest uppercase px-6 py-4 rounded-xl hover:bg-brown/90 transition-colors disabled:opacity-50 flex items-center justify-center gap-2"
                >
                  {processing ? (
                    <>
                      <Loader2 size={16} className="animate-spin" />
                      Gerando PIX...
                    </>
                  ) : (
                    `Gerar PIX de ${price}`
                  )}
                </button>
              </div>
            )}

            {/* Painel Cartão */}
            {method === "card" && (
              <div className="bg-cream-dark rounded-2xl p-6 border border-cream-border flex flex-col gap-4">
                <div className="flex items-center gap-2 mb-1">
                  <CreditCard size={18} className="text-terra" />
                  <span className="text-brown font-bold text-sm">Pagamento com cartão</span>
                </div>
                <MercadoPagoCardForm
                  processing={processing}
                  onToken={handleCard}
                  submitLabel={inTrial ? "Salvar cartão — R$ 0,00 hoje" : `Pagar ${price}`}
                />
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
