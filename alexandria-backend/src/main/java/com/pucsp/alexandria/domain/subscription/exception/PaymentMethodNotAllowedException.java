package com.pucsp.alexandria.domain.subscription.exception;

import com.pucsp.alexandria.domain.shared.exception.DomainException;

public class PaymentMethodNotAllowedException extends DomainException {

  public PaymentMethodNotAllowedException(String message) {
    super(message);
  }
}
