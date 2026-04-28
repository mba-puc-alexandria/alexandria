package com.betrybe.alexandria.adapter.in.rest.dto;

import com.betrybe.alexandria.application.book.dto.BookOutput;

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

