package com.pucsp.alexandria.adapter.in.rest.dto;

import com.pucsp.alexandria.application.book.dto.BookOutput;
import java.util.stream.Collectors;

public record BookSummaryResponse(
    Long id,
    Long gutenbergId,
    String title,
    String authors,
    String cover,
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
