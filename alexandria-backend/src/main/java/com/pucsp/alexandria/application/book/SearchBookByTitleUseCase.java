package com.pucsp.alexandria.application.book;

import com.pucsp.alexandria.application.book.dto.SearchBookOutput;
import com.pucsp.alexandria.domain.book.BookRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public class SearchBookByTitleUseCase {

  private final BookRepository bookRepository;

  public SearchBookByTitleUseCase(BookRepository bookRepository) {
    this.bookRepository = bookRepository;
  }

  public Page<SearchBookOutput> execute(String query, Pageable pageable) {
    if (query == null || query.isBlank()) {
      return Page.empty();
    }

    return bookRepository.searchBookByQuery(query, pageable)
        .map(SearchBookOutput::from);
  }
}

