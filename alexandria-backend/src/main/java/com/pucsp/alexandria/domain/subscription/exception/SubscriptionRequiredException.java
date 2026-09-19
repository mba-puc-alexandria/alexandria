package com.pucsp.alexandria.domain.subscription.exception;

import com.pucsp.alexandria.domain.shared.exception.DomainException;

public class SubscriptionRequiredException extends DomainException {

  public SubscriptionRequiredException() {
    super("SUBSCRIPTION_REQUIRED");
  }
}
