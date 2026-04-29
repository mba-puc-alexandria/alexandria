package com.pucsp.alexandria.adapter.in.rest.dto;

import com.pucsp.alexandria.application.book.dto.BookOutput;

public record BookResponse(
    Long id,
    String title,
    String genre,
    Long publisherId,
    String source
) {

  public static BookResponse from(BookOutput output) {
    return new BookResponse(
        output.id(),
        output.title(),
        output.genre(),
        output.publisherId(),
        output.source()
    );
  }
}

