package com.pucsp.alexandria.adapter.in.rest.dto;

import com.pucsp.alexandria.application.book.dto.BookOutput;

public record BookResponse(
    Long id,
    String title,
    String author,
    Long gutendexId,
    String downloadUrl,
    String coverUrl,
    String languages,
    String subjects,
    Integer downloadCount,
    Long publisherId,
    String source
) {

  public static BookResponse from(BookOutput output) {
    return new BookResponse(
        output.id().getValue(),
        output.title(),
        output.author(),
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

