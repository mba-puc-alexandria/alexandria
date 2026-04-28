package com.betrybe.alexandria.exception;

public class EmptyPublisherListException extends RuntimeException {

  public EmptyPublisherListException() {
    super("A lista de editoras não pode estar vazia");
  }

  public EmptyPublisherListException(String message) {
    super(message);
  }
}
