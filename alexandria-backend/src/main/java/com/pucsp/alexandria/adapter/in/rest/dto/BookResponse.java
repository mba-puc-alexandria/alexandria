package com.pucsp.alexandria.adapter.in.rest.dto;

import com.pucsp.alexandria.application.book.dto.BookOutput;
import com.pucsp.alexandria.domain.book.BookId;

public record BookResponse(
    BookId id,
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
        output.id(),
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

