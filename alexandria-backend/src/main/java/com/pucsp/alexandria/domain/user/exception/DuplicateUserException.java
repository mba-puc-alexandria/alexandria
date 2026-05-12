package com.pucsp.alexandria.domain.user.exception;

import com.pucsp.alexandria.domain.shared.exception.DomainException;

public class DuplicateUserException extends DomainException {

  public DuplicateUserException(String message) {
    super(message);
  }
}
