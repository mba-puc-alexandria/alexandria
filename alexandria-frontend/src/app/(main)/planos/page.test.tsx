import type { AnchorHTMLAttributes } from "react";
import { beforeEach, describe, expect, it, vi } from "vitest";
import PlanosPage from "./page";
import { render } from "@/test-utils/render";

const mocks = vi.hoisted(() => ({
  auth: {
    user: null as { userId: number; username: string } | null,
    subscription: null as {
      status: string;
      trialEndsAt?: string | null;
      currentPeriodEndsAt?: string | null;
    } | null,
    isLoading: false,
  },
}));

vi.mock("next/link", () => ({
  default: ({ children, ...props }: AnchorHTMLAttributes<HTMLAnchorElement>) => (
    <a {...props}>{children}</a>
  ),
}));

vi.mock("@/contexts/AuthContext", () => ({
  useAuth: () => mocks.auth,
}));

describe("PlanosPage", () => {
  beforeEach(() => {
    mocks.auth.user = null;
    mocks.auth.subscription = null;
    mocks.auth.isLoading = false;
  });

  it("oferece o teste grátis para visitante sem prometer cobrança imediata", async () => {
    const screen = await render(<PlanosPage />);

    expect(screen.container).toHaveTextContent("Continue sua leitura sem interrupções");
    expect(screen.container).toHaveTextContent("15 dias grátis");
    expect(screen.container).toHaveTextContent("nada é cobrado agora");
    expect(screen.container.querySelector('a[href="/registrar"]')).toHaveTextContent("Criar conta grátis");

    await screen.unmount();
  });

  it("mostra o trial ativo e direciona para o gerenciamento da assinatura", async () => {
    mocks.auth.user = { userId: 1, username: "leitora" };
    mocks.auth.subscription = {
      status: "TRIALING",
      trialEndsAt: "2026-09-28T12:00:00Z",
    };

    const screen = await render(<PlanosPage />);

    expect(screen.container).toHaveTextContent("Seu teste Alexandria Premium está ativo");
    expect(screen.container).toHaveTextContent("Teste gratuito até 28/09/2026");
    expect(screen.container).toHaveTextContent("A primeira cobrança acontece apenas após o teste.");
    expect(screen.container.querySelector('a[href="/configuracoes"]')).toHaveTextContent("Gerenciar assinatura");

    await screen.unmount();
  });

  it("explica renovação, pendência e assinatura encerrada conforme o status", async () => {
    mocks.auth.user = { userId: 1, username: "leitora" };
    mocks.auth.subscription = { status: "ACTIVE", currentPeriodEndsAt: "2026-10-28T12:00:00Z" };
    const screen = await render(<PlanosPage />);

    expect(screen.container).toHaveTextContent("Seu Alexandria Premium está ativo");
    expect(screen.container).toHaveTextContent("Renovação mensal em 28/10/2026");

    mocks.auth.subscription = { status: "PAST_DUE" };
    await screen.rerender(<PlanosPage />);
    expect(screen.container).toHaveTextContent("Regularize seu pagamento para continuar lendo");
    expect(screen.container.querySelector('a[href="/checkout"]')).toHaveTextContent("Regularizar pagamento");

    mocks.auth.subscription = { status: "CANCELED" };
    await screen.rerender(<PlanosPage />);
    expect(screen.container).toHaveTextContent("Sua assinatura terminou");
    expect(screen.container).toHaveTextContent("Sua biblioteca e seu progresso continuam salvos.");

    await screen.unmount();
  });
});
