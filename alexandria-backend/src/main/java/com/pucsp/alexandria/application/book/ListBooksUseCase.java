package com.pucsp.alexandria.application.book;

import com.pucsp.alexandria.application.book.dto.BookOutput;
import com.pucsp.alexandria.domain.book.BookRepository;
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

