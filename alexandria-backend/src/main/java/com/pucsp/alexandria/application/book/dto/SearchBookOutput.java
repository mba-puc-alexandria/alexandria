package com.pucsp.alexandria.application.book.dto;

import com.pucsp.alexandria.domain.book.Book;
import com.pucsp.alexandria.domain.book.external.BookData;

public record SearchBookOutput(
    Long id,
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
        bookData.id(),
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

  public static SearchBookOutput from(Book book) {
    return new SearchBookOutput(
        book.getId().getValue(),
        book.getGutendexId(),
        book.getTitle(),
        book.getAuthor(),
        book.getDownloadUrl(),
        book.getCoverUrl(),
        book.getLanguages(),
        book.getSubjects(),
        book.getDownloadCount()
    );
  }
}

