package com.pucsp.alexandria.adapter.in.rest.dto;

import com.pucsp.alexandria.application.book.dto.SearchBookOutput;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Resultado da busca de livros.")
public record SearchBookResponse(
    @Schema(description = "ID do livro.", example = "1")
    Long id,

    @Schema(description = "ID do livro no Projeto Gutenberg (Gutendex).", example = "55752")
    Long gutendexId,

    @Schema(description = "Título do livro.", example = "Dom Casmurro")
    String title,

    @Schema(description = "Autores concatenados.", example = "Machado de Assis")
    String authors,

    @Schema(description = "IDs dos autores.", example = "[1, 2]")
    List<Long> authorIds,

    @Schema(description = "URL de download do EPUB.", example = "https://www.gutenberg.org/ebooks/55752.epub3.images")
    String downloadUrl,

    @Schema(description = "URL da capa.", example = "https://www.gutenberg.org/cache/epub/55752/pg55752.cover.medium.jpg")
    String coverUrl,

    @Schema(description = "Idiomas disponíveis.", example = "pt")
    String languages,

    @Schema(description = "Assuntos/temas do livro.", example = "Literatura brasileira, Romance")
    String subjects,

    @Schema(description = "Número de downloads no Gutendex.", example = "1520")
    Integer downloadCount
) {

  public static SearchBookResponse from(SearchBookOutput output) {
    return new SearchBookResponse(
        output.id(),
        output.gutendexId(),
        output.title(),
        output.authorsDisplay(),
        output.authorIds(),
        output.downloadUrl(),
        output.coverUrl(),
        output.languages(),
        output.subjects(),
        output.downloadCount()
    );
  }
}
