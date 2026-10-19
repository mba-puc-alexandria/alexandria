package com.pucsp.alexandria.domain.subscription.exception;

import com.pucsp.alexandria.domain.shared.exception.DomainException;

public class SubscriptionNotFoundException extends DomainException {

  public SubscriptionNotFoundException(Long userId) {
    super("Subscription not found for user with id " + userId);
  }
}
