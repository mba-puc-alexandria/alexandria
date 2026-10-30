import { act, useEffect } from "react";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { AuthProvider, useAuth } from "./AuthContext";
import { click, render } from "@/test-utils/render";

const mocks = vi.hoisted(() => ({
  push: vi.fn(),
  refresh: vi.fn(),
  login: vi.fn(),
  loginWithGoogle: vi.fn(),
  getSubscription: vi.fn(),
}));

vi.mock("next/navigation", () => ({
  useRouter: () => ({ push: mocks.push, refresh: mocks.refresh }),
}));

vi.mock("@/lib/api", () => ({
  login: mocks.login,
  loginWithGoogle: mocks.loginWithGoogle,
  getSubscription: mocks.getSubscription,
}));

function AuthHarness() {
  const auth = useAuth();

  useEffect(() => {
    document.body.dataset.loading = String(auth.isLoading);
  }, [auth.isLoading]);

  return (
    <>
      <p>{auth.user?.username ?? "visitante"}</p>
      <p>{auth.subscription?.status ?? "sem assinatura"}</p>
      <button type="button" onClick={() => void auth.login({ username: "leitora", password: "senha" })}>
        Entrar
      </button>
      <button type="button" onClick={() => auth.updateUsername("novo-nome")}>Renomear</button>
      <button type="button" onClick={auth.logout}>Sair</button>
      <button type="button" onClick={() => void auth.refreshSubscription()}>Atualizar assinatura</button>
    </>
  );
}

async function settle() {
  await new Promise((resolve) => setTimeout(resolve, 0));
}

describe("AuthProvider", () => {
  beforeEach(() => {
    localStorage.clear();
    vi.clearAllMocks();
    mocks.login.mockResolvedValue({ token: "jwt-de-teste", userId: 7, username: "leitora" });
    mocks.getSubscription.mockResolvedValue({ status: "TRIALING" });
  });

  it('sincroniza troca de conta e logout vindos de outra aba', async () => {
    localStorage.setItem('auth-token', 'token-da-conta-1');
    localStorage.setItem('auth-user', JSON.stringify({ userId: 1, username: 'conta-1' }));
    const screen = await render(<AuthProvider><AuthHarness /></AuthProvider>);
    await act(async () => {
      localStorage.setItem('auth-token', 'token-da-conta-2');
      localStorage.setItem('auth-user', JSON.stringify({ userId: 2, username: 'conta-2' }));
      window.dispatchEvent(new StorageEvent('storage', { key: 'auth-user', newValue: localStorage.getItem('auth-user') }));
    });
    expect(screen.container).toHaveTextContent('conta-2');
    expect(screen.container).not.toHaveTextContent('conta-1');
    await act(async () => {
      localStorage.removeItem('auth-token');
      localStorage.removeItem('auth-user');
      window.dispatchEvent(new StorageEvent('storage', { key: 'auth-user', newValue: null }));
    });
    expect(screen.container).toHaveTextContent('visitante');
    await screen.unmount();
  });

  it('ignora assinatura atrasada de A após troca para B e depois do logout', async () => {
    localStorage.setItem('auth-token', 'token-a');
    localStorage.setItem('auth-user', JSON.stringify({ userId: 1, username: 'conta-a' }));
    let finishA!: (value: unknown) => void;
    let finishB!: (value: unknown) => void;
    mocks.getSubscription.mockImplementationOnce(() => new Promise(resolve => { finishA = resolve; }));
    mocks.getSubscription.mockImplementationOnce(() => new Promise(resolve => { finishB = resolve; }));
    const screen = await render(<AuthProvider><AuthHarness /></AuthProvider>);
    await act(async () => {
      localStorage.setItem('auth-token', 'token-b');
      localStorage.setItem('auth-user', JSON.stringify({ userId: 2, username: 'conta-b' }));
      window.dispatchEvent(new StorageEvent('storage', { key: 'auth-user', newValue: localStorage.getItem('auth-user') }));
    });
    await act(async () => { finishA({ status: 'TRIALING' }); });
    expect(screen.container).toHaveTextContent('conta-b');
    expect(screen.container).toHaveTextContent('sem assinatura');
    expect(screen.container).not.toHaveTextContent('TRIALING');
    await click([...screen.container.querySelectorAll('button')].find(button => button.textContent === 'Sair')!);
    await act(async () => { finishB({ status: 'ACTIVE' }); });
    expect(screen.container).toHaveTextContent('visitante');
    expect(screen.container).not.toHaveTextContent('ACTIVE');
    await screen.unmount();
  });

  it('mascara a assinatura já carregada no primeiro render de outra sessão', async () => {
    localStorage.setItem('auth-token', 'token-a');
    localStorage.setItem('auth-user', JSON.stringify({ userId: 1, username: 'conta-a' }));
    mocks.getSubscription.mockResolvedValueOnce({ status: 'TRIALING' });
    const screen = await render(<AuthProvider><AuthHarness /></AuthProvider>);
    expect(screen.container).toHaveTextContent('TRIALING');
    let finish!: (value: unknown) => void;
    mocks.getSubscription.mockImplementationOnce(() => new Promise(resolve => { finish = resolve; }));
    await act(async () => {
      // A token rotation for the same account must also invalidate old requests.
      localStorage.setItem('auth-token', 'token-a-renovado');
      window.dispatchEvent(new StorageEvent('storage', { key: 'auth-token', newValue: 'token-a-renovado' }));
    });
    expect(screen.container).toHaveTextContent('sem assinatura');
    expect(screen.container).not.toHaveTextContent('TRIALING');
    await act(async () => { finish({ status: 'ACTIVE' }); });
    expect(screen.container).toHaveTextContent('ACTIVE');
    await screen.unmount();
  });

  it("persiste a sessão, carrega assinatura e limpa os dados no logout", async () => {
    const screen = await render(
      <AuthProvider>
        <AuthHarness />
      </AuthProvider>,
    );
    await settle();

    expect(screen.container).toHaveTextContent("visitante");
    expect(document.body.dataset.loading).toBe("false");

    await click([...screen.container.querySelectorAll("button")].find((button) => button.textContent === "Entrar")!);
    await settle();
    await settle();

    expect(localStorage.getItem("auth-token")).toBe("jwt-de-teste");
    expect(screen.container).toHaveTextContent("leitora");
    expect(screen.container).toHaveTextContent("TRIALING");
    expect(mocks.push).toHaveBeenCalledWith("/explorar");

    await click([...screen.container.querySelectorAll("button")].find((button) => button.textContent === "Renomear")!);
    expect(screen.container).toHaveTextContent("novo-nome");

    await click([...screen.container.querySelectorAll("button")].find((button) => button.textContent === "Sair")!);
    expect(localStorage.getItem("auth-token")).toBeNull();
    expect(screen.container).toHaveTextContent("visitante");
    expect(mocks.push).toHaveBeenLastCalledWith("/explorar");

    await screen.unmount();
  });
});
