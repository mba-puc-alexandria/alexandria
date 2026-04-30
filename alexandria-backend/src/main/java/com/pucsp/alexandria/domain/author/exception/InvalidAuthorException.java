package com.pucsp.alexandria.domain.author.exception;

import com.pucsp.alexandria.domain.shared.exception.DomainException;

public class InvalidAuthorException extends DomainException {

  public InvalidAuthorException(String message) {
    super(message);
  }

  public InvalidAuthorException(String message, Throwable cause) {
    super(message, cause);
  }
}

