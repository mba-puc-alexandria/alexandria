package com.betrybe.alexandria.exception;

public class BookDetailNotFoundException extends RuntimeException {

  public BookDetailNotFoundException(Long id) {
    super("BookDetail not found with id: " + id);
  }

}
