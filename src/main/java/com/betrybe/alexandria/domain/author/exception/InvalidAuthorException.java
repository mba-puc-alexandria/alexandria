package com.betrybe.alexandria.domain.author.exception;

import com.betrybe.alexandria.domain.shared.exception.DomainException;

public class InvalidAuthorException extends DomainException {

  public InvalidAuthorException(String message) {
    super(message);
  }

  public InvalidAuthorException(String message, Throwable cause) {
    super(message, cause);
  }
}

