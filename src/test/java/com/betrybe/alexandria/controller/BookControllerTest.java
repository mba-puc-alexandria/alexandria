package com.betrybe.alexandria.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.betrybe.alexandria.controller.dto.BookCreationDto;
import com.betrybe.alexandria.controller.dto.BookDetailCreationDto;
import com.betrybe.alexandria.entity.Book;
import com.betrybe.alexandria.entity.BookDetail;
import com.betrybe.alexandria.service.BookService;
import com.betrybe.alexandria.service.PublisherService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@DisplayName("BookController Tests")
@ExtendWith(MockitoExtension.class)
class BookControllerTest {

  @Mock
  private BookService bookService;

  @Mock
  private PublisherService publisherService;

  @InjectMocks
  private BookController bookController;

  @Test
  @DisplayName("Should call bookService.findById when getBookById is called")
  void testGetBookByIdCallsService() {
    // Arrange
    Long bookId = 1L;
    Book book = new Book("Title", "Genre");
    when(bookService.findById(bookId)).thenReturn(book);

    // Act
    bookController.getBookById(bookId);

    // Assert
    verify(bookService).findById(bookId);
  }

  @Test
  @DisplayName("Should call bookService.findAll when getAllBooks is called")
  void testGetAllBooksCallsService() {
    // Arrange
    var pageable = PageRequest.of(0, 10);
    var emptyPage = new PageImpl<Book>(List.of(), pageable, 0);
    when(bookService.findAll(pageable)).thenReturn(emptyPage);

    // Act
    bookController.getAllBooks(pageable);

    // Assert
    verify(bookService).findAll(pageable);
  }

  @Test
  @DisplayName("Should call bookService.create when createBook is called")
  void testCreateBookCallsService() {
    // Arrange
    var dto = new BookCreationDto("Title", "Genre", 1L);
    Book book = new Book("Title", "Genre");
    when(bookService.create(any(Book.class))).thenReturn(book);

    // Act
    bookController.createBook(dto);

    // Assert
    verify(bookService).create(any(Book.class));
  }

  @Test
  @DisplayName("Should call bookService.createBatch when createBooks is called")
  void testCreateBooksCallsService() {
    // Arrange
    var dtos = List.of(
        new BookCreationDto("Book1", "Genre1", 1L),
        new BookCreationDto("Book2", "Genre2", 1L)
    );
    when(bookService.createBatch(any(List.class))).thenReturn(List.of(
        new Book("Book1", "Genre1"),
        new Book("Book2", "Genre2")
    ));

    // Act
    bookController.createBooks(dtos);

    // Assert
    verify(bookService).createBatch(any(List.class));
  }

  @Test
  @DisplayName("Should call bookService.update when updateBook is called")
  void testUpdateBookCallsService() {
    // Arrange
    Long bookId = 1L;
    var dto = new BookCreationDto("Updated", "Genre", 1L);
    Book book = new Book("Updated", "Genre");
    when(bookService.update(eq(bookId), any(Book.class))).thenReturn(book);

    // Act
    bookController.updateBook(bookId, dto);

    // Assert
    verify(bookService).update(eq(bookId), any(Book.class));
  }

  @Test
  @DisplayName("Should call bookService.deleteById when deleteBookById is called")
  void testDeleteBookByIdCallsService() {
    // Arrange
    Long bookId = 1L;
    doNothing().when(bookService).deleteById(bookId);

    // Act
    bookController.deleteBookById(bookId);

    // Assert
    verify(bookService).deleteById(bookId);
  }

  @Test
  @DisplayName("Should call bookService.createBookDetail when createBookDetail is called")
  void testCreateBookDetailCallsService() {
    // Arrange
    Long bookId = 1L;
    var dto = new BookDetailCreationDto("Summary", 300, "2023", "ISBN123");
    Book book = new Book("Title", "Genre");
    BookDetail detail = new BookDetail();
    detail.setBook(book);
    detail.setSummary("Summary");
    when(bookService.findById(bookId)).thenReturn(book);
    when(bookService.createBookDetail(eq(bookId), any(BookDetail.class))).thenReturn(detail);

    // Act
    bookController.createBookDetail(bookId, dto);

    // Assert
    verify(bookService).createBookDetail(eq(bookId), any(BookDetail.class));
  }

  @Test
  @DisplayName("Should call bookService.getBookDetail when getBookDetail is called")
  void testGetBookDetailCallsService() {
    // Arrange
    Long bookId = 1L;
    Book book = new Book("Title", "Genre");
    BookDetail detail = new BookDetail();
    detail.setBook(book);
    when(bookService.getBookDetail(bookId)).thenReturn(detail);

    // Act
    bookController.getBookDetail(bookId);

    // Assert
    verify(bookService).getBookDetail(bookId);
  }

  @Test
  @DisplayName("Should call bookService.updateBookDetail when updateBookDetail is called")
  void testUpdateBookDetailCallsService() {
    // Arrange
    Long bookId = 1L;
    var dto = new BookDetailCreationDto("Summary", 300, "2023", "ISBN123");
    Book book = new Book("Title", "Genre");
    BookDetail detail = new BookDetail();
    detail.setBook(book);
    when(bookService.findById(bookId)).thenReturn(book);
    when(bookService.updateBookDetail(eq(bookId), any(BookDetail.class))).thenReturn(detail);

    // Act
    bookController.updateBookDetail(bookId, dto);

    // Assert
    verify(bookService).updateBookDetail(eq(bookId), any(BookDetail.class));
  }

  @Test
  @DisplayName("Should call bookService.removeBookDetail when removeBookDetail is called")
  void testRemoveBookDetailCallsService() {
    // Arrange
    Long bookId = 1L;
    doNothing().when(bookService).removeBookDetail(bookId);

    // Act
    bookController.removeBookDetail(bookId);

    // Assert
    verify(bookService).removeBookDetail(bookId);
  }
}
