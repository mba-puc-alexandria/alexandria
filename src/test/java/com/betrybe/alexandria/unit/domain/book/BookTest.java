package com.betrybe.alexandria.unit.domain.book;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.betrybe.alexandria.domain.book.Book;
import com.betrybe.alexandria.domain.book.BookSource;
import com.betrybe.alexandria.domain.book.exception.InvalidBookException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Book Domain Entity Tests")
class BookTest {

  @Test
  @DisplayName("Should create a new book with valid data")
  void testCreateValidBook() {
    Book book = Book.create("Clean Code", "Programming", 1L);

    assertNotNull(book.getId());
    assertEquals("Clean Code", book.getTitle());
    assertEquals("Programming", book.getGenre());
    assertEquals(1L, book.getPublisherId());
    assertEquals(BookSource.LOCAL, book.getSource());
  }

  @Test
  @DisplayName("Should fail when title is null")
  void testCreateWithNullTitle() {
    assertThrows(InvalidBookException.class, () -> {
      Book.create(null, "Programming", 1L);
    });
  }

  @Test
  @DisplayName("Should fail when title is blank")
  void testCreateWithBlankTitle() {
    assertThrows(InvalidBookException.class, () -> {
      Book.create("   ", "Programming", 1L);
    });
  }

  @Test
  @DisplayName("Should fail when title exceeds 255 characters")
  void testCreateWithLongTitle() {
    String longTitle = "a".repeat(256);
    assertThrows(InvalidBookException.class, () -> {
      Book.create(longTitle, "Programming", 1L);
    });
  }

  @Test
  @DisplayName("Should fail when genre is null")
  void testCreateWithNullGenre() {
    assertThrows(InvalidBookException.class, () -> {
      Book.create("Clean Code", null, 1L);
    });
  }

  @Test
  @DisplayName("Should fail when publisherId is null")
  void testCreateWithNullPublisherId() {
    assertThrows(InvalidBookException.class, () -> {
      Book.create("Clean Code", "Programming", null);
    });
  }

  @Test
  @DisplayName("Should fail when publisherId is zero")
  void testCreateWithZeroPublisherId() {
    assertThrows(InvalidBookException.class, () -> {
      Book.create("Clean Code", "Programming", 0L);
    });
  }

  @Test
  @DisplayName("Should restore a book from persistence")
  void testRestoreBook() {
    Book book = Book.restore(1L, "Clean Code", "Programming", 1L, BookSource.LOCAL);

    assertEquals(1L, book.getId().getValue());
    assertEquals("Clean Code", book.getTitle());
    assertEquals("Programming", book.getGenre());
    assertEquals(1L, book.getPublisherId());
    assertEquals(BookSource.LOCAL, book.getSource());
  }

  @Test
  @DisplayName("Should update book title")
  void testUpdateBookTitle() {
    Book original = Book.create("Clean Code", "Programming", 1L);
    Book updated = original.updateWith("The Pragmatic Programmer", null, null);

    assertEquals("The Pragmatic Programmer", updated.getTitle());
    assertEquals("Programming", updated.getGenre());
    assertEquals(1L, updated.getPublisherId());
  }

  @Test
  @DisplayName("Should update book genre")
  void testUpdateBookGenre() {
    Book original = Book.create("Clean Code", "Programming", 1L);
    Book updated = original.updateWith(null, "Software", null);

    assertEquals("Clean Code", updated.getTitle());
    assertEquals("Software", updated.getGenre());
    assertEquals(1L, updated.getPublisherId());
  }

  @Test
  @DisplayName("Should fail when updating with invalid title")
  void testUpdateWithInvalidTitle() {
    Book original = Book.create("Clean Code", "Programming", 1L);
    assertThrows(InvalidBookException.class, () -> {
      original.updateWith("", null, null);
    });
  }

  @Test
  @DisplayName("Should maintain equality based on ID")
  void testEqualityBasedOnId() {
    Book book1 = Book.restore(1L, "Clean Code", "Programming", 1L, BookSource.LOCAL);
    Book book2 = Book.restore(1L, "Different Title", "Different Genre", 2L, BookSource.OPEN_LIBRARY);

    assertEquals(book1, book2);
  }
}

