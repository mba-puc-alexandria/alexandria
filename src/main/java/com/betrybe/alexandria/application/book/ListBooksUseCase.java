package com.betrybe.alexandria.application.book;

import com.betrybe.alexandria.application.book.dto.BookOutput;
import com.betrybe.alexandria.domain.book.BookRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public class ListBooksUseCase {

  private final BookRepository bookRepository;

  public ListBooksUseCase(BookRepository bookRepository) {
    this.bookRepository = bookRepository;
  }

  public Page<BookOutput> execute(Pageable pageable) {
    return bookRepository.findAll(pageable)
        .map(BookOutput::from);
  }
}

