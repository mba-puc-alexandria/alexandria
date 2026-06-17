package com.pucsp.alexandria.domain.user.exception;

import com.pucsp.alexandria.domain.shared.exception.DomainException;

public class UserNotFoundException extends DomainException {

  public UserNotFoundException(Long id) {
    super("User with id " + id + " not found");
  }
}
