import { describe, expect, it } from "vitest";
import { AuthModalProvider, useAuthModal } from "./AuthModalContext";
import { click, render } from "@/test-utils/render";

function ModalHarness() {
  const { isOpen, openLoginModal, closeLoginModal } = useAuthModal();

  return (
    <>
      <p>{isOpen ? "aberto" : "fechado"}</p>
      <button type="button" onClick={openLoginModal}>Abrir</button>
      <button type="button" onClick={closeLoginModal}>Fechar</button>
    </>
  );
}

describe("AuthModalProvider", () => {
  it("expõe abertura e fechamento do modal de autenticação", async () => {
    const screen = await render(
      <AuthModalProvider>
        <ModalHarness />
      </AuthModalProvider>,
    );

    expect(screen.container).toHaveTextContent("fechado");
    await click([...screen.container.querySelectorAll("button")].find((button) => button.textContent === "Abrir")!);
    expect(screen.container).toHaveTextContent("aberto");
    await click([...screen.container.querySelectorAll("button")].find((button) => button.textContent === "Fechar")!);
    expect(screen.container).toHaveTextContent("fechado");

    await screen.unmount();
  });
});
