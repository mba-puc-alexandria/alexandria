package com.pucsp.alexandria.application.book.dto;

import com.pucsp.alexandria.domain.book.Book;

public record BookOutput(
    Long id,
    String title,
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
        book.getId().getValue(),
        book.getTitle(),
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

