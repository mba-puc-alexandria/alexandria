package com.betrybe.alexandria.application.book;

import com.betrybe.alexandria.domain.book.Book;
import com.betrybe.alexandria.domain.book.BookRepository;
import com.betrybe.alexandria.domain.book.exception.BookNotFoundException;

public class DeleteBookUseCase {

  private final BookRepository bookRepository;

  public DeleteBookUseCase(BookRepository bookRepository) {
    this.bookRepository = bookRepository;
  }

  public void execute(Long id) {
    Book book = bookRepository.findById(id)
        .orElseThrow(() -> new BookNotFoundException(id));

    bookRepository.delete(book);
  }
}

