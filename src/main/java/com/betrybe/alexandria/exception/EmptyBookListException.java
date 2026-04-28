package com.betrybe.alexandria.exception;

public class EmptyBookListException extends RuntimeException {

  public  EmptyBookListException() {
    super("A lista de livros não pode estar vazia");
  }

  public EmptyBookListException(String message) {
    super(message);
  }
}
