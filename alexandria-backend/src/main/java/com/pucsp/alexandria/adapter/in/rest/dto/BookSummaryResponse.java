package com.pucsp.alexandria.adapter.in.rest.dto;

import com.pucsp.alexandria.application.book.dto.BookOutput;

public record BookSummaryResponse(
    Long id,
    Long gutenbergId,
    String title,
    String author,
    String coverUrl,
    String downloadUrl
) {

  public static BookSummaryResponse from(BookOutput book) {
    return new BookSummaryResponse(
        book.id().getValue(),
        book.gutendexId(),
        book.title(),
        book.author(),
        book.coverUrl(),
        book.downloadUrl()
    );
  }
}
