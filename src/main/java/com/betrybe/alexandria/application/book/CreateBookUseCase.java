package com.betrybe.alexandria.application.book;

import com.betrybe.alexandria.application.book.dto.CreateBookInput;
import com.betrybe.alexandria.application.book.dto.CreateBookOutput;
import com.betrybe.alexandria.domain.book.Book;
import com.betrybe.alexandria.domain.book.BookRepository;
import com.betrybe.alexandria.domain.book.exception.DuplicateBookException;

public class CreateBookUseCase {

  private final BookRepository bookRepository;

  public CreateBookUseCase(BookRepository bookRepository) {
    this.bookRepository = bookRepository;
  }

  public CreateBookOutput execute(CreateBookInput input) {
    if (bookRepository.existsByTitle(input.title())) {
      throw new DuplicateBookException(input.title());
    }

    Book book = Book.create(input.title(), input.genre(), input.publisherId());
    Book saved = bookRepository.save(book);

    return new CreateBookOutput(saved.getId().getValue());
  }
}

