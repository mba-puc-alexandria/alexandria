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
} from "lucide-react";
import { useAuth } from "@/contexts/AuthContext";
import { createCheckout, type CheckoutResponse } from "@/lib/api";

type Method = "pix" | "card";

export default function CheckoutPage() {
  const { subscription } = useAuth();

  // Durante o trial, só cartão. Após o trial, PIX é o destaque.
  const inTrial = subscription?.status === "TRIALING";
  const [method, setMethod] = useState<Method>(inTrial ? "card" : "pix");

  const [copied, setCopied] = useState(false);
  const [processing, setProcessing] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Resultado do checkout (PIX ou cartão)
  const [result, setResult] = useState<CheckoutResponse | null>(null);
  const [pixCode, setPixCode] = useState<string | null>(null);

  // Dados do cartão — no fluxo real, o MercadoPago.js CardForm gera o cardToken.
  const [cardToken, setCardToken] = useState("");
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
      setPixCode(res.qrCode || res.qrCodeBase64 || null);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Falha ao gerar PIX");
    } finally {
      setProcessing(false);
    }
  }

  async function handleCard(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    setProcessing(true);
    try {
      const res = await createCheckout({
        paymentMethod: "CARD",
        cardToken,
        payerEmail: payerEmail || undefined,
        payerDocumentType: payerDocumentType || undefined,
        payerDocumentNumber: payerDocumentNumber || undefined,
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
      message =
        "Você não será cobrado agora. A cobrança será processada quando seu período de teste terminar.";
      highlight = "Seu período de teste continua até o fim.";
    } else if (isPix) {
      title = "Pagamento PIX criado!";
      message = "Escaneie o QR Code para concluir o pagamento.";
      highlight = result.message ?? "Aguarde a confirmação do pagamento.";
    } else {
      message = result.message || message;
    }

    return (
      <div className="px-6 md:px-8 pt-8 md:pt-12 pb-8 flex flex-col items-center">
        <div className="max-w-md w-full text-center">
          <div className="inline-flex items-center justify-center bg-terra/10 rounded-full p-5 mb-6">
            <BadgeCheck size={40} className="text-terra" />
          </div>
          <h1 className="font-serif font-bold text-brown text-3xl mb-3">{title}</h1>
          <p className="text-slate text-sm mb-8">{message}</p>

          {highlight && (
            <div className="bg-terra/10 text-terra rounded-xl px-5 py-3 text-sm font-bold mb-6">
              {highlight}
            </div>
          )}

          {isPix && pixCode && (
            <div className="bg-cream-dark rounded-xl p-5 border border-cream-border text-left mb-8 flex flex-col gap-3">
              <span className="text-brown-soft text-xs uppercase tracking-widest font-bold">
                PIX copia e cola
              </span>
              <div className="flex items-center gap-2">
                <code className="flex-1 bg-cream rounded-lg px-3 py-2 text-xs text-brown break-all">
                  {pixCode}
                </code>
                <button
                  onClick={copyPix}
                  className="p-2 rounded-lg bg-cream border border-cream-border text-brown-soft hover:text-brown"
                >
                  {copied ? <Check size={16} className="text-green-600" /> : <Copy size={16} />}
                </button>
              </div>
            </div>
          )}

          <div className="bg-cream-dark rounded-xl p-5 border border-cream-border text-left mb-8">
            <div className="flex justify-between py-1">
              <span className="text-brown-soft text-sm">Plano</span>
              <span className="text-brown text-sm font-bold">Alexandria Premium</span>
            </div>
            <div className="flex justify-between py-1">
              <span className="text-brown-soft text-sm">Valor</span>
              <span className="text-brown text-sm font-bold">R$ 10,00</span>
            </div>
            <div className="flex justify-between py-1">
              <span className="text-brown-soft text-sm">Método</span>
              <span className="text-brown text-sm font-bold">{isPix ? "PIX" : "Cartão"}</span>
            </div>
            {scheduled && (
              <div className="flex justify-between py-1">
                <span className="text-brown-soft text-sm">Cobrança</span>
                <span className="text-brown text-sm font-bold">Ao fim do teste</span>
              </div>
            )}
          </div>

          <Link
            href="/explorar"
            className="bg-brown text-cream font-bold text-sm tracking-widest uppercase px-6 py-4 rounded-xl hover:bg-brown/90 transition-colors inline-block"
          >
            Ir para a biblioteca
          </Link>
        </div>
      </div>
    );
  }

  return (
    <div className="px-6 md:px-8 pt-8 md:pt-12 pb-8 flex flex-col items-center">
      <div className="max-w-lg w-full">
        <Link
          href="/planos"
          className="inline-flex items-center gap-2 text-brown-soft text-sm font-bold hover:text-brown transition-colors mb-6"
        >
          <ArrowLeft size={16} />
          Voltar para planos
        </Link>

        <h1 className="font-serif font-bold text-brown text-2xl mb-1">Checkout</h1>
        <p className="text-slate text-sm mb-8">Assine o Alexandria Premium.</p>

        <div className="bg-cream-dark rounded-xl p-5 border border-cream-border flex items-center justify-between mb-6">
          <div>
            <span className="text-brown-soft text-xs uppercase tracking-widest font-bold">
              Alexandria Premium
            </span>
            <p className="text-brown text-sm mt-1">
              {inTrial ? "Período de teste ativo" : "Renovação mensal"}
            </p>
          </div>
          <div className="text-right">
            <span className="font-serif font-bold text-brown text-2xl">R$ 10,00</span>
            <span className="text-brown-soft text-xs">/mês</span>
          </div>
        </div>

        {/* Seleção de método */}
        {inTrial ? (
          <div className="mb-6">
            <div className="flex items-center gap-2 rounded-xl px-4 py-3.5 text-sm font-bold bg-brown text-cream border border-brown">
              <CreditCard size={18} />
              Cartão de crédito
            </div>
            <p className="text-terra text-xs font-bold mt-2">
              Durante o teste, o cartão é usado apenas para garantir sua assinatura.
              A cobrança acontece somente após o período de teste.
            </p>
          </div>
        ) : (
          <div className="grid grid-cols-2 gap-3 mb-6">
            <button
              onClick={() => setMethod("pix")}
              className={`flex items-center justify-center gap-2 rounded-xl px-4 py-3.5 text-sm font-bold transition-colors border ${
                method === "pix"
                  ? "bg-brown text-cream border-brown"
                  : "bg-cream-dark text-brown-soft border-cream-border hover:bg-cream-active"
              }`}
            >
              <QrCode size={18} />
              PIX
            </button>
            <button
              onClick={() => setMethod("card")}
              className={`flex items-center justify-center gap-2 rounded-xl px-4 py-3.5 text-sm font-bold transition-colors border ${
                method === "card"
                  ? "bg-brown text-cream border-brown"
                  : "bg-cream-dark text-brown-soft border-cream-border hover:bg-cream-active"
              }`}
            >
              <CreditCard size={18} />
              Cartão
            </button>
          </div>
        )}

        {error && (
          <p className="text-red-600 text-sm bg-red-50 rounded-lg px-4 py-3 mb-4">{error}</p>
        )}

        {/* Painel PIX */}
        {method === "pix" && !inTrial && (
          <div className="bg-cream-dark rounded-2xl p-6 border border-cream-border flex flex-col gap-4">
            <div className="flex items-center gap-2">
              <QrCode size={18} className="text-terra" />
              <span className="text-brown font-bold text-sm">Pagamento via PIX</span>
            </div>

            <label className="flex flex-col gap-1">
              <span className="text-xs font-bold text-brown-soft uppercase tracking-widest">
                E-mail do pagador
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
                "Gerar PIX"
              )}
            </button>
          </div>
        )}

        {/* Painel Cartão */}
        {method === "card" && (
          <form
            onSubmit={handleCard}
            className="bg-cream-dark rounded-2xl p-6 border border-cream-border flex flex-col gap-4"
          >
            <div className="flex items-center gap-2 mb-1">
              <CreditCard size={18} className="text-terra" />
              <span className="text-brown font-bold text-sm">Pagamento com cartão</span>
            </div>

            {inTrial && (
              <p className="text-brown-soft/70 text-xs">
                Você não será cobrado agora. A cobrança acontece quando seu período de
                teste terminar.
              </p>
            )}

            <label className="flex flex-col gap-1">
              <span className="text-xs font-bold text-brown-soft uppercase tracking-widest">
                E-mail do pagador
              </span>
              <input
                type="email"
                value={payerEmail}
                onChange={(e) => setPayerEmail(e.target.value)}
                placeholder="voce@email.com"
                className="bg-cream rounded-lg px-4 py-3 text-brown outline-none border border-cream-border focus:border-terra"
              />
            </label>

            <label className="flex flex-col gap-1">
              <span className="text-xs font-bold text-brown-soft uppercase tracking-widest">
                CardToken (MercadoPago.js)
              </span>
              <input
                type="text"
                value={cardToken}
                onChange={(e) => setCardToken(e.target.value)}
                placeholder="Token gerado pelo CardForm"
                className="bg-cream rounded-lg px-4 py-3 text-brown outline-none border border-cream-border focus:border-terra"
              />
            </label>

            <p className="text-brown-soft/60 text-[11px] flex items-center justify-center gap-1.5">
              <ShieldCheck size={13} />
              Em produção, o MercadoPago.js gera o CardToken a partir do CardForm.
            </p>

            <button
              type="submit"
              disabled={processing || !cardToken}
              className="mt-2 bg-brown text-cream font-bold text-sm tracking-widest uppercase px-6 py-4 rounded-xl hover:bg-brown/90 transition-colors disabled:opacity-50 flex items-center justify-center gap-2"
            >
              {processing ? (
                <>
                  <Loader2 size={16} className="animate-spin" />
                  Processando...
                </>
              ) : inTrial ? (
                "Assinar com cartão"
              ) : (
                "Pagar R$ 10,00"
              )}
            </button>
          </form>
        )}
      </div>
    </div>
  );
}
