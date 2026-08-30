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

type Method = "pix" | "card";

const PRICE = "10,00";
const PIX_CODE = "00020126580014BR.GOV.BCB.PIX0136alexandria-pagamento-000001520400005303986540510.005802BR5913Alexandria6009SAO PAULO62070503***6304A1B2";

// Protótipo: simula o estado da assinatura.
// No fluxo real, isso vem de GET /subscriptions/me.
const MOCK_IN_TRIAL = true;
const MOCK_TRIAL_DAYS_REMAINING = 15;
const MOCK_FIRST_PAYMENT = true;

function FakeQrCode() {
  // QR Code determinístico (mock visual) para o protótipo
  const size = 21;
  const cells: boolean[] = [];
  let seed = 42;
  for (let i = 0; i < size * size; i++) {
    seed = (seed * 9301 + 49297) % 233280;
    cells.push(seed / 233280 > 0.5);
  }

  return (
    <div className="bg-white p-3 rounded-xl border border-cream-border w-fit">
      <div
        className="grid gap-0"
        style={{ gridTemplateColumns: `repeat(${size}, 6px)` }}
      >
        {cells.map((filled, i) => (
          <div
            key={i}
            className="w-[6px] h-[6px]"
            style={{ backgroundColor: filled ? "#300d00" : "#ffffff" }}
          />
        ))}
      </div>
    </div>
  );
}

