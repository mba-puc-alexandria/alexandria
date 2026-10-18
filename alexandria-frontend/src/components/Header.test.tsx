import type { AnchorHTMLAttributes } from "react";
import { beforeEach, describe, expect, it, vi } from "vitest";
import Header from "./Header";
import { click, render } from "@/test-utils/render";

const mocks = vi.hoisted(() => ({
  auth: {
    user: null as { username: string } | null,
    logout: vi.fn(),
  },
  openLoginModal: vi.fn(),
}));

vi.mock("next/link", () => ({
  default: ({ children, ...props }: AnchorHTMLAttributes<HTMLAnchorElement>) => (
    <a {...props}>{children}</a>
  ),
}));

vi.mock("@/contexts/AuthContext", () => ({
  useAuth: () => mocks.auth,
}));

vi.mock("@/contexts/AuthModalContext", () => ({
  useAuthModal: () => ({ openLoginModal: mocks.openLoginModal }),
}));

describe("Header", () => {
  beforeEach(() => {
    localStorage.clear();
    document.documentElement.className = "";
    vi.clearAllMocks();
    mocks.auth.user = null;
  });

  it("abre login para visitante e alterna o tema", async () => {
    const screen = await render(<Header />);

    await click([...screen.container.querySelectorAll("button")].find((button) => button.textContent === "Entrar")!);
    await click(screen.container.querySelector('button[title="Modo escuro"]')!);

    expect(mocks.openLoginModal).toHaveBeenCalledOnce();
    expect(document.documentElement).toHaveClass("dark");
    expect(localStorage.getItem("alexandria-theme")).toBe("dark");
    await screen.unmount();
  });

  it("mostra usuário autenticado e permite sair", async () => {
    mocks.auth.user = { username: "leitora" };
    const screen = await render(<Header />);

    expect(screen.container).toHaveTextContent("leitora");
    await click(screen.container.querySelector('button[title="Sair"]')!);

    expect(mocks.auth.logout).toHaveBeenCalledOnce();
    await screen.unmount();
  });
});
