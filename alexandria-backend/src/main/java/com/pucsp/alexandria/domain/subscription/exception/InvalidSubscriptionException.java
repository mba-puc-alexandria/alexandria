package com.pucsp.alexandria.domain.subscription.exception;

import com.pucsp.alexandria.domain.shared.exception.DomainException;

public class InvalidSubscriptionException extends DomainException {

  public InvalidSubscriptionException(String message) {
    super(message);
  }
}
