package com.betrybe.alexandria.domain.book.exception;

import com.betrybe.alexandria.domain.shared.exception.DomainException;

public class InvalidBookException extends DomainException {

  public InvalidBookException(String message) {
    super(message);
  }

  public InvalidBookException(String message, Throwable cause) {
    super(message, cause);
  }
}

