import { beforeEach, describe, expect, it, vi } from "vitest";
import {
  cancelSubscription,
  createCheckout,
  getAuthHeaders,
  getProfile,
  getSubscription,
  login,
  loginWithGoogle,
  register,
  updatePassword,
  updateProfile,
  updateSubscriptionPaymentMethod,
} from "./api";

const fetchMock = vi.fn();

function response(body: unknown = {}, status = 200) {
  return new Response(JSON.stringify(body), {
    status,
    headers: { "Content-Type": "application/json" },
  });
}

describe("API de assinatura", () => {
  beforeEach(() => {
    localStorage.clear();
    fetchMock.mockReset();
    vi.stubGlobal("fetch", fetchMock);
    localStorage.setItem("auth-token", "jwt-de-teste");
  });

  it("envia o JWT e os contratos de perfil e assinatura", async () => {
    fetchMock
      .mockResolvedValueOnce(response({ email: "leitora@example.com" }))
      .mockResolvedValueOnce(response({ username: "leitora" }))
      .mockResolvedValueOnce(response())
      .mockResolvedValueOnce(response({ status: "TRIALING" }))
      .mockResolvedValueOnce(response({ status: "PENDING", paymentId: "p1" }))
      .mockResolvedValueOnce(response())
      .mockResolvedValueOnce(response());

    await getProfile();
    await updateProfile({ username: "leitora", firstName: "Leitora", lastName: "Teste" });
    await updatePassword({ currentPassword: "senha-antiga", newPassword: "senha-nova" });
    await getSubscription();
    await createCheckout({ paymentMethod: "CARD", cardToken: "token-seguro" });
    await cancelSubscription();
    await updateSubscriptionPaymentMethod({ cardToken: "novo-token", cardBrand: "visa" });

    expect(getAuthHeaders()).toEqual({ Authorization: "Bearer jwt-de-teste" });
    expect(fetchMock).toHaveBeenCalledTimes(7);
    expect(fetchMock.mock.calls[4][1]).toEqual(expect.objectContaining({
      method: "POST",
      body: JSON.stringify({ paymentMethod: "CARD", cardToken: "token-seguro" }),
    }));
    expect(fetchMock.mock.calls[6][1]).toEqual(expect.objectContaining({
      method: "POST",
      body: JSON.stringify({ cardToken: "novo-token", cardBrand: "visa" }),
    }));
  });

  it("preserva as mensagens de erro úteis da API", async () => {
    fetchMock
      .mockResolvedValueOnce(response({ message: "Cartão recusado" }, 400))
      .mockResolvedValueOnce(response({}, 404))
      .mockResolvedValueOnce(response({}, 401))
      .mockResolvedValueOnce(response({}, 401))
      .mockResolvedValueOnce(response({ message: "E-mail já cadastrado" }, 409));

    await expect(createCheckout({ paymentMethod: "CARD", cardToken: "token" }))
      .rejects.toThrow("Cartão recusado");
    await expect(getSubscription()).rejects.toThrow("subscription_not_found");
    await expect(login({ username: "leitora", password: "senha" })).rejects.toThrow("Usuário ou senha incorretos");
    await expect(loginWithGoogle("credential")).rejects.toThrow("Falha no login com Google");
    await expect(register({
      username: "leitora",
      firstName: "Leitora",
      lastName: "Teste",
      email: "leitora@example.com",
      password: "senha",
    })).rejects.toThrow("E-mail já cadastrado");
  });
});
