package com.betrybe.alexandria.domain.publisher.exception;

import com.betrybe.alexandria.domain.shared.exception.DomainException;

public class InvalidPublisherException extends DomainException {

  public InvalidPublisherException(String message) {
    super(message);
  }

  public InvalidPublisherException(String message, Throwable cause) {
    super(message, cause);
  }
}

