package com.pucsp.alexandria.domain.user.exception;

import com.pucsp.alexandria.domain.shared.exception.DomainException;

public class InvalidCredentialsException extends DomainException {

  public InvalidCredentialsException(String message) {
    super(message);
  }
}
