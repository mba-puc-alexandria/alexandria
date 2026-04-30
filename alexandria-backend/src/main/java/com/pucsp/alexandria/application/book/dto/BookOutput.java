package com.pucsp.alexandria.application.book.dto;

import com.pucsp.alexandria.domain.book.Book;
import com.pucsp.alexandria.domain.book.BookId;

public record BookOutput(
    BookId id,
    String title,
    String author,
    Long gutendexId,
    String downloadUrl,
    String coverUrl,
    String languages,
    String subjects,
    Integer downloadCount,
    Long publisherId,
    String source
) {

  public static BookOutput from(Book book) {
    return new BookOutput(
        book.getId(),
        book.getTitle(),
        book.getAuthor(),
        book.getGutendexId(),
        book.getDownloadUrl(),
        book.getCoverUrl(),
        book.getLanguages(),
        book.getSubjects(),
        book.getDownloadCount(),
        book.getPublisherId(),
        book.getSource().name()
    );
  }
}

