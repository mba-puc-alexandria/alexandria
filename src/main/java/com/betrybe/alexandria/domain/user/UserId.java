package com.betrybe.alexandria.domain.user;

import com.betrybe.alexandria.domain.shared.valueobject.Id;

public class UserId extends Id<Long> {

  public UserId(Long value) {
    super(value);
  }

  public static UserId from(Long id) {
    if (id == null || id <= 0) {
      throw new IllegalArgumentException("UserId must be positive");
    }
    return new UserId(id);
  }
}

