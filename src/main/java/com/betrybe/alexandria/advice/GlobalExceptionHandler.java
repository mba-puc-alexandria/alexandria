package com.betrybe.alexandria.advice;

import com.betrybe.alexandria.exception.AuthorNotFoundException;
import com.betrybe.alexandria.exception.BookDetailNotFoundException;
import com.betrybe.alexandria.exception.BookNotFoundException;
import com.betrybe.alexandria.exception.EmptyBookListException;
import com.betrybe.alexandria.exception.EmptyPublisherListException;
import com.betrybe.alexandria.exception.PublisherNotFoundException;
import com.betrybe.alexandria.exception.EmptyAuthorListException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(BookNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleBookNotFound(BookNotFoundException ex) {

    return ResponseEntity
        .status(HttpStatus.NOT_FOUND)
        .body(new ErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND.value()));
  }

  @ExceptionHandler(AuthorNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleAuthorNotFound(AuthorNotFoundException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(new ErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND.value()));
  }

  @ExceptionHandler(PublisherNotFoundException.class)
  public ResponseEntity<ErrorResponse> handlePublisherNotFound(PublisherNotFoundException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(new ErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND.value()));
  }

  @ExceptionHandler(BookDetailNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleBookDetailNotFound(BookDetailNotFoundException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(new ErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND.value()));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleException(Exception ex) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(new ErrorResponse(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
  }

  @ExceptionHandler(EmptyAuthorListException.class)
  public ResponseEntity<ErrorResponse> handleEmptyAuthorListException(
      EmptyAuthorListException ex) {
    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(new ErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST.value()));
  }

  @ExceptionHandler(EmptyPublisherListException.class)
  public ResponseEntity<ErrorResponse> handleEmptyPublisherListException(
      EmptyPublisherListException ex) {
    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(new ErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST.value()));
  }

  @ExceptionHandler(EmptyBookListException.class)
  public ResponseEntity<EmptyBookListException> handleEmptyBookListException(
      EmptyBookListException ex) {
    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(ex);
  }

}