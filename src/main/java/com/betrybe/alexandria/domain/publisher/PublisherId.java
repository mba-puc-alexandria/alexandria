package com.betrybe.alexandria.domain.publisher;

import com.betrybe.alexandria.domain.shared.valueobject.Id;

public class PublisherId extends Id<Long> {

  public PublisherId(Long value) {
    super(value);
  }

  public static PublisherId from(Long id) {
    if (id == null) {
      throw new IllegalArgumentException("PublisherId cannot be null");
    }
    if (id <= 0) {
      throw new IllegalArgumentException("PublisherId must be positive");
    }
    return new PublisherId(id);
  }
}

