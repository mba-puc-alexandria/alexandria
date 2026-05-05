package com.pucsp.alexandria.domain.userbook.exception;

import com.pucsp.alexandria.domain.shared.exception.DomainException;

public class InvalidUserBooksException extends DomainException {

  public InvalidUserBooksException(String message) {
    super(message);
  }
}
