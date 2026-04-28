package com.betrybe.alexandria.exception;

public class EmptyAuthorListException extends RuntimeException {

  public EmptyAuthorListException() {
    super("A lista de autores não pode estar vazia");
  }

  public EmptyAuthorListException(String message) {
    super(message);
  }

}
