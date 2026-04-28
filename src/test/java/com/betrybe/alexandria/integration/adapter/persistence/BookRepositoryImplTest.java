package com.betrybe.alexandria.integration.adapter.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.betrybe.alexandria.adapter.out.persistence.BookRepositoryImpl;
import com.betrybe.alexandria.domain.book.Book;
import com.betrybe.alexandria.domain.book.BookRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
@DisplayName("BookRepositoryImpl Integration Tests")
class BookRepositoryImplTest {

  @Autowired
  private BookRepository bookRepository;

  @Test
  @DisplayName("Should save and retrieve a book")
  void testSaveAndRetrieveBook() {
    Book book = Book.create("Clean Code", "Programming", 1L);
    Book saved = bookRepository.save(book);

    Optional<Book> retrieved = bookRepository.findById(saved.getId().getValue());
    assertTrue(retrieved.isPresent());
    assertEquals("Clean Code", retrieved.get().getTitle());
  }

  @Test
  @DisplayName("Should check if book exists by title")
  void testExistsByTitle() {
    Book book = Book.create("Clean Code", "Programming", 1L);
    bookRepository.save(book);

    assertTrue(bookRepository.existsByTitle("Clean Code"));
    assertFalse(bookRepository.existsByTitle("Non-existent"));
  }

  @Test
  @DisplayName("Should list books with pagination")
  void testFindAllWithPagination() {
    Book book1 = Book.create("Clean Code", "Programming", 1L);
    Book book2 = Book.create("The Pragmatic Programmer", "Software", 1L);
    bookRepository.save(book1);
    bookRepository.save(book2);

    Page<Book> page = bookRepository.findAll(PageRequest.of(0, 10));
    assertEquals(2, page.getTotalElements());
  }

  @Test
  @DisplayName("Should delete a book")
  void testDeleteBook() {
    Book book = Book.create("Clean Code", "Programming", 1L);
    Book saved = bookRepository.save(book);

    bookRepository.delete(saved);

    assertFalse(bookRepository.findById(saved.getId().getValue()).isPresent());
  }
}

