package com.pucsp.alexandria.domain.author.exception;

import com.pucsp.alexandria.domain.shared.exception.DomainException;

public class DuplicateAuthorException extends DomainException {

  public DuplicateAuthorException(String name) {
    super("Author with name '" + name + "' already exists");
  }

  public DuplicateAuthorException(String message, Throwable cause) {
    super(message, cause);
  }
}

