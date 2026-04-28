package com.betrybe.alexandria.unit.application.book;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.betrybe.alexandria.application.book.CreateBookUseCase;
import com.betrybe.alexandria.application.book.dto.CreateBookInput;
import com.betrybe.alexandria.application.book.dto.CreateBookOutput;
import com.betrybe.alexandria.domain.book.Book;
import com.betrybe.alexandria.domain.book.BookRepository;
import com.betrybe.alexandria.domain.book.exception.DuplicateBookException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("CreateBookUseCase Tests")
class CreateBookUseCaseTest {

  private BookRepository bookRepository;
  private CreateBookUseCase useCase;

  @BeforeEach
  void setUp() {
    bookRepository = mock(BookRepository.class);
    useCase = new CreateBookUseCase(bookRepository);
  }

  @Test
  @DisplayName("Should create a new book successfully")
  void testCreateBookSuccess() {
    CreateBookInput input = new CreateBookInput("Clean Code", "Programming", 1L);
    Book savedBook = Book.restore(1L, input.title(), input.genre(), input.publisherId(),
        com.betrybe.alexandria.domain.book.BookSource.LOCAL);

    when(bookRepository.existsByTitle(input.title())).thenReturn(false);
    when(bookRepository.save(any(Book.class))).thenReturn(savedBook);

    CreateBookOutput output = useCase.execute(input);

    assertEquals(1L, output.id());
    verify(bookRepository).existsByTitle(input.title());
    verify(bookRepository).save(any(Book.class));
  }

  @Test
  @DisplayName("Should fail when book title already exists")
  void testCreateDuplicateBook() {
    CreateBookInput input = new CreateBookInput("Clean Code", "Programming", 1L);

    when(bookRepository.existsByTitle(input.title())).thenReturn(true);

    assertThrows(DuplicateBookException.class, () -> {
      useCase.execute(input);
    });

    verify(bookRepository).existsByTitle(input.title());
    verify(bookRepository, never()).save(any());
  }
}

