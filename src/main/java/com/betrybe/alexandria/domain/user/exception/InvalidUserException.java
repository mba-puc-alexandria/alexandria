package com.betrybe.alexandria.domain.user.exception;

import com.betrybe.alexandria.domain.shared.exception.DomainException;

public class InvalidUserException extends DomainException {

  public InvalidUserException(String message) {
    super(message);
  }

  public InvalidUserException(String message, Throwable cause) {
    super(message, cause);
  }
}

