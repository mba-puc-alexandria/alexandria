package com.betrybe.alexandria.application.book;

import com.betrybe.alexandria.application.book.dto.UpdateBookInput;
import com.betrybe.alexandria.application.book.dto.BookOutput;
import com.betrybe.alexandria.domain.book.Book;
import com.betrybe.alexandria.domain.book.BookRepository;
import com.betrybe.alexandria.domain.book.exception.BookNotFoundException;

public class UpdateBookUseCase {

  private final BookRepository bookRepository;

  public UpdateBookUseCase(BookRepository bookRepository) {
    this.bookRepository = bookRepository;
  }

  public BookOutput execute(Long id, UpdateBookInput input) {
    Book book = bookRepository.findById(id)
        .orElseThrow(() -> new BookNotFoundException(id));

    Book updated = book.updateWith(input.title(), input.genre(), input.publisherId());
    Book saved = bookRepository.save(updated);

    return BookOutput.from(saved);
  }
}

