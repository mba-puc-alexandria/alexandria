package com.pucsp.alexandria.domain.userbook.exception;

import com.pucsp.alexandria.domain.shared.exception.DomainException;

public class DuplicateUserBooksException extends DomainException {

  public DuplicateUserBooksException(String message) {
    super(message);
  }
}
