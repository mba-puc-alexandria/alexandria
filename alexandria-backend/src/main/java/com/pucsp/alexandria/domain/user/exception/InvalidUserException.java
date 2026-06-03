package com.pucsp.alexandria.domain.user.exception;

import com.pucsp.alexandria.domain.shared.exception.DomainException;

public class InvalidUserException extends DomainException {

  public InvalidUserException(String message) {
    super(message);
  }

  public InvalidUserException(String message, Throwable cause) {
    super(message, cause);
  }
}

