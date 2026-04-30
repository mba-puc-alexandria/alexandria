package com.pucsp.alexandria.application.book;

import com.pucsp.alexandria.application.book.dto.BookOutput;
import com.pucsp.alexandria.domain.book.Book;
import com.pucsp.alexandria.domain.book.BookRepository;
import com.pucsp.alexandria.domain.book.exception.BookNotFoundException;

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

