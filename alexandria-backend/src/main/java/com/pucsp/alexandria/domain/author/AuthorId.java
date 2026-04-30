package com.pucsp.alexandria.domain.author;

import com.pucsp.alexandria.domain.shared.valueobject.Id;

public class AuthorId extends Id<Long> {

  public AuthorId(Long value) {
    super(value);
  }

  public static AuthorId from(Long id) {
    if (id == null) {
      throw new IllegalArgumentException("AuthorId cannot be null");
    }
    if (id <= 0) {
      throw new IllegalArgumentException("AuthorId must be positive");
    }
    return new AuthorId(id);
  }
}

