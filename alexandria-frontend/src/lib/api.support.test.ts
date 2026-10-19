import { beforeEach, describe, expect, it, vi } from "vitest";
import { sendSupportMessage, type SupportMessage } from "./api";

const fetchMock = vi.fn();

function response(body: unknown = {}, status = 200) {
  return new Response(JSON.stringify(body), {
    status,
    headers: { "Content-Type": "application/json" },
  });
}

const conversa: SupportMessage[] = [{ role: "user", content: "Vocês têm Dracula?" }];

describe("API do assistente de suporte", () => {
  beforeEach(() => {
    localStorage.clear();
    fetchMock.mockReset();
    vi.stubGlobal("fetch", fetchMock);
    localStorage.setItem("auth-token", "jwt-de-teste");
  });

  it("envia a conversa com o JWT e devolve a resposta do agente", async () => {
    fetchMock.mockResolvedValueOnce(response({ reply: "Sim, temos Dracula." }));

    const reply = await sendSupportMessage(conversa);

    expect(reply).toBe("Sim, temos Dracula.");
    const [url, init] = fetchMock.mock.calls[0];
    expect(url).toMatch(/\/support\/chat$/);
    expect(init).toEqual(expect.objectContaining({
      method: "POST",
      body: JSON.stringify({ messages: conversa }),
    }));
    expect(init.headers).toEqual(expect.objectContaining({ Authorization: "Bearer jwt-de-teste" }));
  });

  it("traduz 401 em sessão expirada e outros erros no código HTTP", async () => {
    fetchMock
      .mockResolvedValueOnce(response({}, 401))
      .mockResolvedValueOnce(response({}, 500));

    await expect(sendSupportMessage(conversa)).rejects.toThrow("Sessão expirada. Entre de novo.");
    await expect(sendSupportMessage(conversa)).rejects.toThrow("Falha ao falar com o assistente (erro 500)");
  });
});
