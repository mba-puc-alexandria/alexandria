package com.pucsp.alexandria.domain.author.exception;

import com.pucsp.alexandria.domain.shared.exception.DomainException;

public class AuthorNotFoundException extends DomainException {

  public AuthorNotFoundException(Long id) {
    super("Author with id " + id + " not found");
  }

  public AuthorNotFoundException(String message) {
    super(message);
  }
}

