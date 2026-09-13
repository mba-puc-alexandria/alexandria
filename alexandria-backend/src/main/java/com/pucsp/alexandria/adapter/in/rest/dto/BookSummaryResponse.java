package com.pucsp.alexandria.adapter.in.rest.dto;

import com.pucsp.alexandria.application.book.dto.BookOutput;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.stream.Collectors;

@Schema(description = "Resumo de um livro (usado em listas e na biblioteca).")
public record BookSummaryResponse(
    @Schema(description = "ID do livro.", example = "1")
    Long id,

    @Schema(description = "ID do livro no Projeto Gutenberg (Gutendex).", example = "55752")
    Long gutenbergId,

    @Schema(description = "Título do livro.", example = "Dom Casmurro")
    String title,

    @Schema(description = "Autores concatenados.", example = "Machado de Assis")
    String authors,

    @Schema(description = "URL da capa.", example = "https://www.gutenberg.org/cache/epub/55752/pg55752.cover.medium.jpg")
    String coverUrl,

    @Schema(description = "URL de download do EPUB.", example = "https://www.gutenberg.org/ebooks/55752.epub3.images")
    String downloadUrl
) {

  public static BookSummaryResponse from(BookOutput book) {
    String authorNames = book.authors().stream()
        .map(BookOutput.AuthorInfo::name)
        .collect(Collectors.joining(", "));

    return new BookSummaryResponse(
        book.id(),
        book.gutendexId(),
        book.title(),
        authorNames,
        book.coverUrl(),
        book.downloadUrl()
    );
  }
}
