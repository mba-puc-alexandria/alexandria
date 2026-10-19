package com.pucsp.alexandria.adapter.in.rest.dto;

import com.pucsp.alexandria.application.book.dto.BookOutput;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Detalhes de um livro.")
public record BookResponse(
    @Schema(description = "ID do livro.", example = "1")
    Long id,

    @Schema(description = "Título do livro.", example = "Dom Casmurro")
    String title,

    @Schema(description = "Lista de autores.")
    List<AuthorInfo> authors,

    @Schema(description = "ID do livro no Projeto Gutenberg (Gutendex).", example = "55752")
    Long gutendexId,

    @Schema(description = "URL de download do EPUB.", example = "https://www.gutenberg.org/ebooks/55752.epub3.images")
    String downloadUrl,

    @Schema(description = "URL da capa.", example = "https://www.gutenberg.org/cache/epub/55752/pg55752.cover.medium.jpg")
    String coverUrl,

    @Schema(description = "Idiomas disponíveis.", example = "pt")
    String languages,

    @Schema(description = "Assuntos/temas do livro.", example = "Literatura brasileira, Romance")
    String subjects,

    @Schema(description = "Número de downloads no Gutendex.", example = "1520")
    Integer downloadCount,

    @Schema(description = "ID do publisher (quando livro local).", example = "1")
    Long publisherId,

    @Schema(description = "Fonte do livro.", example = "GUTENDEX", allowableValues = {"GUTENDEX", "LOCAL"})
    String source
) {

  @Schema(description = "Informações resumidas do autor.")
  public record AuthorInfo(
      @Schema(description = "ID do autor.", example = "1")
      Long id,

      @Schema(description = "Nome do autor.", example = "Machado de Assis")
      String name,

      @Schema(description = "Ano de nascimento.", example = "1839")
      Integer birthYear,

      @Schema(description = "Ano de falecimento.", example = "1908")
      Integer deathYear) {}

  public static BookResponse from(BookOutput output) {
    return new BookResponse(
        output.id(),
        output.title(),
        output.authors().stream()
            .map(a -> new AuthorInfo(a.id(), a.name(), a.birthYear(), a.deathYear()))
            .toList(),
        output.gutendexId(),
        output.downloadUrl(),
        output.coverUrl(),
        output.languages(),
        output.subjects(),
        output.downloadCount(),
        output.publisherId(),
        output.source()
    );
  }
}
