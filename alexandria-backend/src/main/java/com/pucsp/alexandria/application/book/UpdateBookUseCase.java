package com.pucsp.alexandria.application.book;

import com.pucsp.alexandria.application.book.dto.UpdateBookInput;
import com.pucsp.alexandria.application.book.dto.BookOutput;
import com.pucsp.alexandria.domain.book.Book;
import com.pucsp.alexandria.domain.book.BookRepository;
import com.pucsp.alexandria.domain.book.exception.BookNotFoundException;

public class UpdateBookUseCase {

  private final BookRepository bookRepository;

  public UpdateBookUseCase(BookRepository bookRepository) {
    this.bookRepository = bookRepository;
  }

  public BookOutput execute(Long id, UpdateBookInput input) {
    Book book = bookRepository.findById(id)
        .orElseThrow(() -> new BookNotFoundException(id));

    Book updated = book.updateWith(input.title());
    Book saved = bookRepository.save(updated);

    return BookOutput.from(saved);
  }
}

