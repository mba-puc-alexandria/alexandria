package com.betrybe.alexandria.domain.publisher.exception;

import com.betrybe.alexandria.domain.shared.exception.DomainException;

public class PublisherNotFoundException extends DomainException {

  public PublisherNotFoundException(Long id) {
    super("Publisher with id " + id + " not found");
  }

  public PublisherNotFoundException(String message) {
    super(message);
  }
}

