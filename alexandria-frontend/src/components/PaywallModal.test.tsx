import type { AnchorHTMLAttributes } from "react";
import { describe, expect, it, vi } from "vitest";
import PaywallModal from "./PaywallModal";
import { click, render } from "@/test-utils/render";

vi.mock("next/link", () => ({
  default: ({ children, ...props }: AnchorHTMLAttributes<HTMLAnchorElement>) => (
    <a {...props}>{children}</a>
  ),
}));

describe("PaywallModal", () => {
  it("não renderiza quando está fechado", async () => {
    const screen = await render(<PaywallModal open={false} onClose={vi.fn()} />);

    expect(screen.container).toBeEmptyDOMElement();

    await screen.unmount();
  });

  it("fecha pelo botão e pelo overlay acessível", async () => {
    const onClose = vi.fn();
    const screen = await render(<PaywallModal open onClose={onClose} />);

    expect(screen.container.querySelector('[role="dialog"]')).toHaveAttribute("aria-modal", "true");
    expect(screen.container).toHaveTextContent("Alexandria Premium");

    const controls = screen.container.querySelectorAll("button");
    expect(controls).toHaveLength(2);

    await click(controls[0]);
    await click(controls[1]);

    expect(onClose).toHaveBeenCalledTimes(2);
    await screen.unmount();
  });
});
