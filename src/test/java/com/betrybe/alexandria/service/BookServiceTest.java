package com.betrybe.alexandria.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.betrybe.alexandria.controller.dto.BookCreationDto;
import com.betrybe.alexandria.entity.Book;
import com.betrybe.alexandria.entity.BookDetail;
import com.betrybe.alexandria.entity.Publisher;
import com.betrybe.alexandria.exception.BookDetailNotFoundException;
import com.betrybe.alexandria.exception.BookNotFoundException;
import com.betrybe.alexandria.exception.EmptyBookListException;
import com.betrybe.alexandria.repository.BookDetailRepository;
import com.betrybe.alexandria.repository.BookRepository;
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

@DisplayName("BookService Tests")
@ExtendWith(MockitoExtension.class)
class BookServiceTest {

  @Mock
  private BookRepository bookRepository;

  @Mock
  private BookDetailRepository bookDetailRepository;

  @Mock
  private PublisherService publisherService;

  @InjectMocks
  private BookService bookService;

  @Test
  @DisplayName("Should find book by id successfully")
  void testFindByIdSuccess() {
    // Arrange
    Long bookId = 1L;
    Book book = new Book("The Great Gatsby", "Fiction");
    when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));

    // Act
    Book result = bookService.findById(bookId);

    // Assert
    assertNotNull(result);
    assertEquals("The Great Gatsby", result.getTitle());
    assertEquals("Fiction", result.getGenre());
    verify(bookRepository).findById(bookId);
  }

  @Test
  @DisplayName("Should throw BookNotFoundException when book not found")
  void testFindByIdNotFound() {
    // Arrange
    Long bookId = 999L;
    when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(BookNotFoundException.class, () -> bookService.findById(bookId));
    verify(bookRepository).findById(bookId);
  }

  @Test
  @DisplayName("Should find all books with pagination")
  void testFindAllSuccess() {
    // Arrange
    Book book1 = new Book("Book 1", "Genre1");
    Book book2 = new Book("Book 2", "Genre2");
    Pageable pageable = PageRequest.of(0, 10);
    Page<Book> bookPage = new PageImpl<>(List.of(book1, book2), pageable, 2);
    when(bookRepository.findAll(pageable)).thenReturn(bookPage);

    // Act
    Page<Book> result = bookService.findAll(pageable);

    // Assert
    assertNotNull(result);
    assertEquals(2, result.getContent().size());
    verify(bookRepository).findAll(pageable);
  }

  @Test
  @DisplayName("Should create book successfully")
  void testCreateSuccess() {
    // Arrange
    Book book = new Book("New Book", "Adventure");
    when(bookRepository.save(book)).thenReturn(book);

    // Act
    Book result = bookService.create(book);

    // Assert
    assertNotNull(result);
    assertEquals("New Book", result.getTitle());
    assertEquals("Adventure", result.getGenre());
    verify(bookRepository).save(book);
  }

  @Test
  @DisplayName("Should create batch of books successfully")
  void testCreateBatchSuccess() {
    // Arrange
    Publisher publisher = new Publisher("Publisher1", "Address1");
    BookCreationDto dto1 = new BookCreationDto("Book1", "Genre1", 1L);
    BookCreationDto dto2 = new BookCreationDto("Book2", "Genre2", 1L);
    List<BookCreationDto> booksDto = List.of(dto1, dto2);

    Book book1 = new Book("Book1", "Genre1");
    Book book2 = new Book("Book2", "Genre2");

    when(publisherService.findById(1L)).thenReturn(publisher);
    when(bookRepository.save(any(Book.class))).thenReturn(book1, book2);

    // Act
    List<Book> result = bookService.createBatch(booksDto);

    // Assert
    assertNotNull(result);
    assertEquals(2, result.size());
    verify(bookRepository, times(2)).save(any(Book.class));
  }

  @Test
  @DisplayName("Should throw EmptyBookListException when list is empty")
  void testCreateBatchEmptyList() {
    // Arrange
    List<BookCreationDto> emptyList = List.of();

    // Act & Assert
    assertThrows(EmptyBookListException.class, () -> bookService.createBatch(emptyList));
    verify(bookRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should throw EmptyBookListException when list is null")
  void testCreateBatchNullList() {
    // Arrange & Act & Assert
    assertThrows(EmptyBookListException.class, () -> bookService.createBatch(null));
    verify(bookRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should update book title successfully")
  void testUpdateTitleSuccess() {
    // Arrange
    Long bookId = 1L;
    Book existingBook = new Book("Old Title", "Genre");
    Book updateData = new Book("New Title", null);

    when(bookRepository.findById(bookId)).thenReturn(Optional.of(existingBook));
    when(bookRepository.save(existingBook)).thenReturn(existingBook);

    // Act
    Book result = bookService.update(bookId, updateData);

    // Assert
    assertNotNull(result);
    assertEquals("New Title", result.getTitle());
    assertEquals("Genre", result.getGenre());
    verify(bookRepository).findById(bookId);
    verify(bookRepository).save(existingBook);
  }

  @Test
  @DisplayName("Should update book genre successfully")
  void testUpdateGenreSuccess() {
    // Arrange
    Long bookId = 1L;
    Book existingBook = new Book("Title", "OldGenre");
    Book updateData = new Book(null, "NewGenre");

    when(bookRepository.findById(bookId)).thenReturn(Optional.of(existingBook));
    when(bookRepository.save(existingBook)).thenReturn(existingBook);

    // Act
    Book result = bookService.update(bookId, updateData);

    // Assert
    assertNotNull(result);
    assertEquals("Title", result.getTitle());
    assertEquals("NewGenre", result.getGenre());
    verify(bookRepository).findById(bookId);
    verify(bookRepository).save(existingBook);
  }

  @Test
  @DisplayName("Should update book publisher successfully")
  void testUpdatePublisherSuccess() {
    // Arrange
    Long bookId = 1L;
    Publisher publisher = new Publisher("New Publisher", "Address");
    Book existingBook = new Book("Title", "Genre");
    Book updateData = new Book(null, null);
    updateData.setPublisher(publisher);

    when(bookRepository.findById(bookId)).thenReturn(Optional.of(existingBook));
    when(publisherService.findById(publisher.getId())).thenReturn(publisher);
    when(bookRepository.save(existingBook)).thenReturn(existingBook);

    // Act
    Book result = bookService.update(bookId, updateData);

    // Assert
    assertNotNull(result);
    verify(bookRepository).findById(bookId);
    verify(publisherService).findById(publisher.getId());
    verify(bookRepository).save(existingBook);
  }

  @Test
  @DisplayName("Should throw BookNotFoundException when updating non-existent book")
  void testUpdateNotFound() {
    // Arrange
    Long bookId = 999L;
    Book updateData = new Book("Title", "Genre");
    when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(BookNotFoundException.class, () -> bookService.update(bookId, updateData));
    verify(bookRepository).findById(bookId);
    verify(bookRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should delete book successfully")
  void testDeleteByIdSuccess() {
    // Arrange
    Long bookId = 1L;
    Book book = new Book("Title", "Genre");
    when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));

    // Act
    bookService.deleteById(bookId);

    // Assert
    verify(bookRepository).findById(bookId);
    verify(bookRepository).delete(book);
  }

  @Test
  @DisplayName("Should throw BookNotFoundException when deleting non-existent book")
  void testDeleteByIdNotFound() {
    // Arrange
    Long bookId = 999L;
    when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(BookNotFoundException.class, () -> bookService.deleteById(bookId));
    verify(bookRepository).findById(bookId);
    verify(bookRepository, never()).delete(any());
  }

  @Test
  @DisplayName("Should create book detail successfully")
  void testCreateBookDetailSuccess() {
    // Arrange
    Long bookId = 1L;
    Book book = new Book("Title", "Genre");
    BookDetail bookDetail = new BookDetail();
    bookDetail.setSummary("Summary");
    bookDetail.setPageCount(300);
    bookDetail.setYear("2023");
    bookDetail.setIsbn("123456");

    when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
    when(bookDetailRepository.save(bookDetail)).thenReturn(bookDetail);

    // Act
    BookDetail result = bookService.createBookDetail(bookId, bookDetail);

    // Assert
    assertNotNull(result);
    assertEquals(book, result.getBook());
    assertEquals("Summary", result.getSummary());
    verify(bookRepository).findById(bookId);
    verify(bookDetailRepository).save(bookDetail);
  }

  @Test
  @DisplayName("Should throw BookNotFoundException when creating detail for non-existent book")
  void testCreateBookDetailBookNotFound() {
    // Arrange
    Long bookId = 999L;
    BookDetail bookDetail = new BookDetail();
    when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(BookNotFoundException.class,
        () -> bookService.createBookDetail(bookId, bookDetail));
    verify(bookRepository).findById(bookId);
    verify(bookDetailRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should get book detail successfully")
  void testGetBookDetailSuccess() {
    // Arrange
    Long bookId = 1L;
    Book book = new Book("Title", "Genre");
    BookDetail bookDetail = new BookDetail();
    bookDetail.setSummary("Summary");
    book.setBookDetail(bookDetail);

    when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));

    // Act
    BookDetail result = bookService.getBookDetail(bookId);

    // Assert
    assertNotNull(result);
    assertEquals("Summary", result.getSummary());
    verify(bookRepository).findById(bookId);
  }

  @Test
  @DisplayName("Should throw BookNotFoundException when getting detail for non-existent book")
  void testGetBookDetailBookNotFound() {
    // Arrange
    Long bookId = 999L;
    when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(BookNotFoundException.class, () -> bookService.getBookDetail(bookId));
    verify(bookRepository).findById(bookId);
  }

  @Test
  @DisplayName("Should throw BookDetailNotFoundException when book has no detail")
  void testGetBookDetailNotFound() {
    // Arrange
    Long bookId = 1L;
    Book book = new Book("Title", "Genre");
    when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));

    // Act & Assert
    assertThrows(BookDetailNotFoundException.class, () -> bookService.getBookDetail(bookId));
    verify(bookRepository).findById(bookId);
  }

  @Test
  @DisplayName("Should update book detail successfully")
  void testUpdateBookDetailSuccess() {
    // Arrange
    Long bookId = 1L;
    Book book = new Book("Title", "Genre");
    BookDetail existingDetail = new BookDetail();
    existingDetail.setSummary("Old Summary");
    existingDetail.setPageCount(200);
    existingDetail.setYear("2022");
    existingDetail.setIsbn("OLD123");
    book.setBookDetail(existingDetail);

    BookDetail updateData = new BookDetail();
    updateData.setSummary("New Summary");
    updateData.setPageCount(300);
    updateData.setYear("2023");
    updateData.setIsbn("NEW123");

    when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
    when(bookDetailRepository.save(existingDetail)).thenReturn(existingDetail);

    // Act
    BookDetail result = bookService.updateBookDetail(bookId, updateData);

    // Assert
    assertNotNull(result);
    assertEquals("New Summary", result.getSummary());
    assertEquals(300, result.getPageCount());
    assertEquals("2023", result.getYear());
    assertEquals("NEW123", result.getIsbn());
    verify(bookRepository).findById(bookId);
    verify(bookDetailRepository).save(existingDetail);
  }

  @Test
  @DisplayName("Should throw BookDetailNotFoundException when updating detail for book without detail")
  void testUpdateBookDetailNotFound() {
    // Arrange
    Long bookId = 1L;
    Book book = new Book("Title", "Genre");
    BookDetail updateData = new BookDetail();
    updateData.setSummary("Summary");

    when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));

    // Act & Assert
    assertThrows(BookDetailNotFoundException.class,
        () -> bookService.updateBookDetail(bookId, updateData));
    verify(bookRepository).findById(bookId);
    verify(bookDetailRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should remove book detail successfully")
  void testRemoveBookDetailSuccess() {
    // Arrange
    Long bookId = 1L;
    Book book = new Book("Title", "Genre");
    BookDetail bookDetail = new BookDetail();
    book.setBookDetail(bookDetail);

    when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));

    // Act
    bookService.removeBookDetail(bookId);

    // Assert
    verify(bookRepository).findById(bookId);
    verify(bookDetailRepository).delete(bookDetail);
  }

  @Test
  @DisplayName("Should throw BookDetailNotFoundException when removing detail from book without detail")
  void testRemoveBookDetailNotFound() {
    // Arrange
    Long bookId = 1L;
    Book book = new Book("Title", "Genre");

    when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));

    // Act & Assert
    assertThrows(BookDetailNotFoundException.class, () -> bookService.removeBookDetail(bookId));
    verify(bookRepository).findById(bookId);
    verify(bookDetailRepository, never()).delete(any());
  }
}

