import { act } from "react";
import { describe, expect, it, vi, afterEach } from "vitest";
import { click, render } from "@/test-utils/render";

const mocks = vi.hoisted(() => ({
  cardFormOptions: null as Record<string, unknown> | null,
  unmount: vi.fn(),
}));

vi.mock("next/script", () => ({
  default: ({ onReady }: { onReady?: () => void }) => (
    <button type="button" onClick={onReady}>Carregar SDK Mercado Pago</button>
  ),
}));

async function settle() {
  await new Promise((resolve) => setTimeout(resolve, 0));
}

describe("MercadoPagoCardForm", () => {
  afterEach(() => {
    vi.unstubAllEnvs();
    vi.resetModules();
    delete window.MercadoPago;
    mocks.cardFormOptions = null;
    mocks.unmount.mockReset();
  });

  it("informa quando a chave pública não foi configurada", async () => {
    vi.stubEnv("NEXT_PUBLIC_MERCADOPAGO_PUBLIC_KEY", "");
    const { default: MercadoPagoCardForm } = await import("./MercadoPagoCardForm");
    const screen = await render(<MercadoPagoCardForm submitLabel="Pagar" onToken={vi.fn()} />);

    expect(screen.container).toHaveTextContent("A chave pública do Mercado Pago não está configurada.");
    await screen.unmount();
  });

  it("monta campos seguros, entrega somente o token e desmonta o SDK", async () => {
    vi.stubEnv("NEXT_PUBLIC_MERCADOPAGO_PUBLIC_KEY", "TEST-public-key");
    window.MercadoPago = class {
      cardForm(options: Record<string, unknown>) {
        mocks.cardFormOptions = options;
        const callbacks = options.callbacks as { onFormMounted: (error: unknown) => void };
        callbacks.onFormMounted(null);
        return {
          getCardFormData: () => ({ token: "token-seguro", paymentMethodId: "visa" }),
          unmount: mocks.unmount,
        };
      }
    } as never;

    const onToken = vi.fn();
    const { default: MercadoPagoCardForm } = await import("./MercadoPagoCardForm");
    const screen = await render(<MercadoPagoCardForm submitLabel="Confirmar pagamento" onToken={onToken} />);

    await click(screen.container.querySelector("button")!);
    await settle();

    expect(screen.container).toHaveTextContent("Número do cartão");
    expect(screen.container).toHaveTextContent("Bandeiras aceitas");
    expect(screen.container).toHaveTextContent("Confirmar pagamento");

    const callbacks = mocks.cardFormOptions?.callbacks as {
      onSubmit: (event: { preventDefault: () => void }) => Promise<void>;
    };
    const preventDefault = vi.fn();
    await callbacks.onSubmit({ preventDefault });

    expect(preventDefault).toHaveBeenCalledOnce();
    expect(onToken).toHaveBeenCalledWith({
      token: "token-seguro",
      paymentMethodId: "visa",
      installments: 1,
    });

    await screen.unmount();
    expect(mocks.unmount).toHaveBeenCalledOnce();
  });

  it("expõe erros de montagem e de tokenização sem vazar dados do cartão", async () => {
    vi.stubEnv("NEXT_PUBLIC_MERCADOPAGO_PUBLIC_KEY", "TEST-public-key");
    window.MercadoPago = class {
      cardForm(options: Record<string, unknown>) {
        mocks.cardFormOptions = options;
        const callbacks = options.callbacks as { onFormMounted: (error: unknown) => void };
        callbacks.onFormMounted(new Error("SDK indisponível"));
        return {
          getCardFormData: () => ({}),
          unmount: mocks.unmount,
        };
      }
    } as never;

    const { default: MercadoPagoCardForm } = await import("./MercadoPagoCardForm");
    const screen = await render(<MercadoPagoCardForm submitLabel="Confirmar" onToken={vi.fn()} />);
    await click(screen.container.querySelector("button")!);
    await settle();

    expect(screen.container).toHaveTextContent("Não foi possível carregar o formulário do cartão.");
    const callbacks = mocks.cardFormOptions?.callbacks as {
      onSubmit: (event: { preventDefault: () => void }) => Promise<void>;
    };
    await act(async () => {
      await callbacks.onSubmit({ preventDefault: vi.fn() });
    });
    expect(screen.container).toHaveTextContent("Não foi possível tokenizar o cartão.");
    await screen.unmount();
  });
});
