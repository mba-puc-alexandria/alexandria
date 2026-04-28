package com.betrybe.alexandria.domain.book;

import com.betrybe.alexandria.domain.shared.valueobject.Id;

public class BookId extends Id<Long> {

  public BookId(Long value) {
    super(value);
  }

  public static BookId from(Long id) {
    if (id == null) {
      throw new IllegalArgumentException("BookId cannot be null");
    }
    if (id <= 0) {
      throw new IllegalArgumentException("BookId must be positive");
    }
    return new BookId(id);
  }
}

