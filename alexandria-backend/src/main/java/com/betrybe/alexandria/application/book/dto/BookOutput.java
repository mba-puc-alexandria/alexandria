package com.betrybe.alexandria.application.book.dto;

import com.betrybe.alexandria.domain.book.Book;
import com.betrybe.alexandria.domain.book.BookSource;

public record BookOutput(
    Long id,
    String title,
    String genre,
    Long publisherId,
    String source
) {

  public static BookOutput from(Book book) {
    return new BookOutput(
        book.getId().getValue(),
        book.getTitle(),
        book.getGenre(),
        book.getPublisherId(),
        book.getSource().name()
    );
  }
}