export default function CheckoutPage() {
  const [method, setMethod] = useState<Method>(MOCK_IN_TRIAL ? "card" : "pix");
  const [copied, setCopied] = useState(false);
  const [pixPaid, setPixPaid] = useState(false);
  const [cardPaid, setCardPaid] = useState(false);
  const [cardScheduled, setCardScheduled] = useState(false);
  const [processing, setProcessing] = useState(false);

  const [cardNumber, setCardNumber] = useState("");
  const [cardName, setCardName] = useState("");
  const [cardExpiry, setCardExpiry] = useState("");
  const [cardCvv, setCardCvv] = useState("");

  function copyPix() {
    navigator.clipboard?.writeText(PIX_CODE).catch(() => {});
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  }

  function simulatePixApproval() {
    setProcessing(true);
    setTimeout(() => {
      setPixPaid(true);
      setProcessing(false);
    }, 1200);
  }

  function payCard(e: React.FormEvent) {
    e.preventDefault();
    setProcessing(true);
    setTimeout(() => {
      if (MOCK_IN_TRIAL) {
        setCardScheduled(true);
      } else {
        setCardPaid(true);
      }
      setProcessing(false);
    }, 1200);
  }

  // Estado de sucesso (PIX, cartão pós-trial ou cartão agendado durante o trial)
  if (pixPaid || cardPaid || cardScheduled) {
    let title = "Assinatura ativada!";
    let message = "Seu pagamento foi aprovado e sua assinatura está ativa.";
    let highlight: string | null = null;

    if (pixPaid && MOCK_FIRST_PAYMENT) {
      title = "Pagamento realizado!";
      message = "Seu PIX foi aprovado e sua assinatura está ativa.";
      highlight = "Você tem 30 dias até o próximo pagamento.";
    } else if (pixPaid) {
      title = "Pagamento realizado!";
      message = "Sua assinatura foi renovada.";
      highlight = "Você tem 30 dias até o próximo pagamento.";
    } else if (cardScheduled) {
      title = "Assinatura confirmada!";
      message =
        "Você não será cobrado agora. A cobrança será processada quando seu período de teste terminar.";
      highlight = "Seu período de teste continua até o fim.";
    } else {
      // cardPaid (pós-trial)
      title = "Assinatura ativada!";
      message = "Seu pagamento com cartão foi aprovado e sua assinatura está ativa.";
      highlight = "Você tem 30 dias até o próximo pagamento.";
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
              <span className="text-brown text-sm font-bold">
                {pixPaid ? "PIX" : "Cartão"}
              </span>
            </div>
            {cardScheduled && (
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
        {/* Voltar */}
        <Link
          href="/planos"
          className="inline-flex items-center gap-2 text-brown-soft text-sm font-bold hover:text-brown transition-colors mb-6"
        >
          <ArrowLeft size={16} />
          Voltar para planos
        </Link>

        <h1 className="font-serif font-bold text-brown text-2xl mb-1">Checkout</h1>
        <p className="text-slate text-sm mb-8">Escolha como quer assinar o Alexandria Premium.</p>

        {/* Resumo */}
        <div className="bg-cream-dark rounded-xl p-5 border border-cream-border flex items-center justify-between mb-6">
          <div>
            <span className="text-brown-soft text-xs uppercase tracking-widest font-bold">
              Alexandria Premium
            </span>
            <p className="text-brown text-sm mt-1">
              {MOCK_IN_TRIAL
                ? `${MOCK_TRIAL_DAYS_REMAINING} dias restantes de teste`
                : "15 dias grátis + renovação mensal"}
            </p>
          </div>
          <div className="text-right">
            <span className="font-serif font-bold text-brown text-2xl">R$ {PRICE}</span>
            <span className="text-brown-soft text-xs">/mês</span>
          </div>
        </div>

        {/* Seleção de método */}
        {MOCK_IN_TRIAL ? (
          <div className="mb-6">
            <div className="flex items-center gap-2 rounded-xl px-4 py-3.5 text-sm font-bold bg-brown text-cream border border-brown">
              <CreditCard size={18} />
              Cartão de crédito
            </div>
            <p className="text-terra text-xs font-bold mt-2">
              Durante o teste, o cartão é usado apenas para garantir sua assinatura.
              A cobrança acontece somente após os {MOCK_TRIAL_DAYS_REMAINING} dias de teste.
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

        {/* Painel PIX */}
        {method === "pix" && (
          <div className="bg-cream-dark rounded-2xl p-6 border border-cream-border flex flex-col items-center">
            <div className="flex items-center gap-2 mb-2">
              <QrCode size={18} className="text-terra" />
              <span className="text-brown font-bold text-sm">
                Pague com PIX escaneando o QR Code
              </span>
            </div>

            <FakeQrCode />

            <p className="text-brown-soft text-xs text-center mt-4 mb-2">
              Ou use o código copia-e-cola:
            </p>

            <button
              onClick={copyPix}
              className="flex items-center gap-2 bg-cream rounded-lg border border-cream-border px-4 py-2.5 text-brown text-xs font-mono w-full justify-between hover:bg-cream-active transition-colors"
            >
              <span className="truncate">{PIX_CODE.slice(0, 40)}...</span>
              {copied ? (
                <Check size={16} className="text-green-600 shrink-0" />
              ) : (
                <Copy size={16} className="text-brown-soft shrink-0" />
              )}
            </button>

            {copied && (
              <p className="text-green-700 text-xs mt-2">Código copiado!</p>
            )}

            {/* Protótipo: simular aprovação do PIX */}
            <button
              onClick={simulatePixApproval}
              disabled={processing}
              className="mt-6 w-full bg-brown text-cream font-bold text-xs tracking-widest uppercase px-6 py-3 rounded-xl hover:bg-brown/90 transition-colors disabled:opacity-50 flex items-center justify-center gap-2"
            >
              {processing ? (
                <>
                  <Loader2 size={16} className="animate-spin" />
                  Verificando pagamento...
                </>
              ) : (
                "Simular pagamento aprovado (protótipo)"
              )}
            </button>
            <p className="text-brown-soft/60 text-[11px] mt-3">
              No fluxo real, a aprovação chega via webhook do Mercado Pago.
            </p>
          </div>
        )}

        {/* Painel Cartão */}
        {method === "card" && (
          <form
            onSubmit={payCard}
            className="bg-cream-dark rounded-2xl p-6 border border-cream-border flex flex-col gap-4"
          >
            <div className="flex items-center gap-2 mb-1">
              <CreditCard size={18} className="text-terra" />
              <span className="text-brown font-bold text-sm">
                Pagamento com cartão
              </span>
            </div>

            {MOCK_IN_TRIAL && (
              <p className="text-brown-soft/70 text-xs">
                Você não será cobrado agora. A cobrança acontece quando seu período de
                teste terminar.
              </p>
            )}

            <label className="flex flex-col gap-1">
              <span className="text-xs font-bold text-brown-soft uppercase tracking-widest">
                Número do cartão
              </span>
              <input
                type="text"
                inputMode="numeric"
                value={cardNumber}
                onChange={(e) => setCardNumber(e.target.value)}
                placeholder="0000 0000 0000 0000"
                className="bg-cream rounded-lg px-4 py-3 text-brown outline-none border border-cream-border focus:border-terra"
              />
            </label>

            <label className="flex flex-col gap-1">
              <span className="text-xs font-bold text-brown-soft uppercase tracking-widest">
                Nome impresso no cartão
              </span>
              <input
                type="text"
                value={cardName}
                onChange={(e) => setCardName(e.target.value)}
                placeholder="Como está no cartão"
                className="bg-cream rounded-lg px-4 py-3 text-brown outline-none border border-cream-border focus:border-terra"
              />
            </label>

            <div className="grid grid-cols-2 gap-4">
              <label className="flex flex-col gap-1">
                <span className="text-xs font-bold text-brown-soft uppercase tracking-widest">
                  Validade
                </span>
                <input
                  type="text"
                  inputMode="numeric"
                  value={cardExpiry}
                  onChange={(e) => setCardExpiry(e.target.value)}
                  placeholder="MM/AA"
                  className="bg-cream rounded-lg px-4 py-3 text-brown outline-none border border-cream-border focus:border-terra"
                />
              </label>
              <label className="flex flex-col gap-1">
                <span className="text-xs font-bold text-brown-soft uppercase tracking-widest">
                  CVV
                </span>
                <input
                  type="password"
                  inputMode="numeric"
                  maxLength={4}
                  value={cardCvv}
                  onChange={(e) => setCardCvv(e.target.value)}
                  placeholder="•••"
                  className="bg-cream rounded-lg px-4 py-3 text-brown outline-none border border-cream-border focus:border-terra"
                />
              </label>
            </div>

            <button
              type="submit"
              disabled={processing}
              className="mt-2 bg-brown text-cream font-bold text-sm tracking-widest uppercase px-6 py-4 rounded-xl hover:bg-brown/90 transition-colors disabled:opacity-50 flex items-center justify-center gap-2"
            >
              {processing ? (
                <>
                  <Loader2 size={16} className="animate-spin" />
                  Processando...
                </>
              ) : MOCK_IN_TRIAL ? (
                "Assinar com cartão"
              ) : (
                "Pagar R$ 10,00"
              )}
            </button>

            <p className="text-brown-soft/60 text-[11px] flex items-center justify-center gap-1.5">
              <ShieldCheck size={13} />
              Protótipo — no fluxo real, o cartão vira CardToken via MercadoPago.js.
            </p>
          </form>
        )}
      </div>
    </div>
  );
}
