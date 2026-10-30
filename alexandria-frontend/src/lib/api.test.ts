import { describe, expect, it } from "vitest";
import { getAuthorDisplay, type BookApiResponse } from "./api";

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
