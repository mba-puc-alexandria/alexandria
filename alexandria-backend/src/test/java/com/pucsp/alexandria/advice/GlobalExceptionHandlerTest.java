package com.pucsp.alexandria.advice;

import static org.junit.jupiter.api.Assertions.*;

import com.pucsp.alexandria.domain.book.exception.BookNotFoundException;
import com.pucsp.alexandria.domain.userbook.exception.DuplicateUserBooksException;
import com.pucsp.alexandria.domain.userbook.exception.InvalidUserBooksException;
import com.pucsp.alexandria.domain.userbook.exception.UserBooksNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void shouldHandleBookNotFound() {
        BookNotFoundException ex = new BookNotFoundException(99L);
        ResponseEntity<ErrorResponse> response = handler.handleBookNotFound(ex);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Book with id 99 not found", response.getBody().getMessage());
        assertEquals(404, response.getBody().getStatus());
    }

    @Test
    void shouldHandleUserBooksNotFound() {
        UserBooksNotFoundException ex = new UserBooksNotFoundException("UserBook not found with id: 1");
        ResponseEntity<ErrorResponse> response = handler.handleUserBooksNotFound(ex);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("UserBook not found with id: 1", response.getBody().getMessage());
    }

    @Test
    void shouldHandleInvalidUserBooks() {
        InvalidUserBooksException ex = new InvalidUserBooksException("Invalid data");
        ResponseEntity<ErrorResponse> response = handler.handleInvalidUserBooks(ex);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid data", response.getBody().getMessage());
        assertEquals(400, response.getBody().getStatus());
    }

    @Test
    void shouldHandleIllegalArgument() {
        IllegalArgumentException ex = new IllegalArgumentException("Invalid argument");
        ResponseEntity<ErrorResponse> response = handler.handleIllegalArgument(ex);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid argument", response.getBody().getMessage());
    }

    @Test
    void shouldHandleDuplicateUserBooks() {
        DuplicateUserBooksException ex = new DuplicateUserBooksException("Duplicate entry");
        ResponseEntity<ErrorResponse> response = handler.handleDuplicateUserBooks(ex);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Duplicate entry", response.getBody().getMessage());
        assertEquals(409, response.getBody().getStatus());
    }

    @Test
    void shouldHandleBadCredentials() {
        BadCredentialsException ex = new BadCredentialsException("Bad credentials");
        ResponseEntity<ErrorResponse> response = handler.handleBadCredentials(ex);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Invalid username or password", response.getBody().getMessage());
        assertEquals(401, response.getBody().getStatus());
    }

    @Test
    void shouldHandleGenericException() {
        Exception ex = new RuntimeException("Unexpected error");
        ResponseEntity<ErrorResponse> response = handler.handleException(ex);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Unexpected error", response.getBody().getMessage());
        assertEquals(500, response.getBody().getStatus());
    }

    @Test
    void errorResponseShouldHaveTimestamp() {
        BookNotFoundException ex = new BookNotFoundException(1L);
        ResponseEntity<ErrorResponse> response = handler.handleBookNotFound(ex);
        assertNotNull(response.getBody().getTimestamp());
    }
}
