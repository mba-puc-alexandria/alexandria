import { describe, expect, it } from "vitest";
import { getAuthorDisplay, type BookApiResponse } from "./api";

const book = (authors: BookApiResponse["authors"]): BookApiResponse => ({
  id: 1,
  title: "Livro de teste",
  authors,
  coverUrl: null,
  isbn: null,
  publicationYear: 2024,
  genre: "Ficção",
  synopsis: "Sinopse de teste",
  averageRating: 0,
  ratingsCount: 0,
});

describe("getAuthorDisplay", () => {
  it("mantém o formato legado em texto", () => {
    expect(getAuthorDisplay(book("Machado de Assis"))).toBe("Machado de Assis");
  });

  it("combina os nomes de autores estruturados", () => {
    expect(
      getAuthorDisplay(
        book([
          { id: 1, name: "Lygia Fagundes Telles" },
          { id: 2, name: "Clarice Lispector" },
        ]),
      ),
    ).toBe("Lygia Fagundes Telles, Clarice Lispector");
  });

  it("retorna texto vazio quando não há autores", () => {
    expect(getAuthorDisplay(book([]))).toBe("");
  });
});
