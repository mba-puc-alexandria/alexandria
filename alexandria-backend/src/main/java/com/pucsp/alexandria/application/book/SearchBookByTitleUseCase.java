package com.pucsp.alexandria.application.book;

import com.pucsp.alexandria.application.book.dto.SearchBookOutput;
import com.pucsp.alexandria.domain.book.Book;
import com.pucsp.alexandria.domain.book.BookRepository;
import java.util.List;

public class SearchBookByTitleUseCase {

  private final BookRepository bookRepository;

  public SearchBookByTitleUseCase(BookRepository bookRepository) {
    this.bookRepository = bookRepository;
  }

  public List<SearchBookOutput> execute(String query) {
    if (query == null || query.isBlank()) {
      return List.of();
    }

    List<Book> booksData = bookRepository.searchBookByQuery(query);

    return booksData.stream()
        .map(SearchBookOutput::from)
        .toList();
  }
}

