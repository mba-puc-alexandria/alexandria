package com.pucsp.alexandria.application.book;

import com.pucsp.alexandria.application.book.dto.SearchBookOutput;
import com.pucsp.alexandria.domain.book.external.BookApiClient;
import com.pucsp.alexandria.domain.book.external.BookData;
import java.util.List;

public class SearchBookByTitleUseCase {

  private final BookApiClient bookApiClient;

  public SearchBookByTitleUseCase(BookApiClient bookApiClient) {
    this.bookApiClient = bookApiClient;
  }

  public List<SearchBookOutput> execute(String query) {
    if (query == null || query.isBlank()) {
      return List.of();
    }

    List<BookData> booksData = bookApiClient.searchByTitle(query.trim());

    return booksData.stream()
        .map(SearchBookOutput::from)
        .toList();
  }
}

