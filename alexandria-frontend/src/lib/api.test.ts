import { describe, expect, it, vi } from "vitest";
import { getAuthorDisplay, getUserBooks, type BookApiResponse, type UserBookResponse } from "./api";

const book = (authors: BookApiResponse["authors"]): BookApiResponse => ({
  id: 1,
  title: "Livro de teste",
  authors,
  coverUrl: null,
  downloadUrl: null,
  languages: null,
  subjects: null,
  source: 'GUTENDEX',
  gutendexId: null,
  downloadCount: null,
  publisherId: null,
});

describe("getAuthorDisplay", () => {
  it("mantém o formato legado em texto", () => {
    expect(getAuthorDisplay(book("Machado de Assis"))).toBe("Machado de Assis");
  });

  it("combina os nomes de autores estruturados", () => {
    expect(
      getAuthorDisplay(
        book([
          { id: 1, name: "Lygia Fagundes Telles", birthYear: null, deathYear: null },
          { id: 2, name: "Clarice Lispector", birthYear: null, deathYear: null },
        ]),
      ),
    ).toBe("Lygia Fagundes Telles, Clarice Lispector");
  });

  it("retorna texto vazio quando não há autores", () => {
    expect(getAuthorDisplay(book([]))).toBe("");
  });
});

describe("getUserBooks", () => {
  it("carrega a biblioteca inteira para contadores e leitura após a primeira página", async () => {
    const entry = (id: number): UserBookResponse => ({
      id, status: "toread", progress: null, rating: null,
      book: { ...book([]), id, title: `Livro ${id}` },
    });
    const fetchMock = vi.spyOn(globalThis, "fetch")
      .mockResolvedValueOnce({ ok: true, json: async () => ({ content: Array.from({ length: 100 }, (_, index) => entry(index + 1)), totalPages: 2 }) } as Response)
      .mockResolvedValueOnce({ ok: true, json: async () => ({ content: [entry(101)], totalPages: 2 }) } as Response);
    try {
      const books = await getUserBooks();
      expect(books).toHaveLength(101);
      expect(books.at(-1)?.book.id).toBe(101);
      expect(fetchMock.mock.calls.map(([url]) => String(url).split("/user-books")[1])).toEqual([
        "?page=0&size=100&sort=id,asc", "?page=1&size=100&sort=id,asc",
      ]);
    } finally {
      fetchMock.mockRestore();
    }
  });

  it("não mistura páginas privadas quando a sessão muda durante a resposta", async () => {
    const originalToken = localStorage.getItem("auth-token");
    const originalUser = localStorage.getItem("auth-user");
    localStorage.setItem("auth-token", "token-a");
    localStorage.setItem("auth-user", JSON.stringify({ userId: 1 }));
    const fetchMock = vi.spyOn(globalThis, "fetch").mockResolvedValue({
      ok: true,
      json: async () => {
        localStorage.setItem("auth-token", "token-b");
        localStorage.setItem("auth-user", JSON.stringify({ userId: 2 }));
        return { content: [{ id: 1 }], totalPages: 2 };
      },
    } as Response);
    try {
      await expect(getUserBooks()).rejects.toMatchObject({ name: "AbortError" });
      expect(fetchMock).toHaveBeenCalledTimes(1);
      expect(fetchMock.mock.calls[0][1]).toMatchObject({ headers: { Authorization: "Bearer token-a" } });
    } finally {
      fetchMock.mockRestore();
      if (originalToken === null) localStorage.removeItem("auth-token");
      else localStorage.setItem("auth-token", originalToken);
      if (originalUser === null) localStorage.removeItem("auth-user");
      else localStorage.setItem("auth-user", originalUser);
    }
  });
});
