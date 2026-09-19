import type { AnchorHTMLAttributes } from "react";
import { beforeEach, describe, expect, it, vi } from "vitest";
import ConfiguracoesPage from "./page";
import { change, click, render, submit } from "@/test-utils/render";

const mocks = vi.hoisted(() => ({
  auth: {
    subscription: {
      status: "TRIALING",
      trialEndsAt: "2026-09-28T12:00:00Z",
    } as { status: string; trialEndsAt?: string; currentPeriodEndsAt?: string } | null,
    updateUsername: vi.fn(),
    refreshSubscription: vi.fn(),
  },
  getProfile: vi.fn(),
  updateProfile: vi.fn(),
  updatePassword: vi.fn(),
  cancelSubscription: vi.fn(),
  updateSubscriptionPaymentMethod: vi.fn(),
}));

vi.mock("next/link", () => ({
  default: ({ children, ...props }: AnchorHTMLAttributes<HTMLAnchorElement>) => (
    <a {...props}>{children}</a>
  ),
}));

vi.mock("@/contexts/AuthContext", () => ({
  useAuth: () => mocks.auth,
}));

vi.mock("@/components/MercadoPagoCardForm", () => ({
  default: ({ onToken }: { onToken: (data: { token: string; paymentMethodId: string }) => Promise<void> }) => (
    <button type="button" onClick={() => void onToken({ token: "token-novo", paymentMethodId: "visa" })}>
      Atualizar cartão
    </button>
  ),
}));

vi.mock("@/lib/api", () => ({
  getProfile: mocks.getProfile,
  updateProfile: mocks.updateProfile,
  updatePassword: mocks.updatePassword,
  cancelSubscription: mocks.cancelSubscription,
  updateSubscriptionPaymentMethod: mocks.updateSubscriptionPaymentMethod,
}));

async function settle() {
  await new Promise((resolve) => setTimeout(resolve, 0));
}

describe("ConfiguracoesPage", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mocks.auth.subscription = { status: "TRIALING", trialEndsAt: "2026-09-28T12:00:00Z" };
    mocks.getProfile.mockResolvedValue({
      userId: 1,
      email: "leitora@example.com",
      username: "leitora",
      firstName: "Leitora",
      lastName: "Teste",
      createdAt: "2026-01-01T00:00:00Z",
    });
    mocks.updateProfile.mockResolvedValue({
      userId: 1,
      email: "leitora@example.com",
      username: "leitora-atualizada",
      firstName: "Leitora",
      lastName: "Teste",
      createdAt: "2026-01-01T00:00:00Z",
    });
    mocks.cancelSubscription.mockResolvedValue(undefined);
    mocks.auth.refreshSubscription.mockResolvedValue(undefined);
  });

  it("carrega perfil, descreve o trial e permite cancelar a assinatura", async () => {
    const screen = await render(<ConfiguracoesPage />);
    await settle();

    expect(screen.container).toHaveTextContent("Período de teste");
    expect(screen.container).toHaveTextContent("Seu teste gratuito termina em 28/09/2026");
    expect(screen.container).toHaveTextContent("leitora@example.com");

    await click([...screen.container.querySelectorAll("button")].find((button) => button.textContent === "Cancelar assinatura")!);
    await settle();

    expect(mocks.cancelSubscription).toHaveBeenCalledOnce();
    expect(mocks.auth.refreshSubscription).toHaveBeenCalledOnce();
    await screen.unmount();
  });

  it("atualiza os dados do perfil", async () => {
    const screen = await render(<ConfiguracoesPage />);
    await settle();

    await submit(screen.container.querySelectorAll("form")[0]);
    await settle();

    expect(mocks.updateProfile).toHaveBeenCalledWith({
      username: "leitora",
      firstName: "Leitora",
      lastName: "Teste",
    });
    expect(mocks.auth.updateUsername).toHaveBeenCalledWith("leitora-atualizada");
    expect(screen.container).toHaveTextContent("Perfil atualizado com sucesso.");
    await screen.unmount();
  });

  it("troca o cartão e valida a nova senha antes de enviar", async () => {
    mocks.updateSubscriptionPaymentMethod.mockResolvedValue(undefined);
    const screen = await render(<ConfiguracoesPage />);
    await settle();

    await click([...screen.container.querySelectorAll("button")].find((button) => button.textContent === "Atualizar cartão")!);
    await settle();
    expect(mocks.updateSubscriptionPaymentMethod).toHaveBeenCalledWith({ cardToken: "token-novo", cardBrand: "visa" });
    expect(screen.container).toHaveTextContent("Cartão atualizado com segurança.");

    const passwordInputs = screen.container.querySelectorAll('input[type="password"]') as NodeListOf<HTMLInputElement>;
    await change(passwordInputs[0], "atual");
    await change(passwordInputs[1], "curta");
    await change(passwordInputs[2], "curta");
    await submit(screen.container.querySelectorAll("form")[1]);
    expect(screen.container).toHaveTextContent("A nova senha deve ter pelo menos 8 caracteres.");

    await screen.unmount();
  });
});
