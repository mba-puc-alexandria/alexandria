import { useEffect } from "react";
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
