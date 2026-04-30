package com.pucsp.alexandria.application.book.dto;

import com.pucsp.alexandria.domain.book.external.BookData;

public record SearchBookOutput(
    Long gutendexId,
    String title,
    String authors,
    String downloadUrl,
    String coverUrl,
    String languages,
    String subjects,
    Integer downloadCount
) {

  public static SearchBookOutput from(BookData bookData) {
    return new SearchBookOutput(
        bookData.gutendexId(),
        bookData.title(),
        bookData.authors(),
        bookData.downloadUrl(),
        bookData.coverUrl(),
        bookData.languages(),
        bookData.subjects(),
        bookData.downloadCount()
    );
  }
}

