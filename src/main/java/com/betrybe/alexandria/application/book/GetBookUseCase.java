package com.betrybe.alexandria.application.book;

import com.betrybe.alexandria.application.book.dto.BookOutput;
import com.betrybe.alexandria.domain.book.Book;
import com.betrybe.alexandria.domain.book.BookRepository;
import com.betrybe.alexandria.domain.book.exception.BookNotFoundException;

public class GetBookUseCase {

  private final BookRepository bookRepository;

  public GetBookUseCase(BookRepository bookRepository) {
    this.bookRepository = bookRepository;
  }

  public BookOutput execute(Long id) {
    Book book = bookRepository.findById(id)
        .orElseThrow(() -> new BookNotFoundException(id));

    return BookOutput.from(book);
  }
}

