import type { AnchorHTMLAttributes } from "react";
import { describe, expect, it, beforeEach, vi } from "vitest";
import CheckoutPage from "./page";
import { change, click, render } from "@/test-utils/render";

const mocks = vi.hoisted(() => ({
  auth: {
    subscription: null as {
      status: string;
      price?: number;
      currency?: string;
      periodDays?: number;
      trialEndsAt?: string | null;
    } | null,
  },
  createCheckout: vi.fn(),
}));

vi.mock("next/link", () => ({
  default: ({ children, ...props }: AnchorHTMLAttributes<HTMLAnchorElement>) => (
    <a {...props}>{children}</a>
  ),
}));

vi.mock("@/contexts/AuthContext", () => ({
  useAuth: () => mocks.auth,
}));

vi.mock("@/lib/api", () => ({
  createCheckout: mocks.createCheckout,
}));

vi.mock("@/components/MercadoPagoCardForm", () => ({
  default: ({ submitLabel, onToken }: { submitLabel: string; onToken: (data: { token: string }) => Promise<void> }) => (
    <button type="button" onClick={() => void onToken({ token: "token-test" })}>
      {submitLabel}
    </button>
  ),
}));

async function settle() {
  await new Promise((resolve) => setTimeout(resolve, 0));
}

describe("CheckoutPage", () => {
  beforeEach(() => {
    mocks.createCheckout.mockReset();
    mocks.auth.subscription = null;
  });

  it("mantém cartão como único método durante o trial e deixa a cobrança clara", async () => {
    mocks.auth.subscription = {
      status: "TRIALING",
      price: 10,
      currency: "BRL",
      trialEndsAt: "2026-09-28T12:00:00Z",
    };

    const screen = await render(<CheckoutPage />);

    expect(screen.container).toHaveTextContent("Confirme o pagamento de R$ 10,00 após o término do seu teste.");
    expect(screen.container).toHaveTextContent("Cobrança após o teste");
    expect(screen.container).toHaveTextContent("Confirmar pagamento após o teste");
    expect(screen.container).not.toHaveTextContent("Pagamento via PIX");

    await screen.unmount();
  });

  it("confirma a assinatura sem cobrar durante o trial", async () => {
    mocks.auth.subscription = {
      status: "TRIALING",
      price: 10,
      currency: "BRL",
      trialEndsAt: "2026-09-28T12:00:00Z",
    };
    mocks.createCheckout.mockResolvedValue({
      subscriptionStatus: "TRIALING",
      message: "Cobrança agendada",
    });

    const screen = await render(<CheckoutPage />);
    await click(screen.container.querySelector("button")!);
    await settle();

    expect(mocks.createCheckout).toHaveBeenCalledWith(expect.objectContaining({
      paymentMethod: "CARD",
      cardToken: "token-test",
    }));
    expect(screen.container).toHaveTextContent("Assinatura confirmada!");
    expect(screen.container).toHaveTextContent("Você não foi cobrado agora.");

    await screen.unmount();
  });

  it("gera PIX para uma assinatura fora do trial", async () => {
    mocks.auth.subscription = {
      status: "EXPIRED",
      price: 10,
      currency: "BRL",
      periodDays: 30,
    };
    mocks.createCheckout.mockResolvedValue({
      subscriptionStatus: "PENDING",
      qrCode: "pix-copia-e-cola",
      qrCodeBase64: "ZmFrZQ==",
      message: "Aguardando pagamento",
    });

    const screen = await render(<CheckoutPage />);
    const input = screen.container.querySelector('input[type="email"]') as HTMLInputElement;
    await change(input, "comprador@example.com");
    await click([...screen.container.querySelectorAll("button")].find((button) => button.textContent?.includes("Gerar PIX"))!);
    await settle();

    expect(mocks.createCheckout).toHaveBeenCalledWith(expect.objectContaining({
      paymentMethod: "PIX",
      payerEmail: "comprador@example.com",
    }));
    expect(screen.container).toHaveTextContent("Falta pouco: pague o PIX");
    expect(screen.container).toHaveTextContent("pix-copia-e-cola");

    await screen.unmount();
  });

  it("mostra falha do PIX e permite selecionar cartão fora do trial", async () => {
    mocks.auth.subscription = { status: "PAST_DUE", price: 10, currency: "BRL", periodDays: 30 };
    mocks.createCheckout.mockRejectedValue(new Error("PIX indisponível"));
    const screen = await render(<CheckoutPage />);

    expect(screen.container).toHaveTextContent("Regularize o pagamento para continuar lendo.");
    await click([...screen.container.querySelectorAll("button")].find((button) => button.textContent?.includes("Gerar PIX"))!);
    await settle();
    expect(screen.container).toHaveTextContent("PIX indisponível");

    await click([...screen.container.querySelectorAll("button")].find((button) => button.textContent?.includes("Cartão"))!);
    expect(screen.container).toHaveTextContent("Pagar R$ 10,00");
    await screen.unmount();
  });
});
