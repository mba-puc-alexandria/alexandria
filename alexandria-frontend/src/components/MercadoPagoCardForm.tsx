"use client";

import Script from "next/script";
import { useEffect, useId, useRef, useState } from "react";

export interface MercadoPagoCardData {
  token: string;
  paymentMethodId?: string;
  installments?: number;
  cardholderEmail?: string;
  identificationType?: string;
  identificationNumber?: string;
}

interface MercadoPagoCardFormProps {
  submitLabel: string;
  processing?: boolean;
  onToken: (data: MercadoPagoCardData) => Promise<void> | void;
}

type CardFormController = {
  unmount?: () => void;
  getCardFormData: () => MercadoPagoCardData;
};

type MercadoPagoConstructor = new (publicKey: string, options?: { locale?: string }) => {
  cardForm: (options: Record<string, unknown>) => CardFormController;
};

declare global {
  interface Window {
    MercadoPago?: MercadoPagoConstructor;
  }
}

const publicKey = process.env.NEXT_PUBLIC_MERCADOPAGO_PUBLIC_KEY;
const acceptedBrands = ["Visa", "Mastercard", "American Express", "Elo"];

/** Renders MercadoPago's iframe fields so card PAN/CVV never pass through Alexandria. */
export default function MercadoPagoCardForm({ submitLabel, processing = false, onToken }: MercadoPagoCardFormProps) {
  const formId = `mp-card-form-${useId().replace(/:/g, "")}`;
  const controller = useRef<CardFormController | null>(null);
  const onTokenRef = useRef(onToken);
  const [sdkReady, setSdkReady] = useState(false);
  const [mounted, setMounted] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    onTokenRef.current = onToken;
  }, [onToken]);

  useEffect(() => {
    if (!sdkReady || !publicKey || !window.MercadoPago || controller.current) return;
    const mp = new window.MercadoPago(publicKey, { locale: "pt-BR" });
    controller.current = mp.cardForm({
      amount: "10.00",
      iframe: true,
      form: {
        id: formId,
        cardNumber: { id: `${formId}-number`, placeholder: "Número do cartão" },
        expirationDate: { id: `${formId}-expiration`, placeholder: "MM/AA" },
        securityCode: { id: `${formId}-security`, placeholder: "CVV" },
        cardholderName: { id: `${formId}-holder`, placeholder: "Como impresso no cartão" },
        issuer: { id: `${formId}-issuer`, placeholder: "Banco emissor" },
        installments: { id: `${formId}-installments`, placeholder: "Parcelas" },
        identificationType: { id: `${formId}-document-type`, placeholder: "Tipo" },
        identificationNumber: { id: `${formId}-document-number`, placeholder: "Número do CPF" },
        cardholderEmail: { id: `${formId}-email`, placeholder: "voce@email.com" },
      },
      callbacks: {
        onFormMounted: (mountError: unknown) => {
          if (mountError) {
            setError("Não foi possível carregar o formulário do cartão.");
            return;
          }
          setMounted(true);
        },
        onSubmit: async (event: Event) => {
          event.preventDefault();
          try {
            const data = controller.current?.getCardFormData();
            if (!data?.token) throw new Error("Não foi possível tokenizar o cartão.");
            // Assinaturas são sempre cobradas em uma única parcela. O select continua
            // montado para que o SDK possa concluir a tokenização, mas não é exposto.
            await onTokenRef.current({ ...data, installments: 1 });
          } catch (submitError) {
            setError(submitError instanceof Error ? submitError.message : "Falha ao processar cartão.");
          }
        },
      },
    });
    return () => {
      controller.current?.unmount?.();
      controller.current = null;
    };
  }, [formId, sdkReady]);

  if (!publicKey) {
    return <p className="text-danger text-sm">A chave pública do Mercado Pago não está configurada.</p>;
  }

  return (
    <>
      <Script src="https://sdk.mercadopago.com/js/v2" strategy="afterInteractive" onReady={() => setSdkReady(true)} />
      <form id={formId} className="flex flex-col gap-4">
        <div>
          <label htmlFor={`${formId}-number`} className="mb-1.5 block text-xs font-semibold text-brown-soft">
            Número do cartão
          </label>
          <div id={`${formId}-number`} className="h-12 overflow-hidden rounded-lg border border-cream-border bg-cream px-3 py-3 [&>iframe]:!h-full [&>iframe]:!w-full" />
        </div>
        <div className="grid grid-cols-2 gap-3">
          <div>
            <label htmlFor={`${formId}-expiration`} className="mb-1.5 block text-xs font-semibold text-brown-soft">
              Validade
            </label>
            <div id={`${formId}-expiration`} className="h-12 overflow-hidden rounded-lg border border-cream-border bg-cream px-3 py-3 [&>iframe]:!h-full [&>iframe]:!w-full" />
          </div>
          <div>
            <label htmlFor={`${formId}-security`} className="mb-1.5 block text-xs font-semibold text-brown-soft">
              CVV
            </label>
            <div id={`${formId}-security`} className="h-12 overflow-hidden rounded-lg border border-cream-border bg-cream px-3 py-3 [&>iframe]:!h-full [&>iframe]:!w-full" />
          </div>
        </div>
        <div className="flex flex-wrap items-center gap-2 text-xs text-brown-soft">
          <span className="mr-1 font-semibold">Bandeiras aceitas</span>
          {acceptedBrands.map((brand) => (
            <span key={brand} className="rounded-md border border-cream-border bg-cream px-2 py-1 font-medium text-brown">
              {brand}
            </span>
          ))}
        </div>
        <div>
          <label htmlFor={`${formId}-holder`} className="mb-1.5 block text-xs font-semibold text-brown-soft">
            Nome do titular
          </label>
          <input id={`${formId}-holder`} className="h-12 w-full rounded-lg border border-cream-border bg-cream px-4 text-brown" />
        </div>
        <div>
          <label htmlFor={`${formId}-email`} className="mb-1.5 block text-xs font-semibold text-brown-soft">
            E-mail
          </label>
          <input id={`${formId}-email`} type="email" className="h-12 w-full rounded-lg border border-cream-border bg-cream px-4 text-brown" />
        </div>
        {/* O SDK preenche estes campos a partir do BIN; eles não são escolhas do comprador. */}
        <div className="sr-only" aria-hidden="true">
          <select id={`${formId}-issuer`} />
          <select id={`${formId}-installments`} />
        </div>
        <div>
          <label htmlFor={`${formId}-document-number`} className="mb-1.5 block text-xs font-semibold text-brown-soft">
            Documento do titular
          </label>
          <div className="grid grid-cols-3 gap-3">
            <select id={`${formId}-document-type`} aria-label="Tipo de documento" className="bg-cream rounded-lg px-3 py-3 text-brown border border-cream-border" />
            <input id={`${formId}-document-number`} className="col-span-2 bg-cream rounded-lg px-4 py-3 text-brown border border-cream-border" />
          </div>
        </div>
        {error && <p className="text-danger text-sm">{error}</p>}
        <button type="submit" disabled={!mounted || processing} className="bg-brown text-cream font-bold text-sm tracking-widest uppercase px-6 py-4 rounded-xl disabled:opacity-50">
          {processing ? "Processando..." : mounted ? submitLabel : "Carregando formulário..."}
        </button>
      </form>
    </>
  );
}
