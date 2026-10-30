import { act } from "react";
import { beforeEach, describe, expect, it, vi } from "vitest";
import SuportePage from "./page";
import { change, click, render, submit } from "@/test-utils/render";

const mocks = vi.hoisted(() => ({
  auth: { user: null as { userId: number; username: string } | null },
  openLoginModal: vi.fn(),
  sendSupportMessage: vi.fn(),
}));

vi.mock("@/contexts/AuthContext", () => ({
  useAuth: () => mocks.auth,
}));

vi.mock("@/contexts/AuthModalContext", () => ({
  useAuthModal: () => ({ openLoginModal: mocks.openLoginModal }),
}));

vi.mock("@/lib/api", () => ({
  sendSupportMessage: mocks.sendSupportMessage,
}));

async function renderLoggedIn() {
  mocks.auth.user = { userId: 7, username: "leitora" };
  const screen = await render(<SuportePage />);
  const input = screen.container.querySelector<HTMLInputElement>('input[aria-label="Mensagem para o assistente"]')!;
  const form = screen.container.querySelector("form")!;
  const send = screen.container.querySelector<HTMLButtonElement>('button[aria-label="Enviar"]')!;
  return { screen, input, form, send };
}

describe("SuportePage — assistente virtual", () => {
  beforeEach(() => {
    mocks.auth.user = null;
    mocks.openLoginModal.mockReset();
    mocks.sendSupportMessage.mockReset();
  });

  it("avisa que é uma IA e pede login ao visitante", async () => {
    const screen = await render(<SuportePage />);

    expect(screen.container).toHaveTextContent("Você está falando com uma IA");
    expect(screen.container.querySelector("form")).toBeNull();

    const loginButton = [...screen.container.querySelectorAll("button")]
      .find((b) => b.textContent?.includes("Entre na sua conta"))!;
    await click(loginButton);

    expect(mocks.openLoginModal).toHaveBeenCalledOnce();
  });

  it("envia a pergunta com o histórico e mostra a resposta", async () => {
    mocks.sendSupportMessage
      .mockResolvedValueOnce("Sim, temos Dracula.")
      .mockResolvedValueOnce("É de Bram Stoker.");
    const { screen, input, form } = await renderLoggedIn();

    await change(input, "  Vocês têm Dracula?  ");
    await submit(form);

    expect(mocks.sendSupportMessage).toHaveBeenCalledWith([
      { role: "user", content: "Vocês têm Dracula?" },
    ]);
    expect(screen.container).toHaveTextContent("Sim, temos Dracula.");
    expect(input.value).toBe("");

    await change(input, "Quem escreveu?");
    await submit(form);

    // O id usado como key da lista não vai para a API.
    expect(mocks.sendSupportMessage).toHaveBeenLastCalledWith([
      { role: "user", content: "Vocês têm Dracula?" },
      { role: "assistant", content: "Sim, temos Dracula." },
      { role: "user", content: "Quem escreveu?" },
    ]);
    expect(screen.container).toHaveTextContent("É de Bram Stoker.");
  });

  it("não envia mensagem vazia e bloqueia o botão durante o envio", async () => {
    let resolveReply: (reply: string) => void = () => {};
    mocks.sendSupportMessage.mockReturnValueOnce(new Promise<string>((resolve) => { resolveReply = resolve; }));
    const { screen, input, form, send } = await renderLoggedIn();

    expect(send.disabled).toBe(true);
    await change(input, "   ");
    await submit(form);
    expect(mocks.sendSupportMessage).not.toHaveBeenCalled();

    await change(input, "Olá");
    expect(send.disabled).toBe(false);
    await submit(form);

    expect(screen.container).toHaveTextContent("Digitando…");
    expect(send.disabled).toBe(true);

    await act(async () => resolveReply("Oi! Como posso ajudar?"));

    expect(screen.container).not.toHaveTextContent("Digitando…");
    expect(screen.container).toHaveTextContent("Oi! Como posso ajudar?");
  });

  it("mostra o erro da API na conversa e indica o contato humano", async () => {
    mocks.sendSupportMessage.mockRejectedValueOnce(new Error("Sessão expirada. Entre de novo."));
    const { screen, input, form } = await renderLoggedIn();

    await change(input, "Minha assinatura vence quando?");
    await submit(form);

    expect(screen.container).toHaveTextContent(
      "Não consegui responder agora (Sessão expirada. Entre de novo.). Tente de novo ou use o e-mail de contato abaixo.",
    );
  });
});
