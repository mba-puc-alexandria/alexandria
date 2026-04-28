package com.betrybe.alexandria.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.betrybe.alexandria.controller.dto.AuthorCreationDto;
import com.betrybe.alexandria.entity.Author;
import com.betrybe.alexandria.service.AuthorService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@DisplayName("AuthorController Tests")
@ExtendWith(MockitoExtension.class)
class AuthorControllerTest {

  @Mock
  private AuthorService authorService;

  @InjectMocks
  private AuthorController authorController;

  @Test
  @DisplayName("Should call authorService.findById when getAuthorById is called")
  void testGetAuthorByIdCallsService() {
    // Arrange
    Long authorId = 1L;
    Author author = new Author("João", "Brasileiro");
    when(authorService.findById(authorId)).thenReturn(author);

    // Act
    authorController.getAuthorById(authorId);

    // Assert
    verify(authorService).findById(authorId);
  }

  @Test
  @DisplayName("Should call authorService.findAll when getAllAuthors is called")
  void testGetAllAuthorsCallsService() {
    // Arrange
    var pageable = PageRequest.of(0, 10);
    var emptyPage = new PageImpl<Author>(List.of(), pageable, 0);
    when(authorService.findAll(pageable)).thenReturn(emptyPage);

    // Act
    authorController.getAllAuthors(pageable);

    // Assert
    verify(authorService).findAll(pageable);
  }

  @Test
  @DisplayName("Should call authorService.create when createAuthor is called")
  void testCreateAuthorCallsService() {
    // Arrange
    var dto = new AuthorCreationDto("Paulo", "Argentino");
    Author author = new Author("Paulo", "Argentino");
    when(authorService.create(any(Author.class))).thenReturn(author);

    // Act
    authorController.createAuthor(dto);

    // Assert
    verify(authorService).create(any(Author.class));
  }

  @Test
  @DisplayName("Should call authorService.createBatch when createAuthors is called")
  void testCreateAuthorsCallsService() {
    // Arrange
    var dtos = List.of(
        new AuthorCreationDto("Author1", "Country1"),
        new AuthorCreationDto("Author2", "Country2")
    );
    when(authorService.createBatch(any(List.class))).thenReturn(List.of(
        new Author("Author1", "Country1"),
        new Author("Author2", "Country2")
    ));

    // Act
    authorController.createAuthors(dtos);

    // Assert
    verify(authorService).createBatch(any(List.class));
  }

  @Test
  @DisplayName("Should call authorService.update when updateAuthor is called")
  void testUpdateAuthorCallsService() {
    // Arrange
    Long authorId = 1L;
    var dto = new AuthorCreationDto("Updated", "Country");
    Author author = new Author("Updated", "Country");
    when(authorService.update(eq(authorId), any(Author.class))).thenReturn(author);

    // Act
    authorController.updateAuthor(authorId, dto);

    // Assert
    verify(authorService).update(eq(authorId), any(Author.class));
  }

  @Test
  @DisplayName("Should call authorService.deleteById when deleteAuthorById is called")
  void testDeleteAuthorByIdCallsService() {
    // Arrange
    Long authorId = 1L;
    doNothing().when(authorService).deleteById(authorId);

    // Act
    authorController.deleteAuthorById(authorId);

    // Assert
    verify(authorService).deleteById(authorId);
  }

  @Test
  @DisplayName("Should call authorService.getAuthorBooks when getAuthorBooks is called")
  void testGetAuthorBooksCallsService() {
    // Arrange
    Long authorId = 1L;
    var books = List.of(new com.betrybe.alexandria.entity.Book("Book1", "Genre1"));
    when(authorService.getAuthorBooks(authorId)).thenReturn(books);

    // Act
    authorController.getAuthorBooks(authorId);

    // Assert
    verify(authorService).getAuthorBooks(authorId);
  }
}










