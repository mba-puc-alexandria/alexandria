package com.pucsp.alexandria.domain.userbook.exception;

import com.pucsp.alexandria.domain.shared.exception.DomainException;

public class UserBooksNotFoundException extends DomainException {

  public UserBooksNotFoundException(String message) {
    super(message);
  }
}
