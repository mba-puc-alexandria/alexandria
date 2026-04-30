package com.pucsp.alexandria.domain.book.exception;

import com.pucsp.alexandria.domain.shared.exception.DomainException;

public class BookNotFoundException extends DomainException {

  public BookNotFoundException(Long id) {
    super("Book with id " + id + " not found");
  }

  public BookNotFoundException(String message) {
    super(message);
  }
}

