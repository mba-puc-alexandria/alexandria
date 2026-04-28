package com.betrybe.alexandria.domain.publisher.exception;

import com.betrybe.alexandria.domain.shared.exception.DomainException;

public class DuplicatePublisherException extends DomainException {

  public DuplicatePublisherException(String name) {
    super("Publisher with name '" + name + "' already exists");
  }

  public DuplicatePublisherException(String message, Throwable cause) {
    super(message, cause);
  }
}

