package com.betrybe.alexandria.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.betrybe.alexandria.controller.dto.AuthorCreationDto;
import com.betrybe.alexandria.entity.Author;
import com.betrybe.alexandria.entity.Book;
import com.betrybe.alexandria.exception.AuthorNotFoundException;
import com.betrybe.alexandria.exception.EmptyAuthorListException;
import com.betrybe.alexandria.repository.AuthorRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@DisplayName("AuthorService Tests")
@ExtendWith(MockitoExtension.class)
class AuthorServiceTest {

  @Mock
  private AuthorRepository authorRepository;

  @InjectMocks
  private AuthorService authorService;

  @Test
  @DisplayName("Should find author by id successfully")
  void testFindByIdSuccess() {
    // Arrange
    Long authorId = 1L;
    Author author = new Author("João", "Brasileiro");
    when(authorRepository.findById(authorId)).thenReturn(Optional.of(author));

    // Act
    Author result = authorService.findById(authorId);

    // Assert
    assertNotNull(result);
    assertEquals("João", result.getName());
    assertEquals("Brasileiro", result.getNationality());
    verify(authorRepository).findById(authorId);
  }

  @Test
  @DisplayName("Should throw AuthorNotFoundException when author not found")
  void testFindByIdNotFound() {
    // Arrange
    Long authorId = 999L;
    when(authorRepository.findById(authorId)).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(AuthorNotFoundException.class, () -> authorService.findById(authorId));
    verify(authorRepository).findById(authorId);
  }

  @Test
  @DisplayName("Should find all authors with pagination")
  void testFindAllSuccess() {
    // Arrange
    Author author1 = new Author("João", "Brasileiro");
    Author author2 = new Author("Maria", "Portuguesa");
    Pageable pageable = PageRequest.of(0, 10);
    Page<Author> authorPage = new PageImpl<>(List.of(author1, author2), pageable, 2);
    when(authorRepository.findAll(pageable)).thenReturn(authorPage);

    // Act
    Page<Author> result = authorService.findAll(pageable);

    // Assert
    assertNotNull(result);
    assertEquals(2, result.getContent().size());
    verify(authorRepository).findAll(pageable);
  }

  @Test
  @DisplayName("Should create author successfully")
  void testCreateSuccess() {
    // Arrange
    Author author = new Author("Paulo", "Argentino");
    when(authorRepository.save(author)).thenReturn(author);

    // Act
    Author result = authorService.create(author);

    // Assert
    assertNotNull(result);
    assertEquals("Paulo", result.getName());
    assertEquals("Argentino", result.getNationality());
    verify(authorRepository).save(author);
  }

  @Test
  @DisplayName("Should create batch of authors successfully")
  void testCreateBatchSuccess() {
    // Arrange
    AuthorCreationDto dto1 = new AuthorCreationDto("Clarice", "Brasileira");
    AuthorCreationDto dto2 = new AuthorCreationDto("Machado", "Brasileiro");
    List<AuthorCreationDto> authorsDto = List.of(dto1, dto2);

    Author author1 = new Author("Clarice", "Brasileira");
    Author author2 = new Author("Machado", "Brasileiro");
    when(authorRepository.save(any(Author.class))).thenReturn(author1, author2);

    // Act
    List<Author> result = authorService.createBatch(authorsDto);

    // Assert
    assertNotNull(result);
    assertEquals(2, result.size());
    verify(authorRepository, times(2)).save(any(Author.class));
  }

  @Test
  @DisplayName("Should throw EmptyAuthorListException when list is empty")
  void testCreateBatchEmptyList() {
    // Arrange
    List<AuthorCreationDto> emptyList = List.of();

    // Act & Assert
    assertThrows(EmptyAuthorListException.class, () -> authorService.createBatch(emptyList));
    verify(authorRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should throw EmptyAuthorListException when list is null")
  void testCreateBatchNullList() {
    // Arrange & Act & Assert
    assertThrows(EmptyAuthorListException.class, () -> authorService.createBatch(null));
    verify(authorRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should update author successfully")
  void testUpdateSuccess() {
    // Arrange
    Long authorId = 1L;
    Author existingAuthor = new Author("João", "Brasileiro");
    Author updateData = new Author("João Silva", "Português");

    when(authorRepository.findById(authorId)).thenReturn(Optional.of(existingAuthor));
    when(authorRepository.save(existingAuthor)).thenReturn(existingAuthor);

    // Act
    Author result = authorService.update(authorId, updateData);

    // Assert
    assertNotNull(result);
    assertEquals("João Silva", result.getName());
    assertEquals("Português", result.getNationality());
    verify(authorRepository).findById(authorId);
    verify(authorRepository).save(existingAuthor);
  }

  @Test
  @DisplayName("Should throw AuthorNotFoundException when updating non-existent author")
  void testUpdateNotFound() {
    // Arrange
    Long authorId = 999L;
    Author updateData = new Author("João", "Brasileiro");
    when(authorRepository.findById(authorId)).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(AuthorNotFoundException.class, () -> authorService.update(authorId, updateData));
    verify(authorRepository).findById(authorId);
    verify(authorRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should delete author successfully")
  void testDeleteByIdSuccess() {
    // Arrange
    Long authorId = 1L;
    Author author = new Author("João", "Brasileiro");
    when(authorRepository.findById(authorId)).thenReturn(Optional.of(author));

    // Act
    authorService.deleteById(authorId);

    // Assert
    verify(authorRepository).findById(authorId);
    verify(authorRepository).delete(author);
  }

  @Test
  @DisplayName("Should throw AuthorNotFoundException when deleting non-existent author")
  void testDeleteByIdNotFound() {
    // Arrange
    Long authorId = 999L;
    when(authorRepository.findById(authorId)).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(AuthorNotFoundException.class, () -> authorService.deleteById(authorId));
    verify(authorRepository).findById(authorId);
    verify(authorRepository, never()).delete(any());
  }

  @Test
  @DisplayName("Should get author books successfully")
  void testGetAuthorBooksSuccess() {
    // Arrange
    Long authorId = 1L;
    Author author = new Author("João", "Brasileiro");

    when(authorRepository.findById(authorId)).thenReturn(Optional.of(author));

    // Act
    List<Book> result = authorService.getAuthorBooks(authorId);

    // Assert
    assertNotNull(result);
    assertEquals(0, result.size()); // Author has no books
    verify(authorRepository).findById(authorId);
  }

  @Test
  @DisplayName("Should return empty list when author has no books")
  void testGetAuthorBooksEmpty() {
    // Arrange
    Long authorId = 1L;
    Author author = new Author("João", "Brasileiro");
    when(authorRepository.findById(authorId)).thenReturn(Optional.of(author));

    // Act
    List<Book> result = authorService.getAuthorBooks(authorId);

    // Assert
    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(authorRepository).findById(authorId);
  }

  @Test
  @DisplayName("Should throw AuthorNotFoundException when getting books of non-existent author")
  void testGetAuthorBooksNotFound() {
    // Arrange
    Long authorId = 999L;
    when(authorRepository.findById(authorId)).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(AuthorNotFoundException.class, () -> authorService.getAuthorBooks(authorId));
    verify(authorRepository).findById(authorId);
  }
}
