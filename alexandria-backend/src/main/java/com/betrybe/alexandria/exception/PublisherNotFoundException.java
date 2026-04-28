package com.betrybe.alexandria.exception;

public class PublisherNotFoundException extends RuntimeException {

  public PublisherNotFoundException(Long id) {
    super("Publisher not found with id: " + id);
  }

}
