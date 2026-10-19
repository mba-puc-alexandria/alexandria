package com.pucsp.alexandria.domain.subscription;

import com.pucsp.alexandria.domain.shared.valueobject.Id;

public class SubscriptionId extends Id<Long> {

  public SubscriptionId(Long value) {
    super(value);
  }

  public static SubscriptionId from(Long id) {
    if (id == null || id <= 0) {
      throw new IllegalArgumentException("SubscriptionId must be positive");
    }
    return new SubscriptionId(id);
  }
}
