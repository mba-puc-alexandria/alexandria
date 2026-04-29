package com.pucsp.alexandria.domain.book.exception;

import com.pucsp.alexandria.domain.shared.exception.DomainException;

public class DuplicateBookException extends DomainException {

  public DuplicateBookException(String title) {
    super("Book with title '" + title + "' already exists");
  }

  public DuplicateBookException(String message, Throwable cause) {
    super(message, cause);
  }
}

