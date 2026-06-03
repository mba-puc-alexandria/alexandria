package com.pucsp.alexandria.application.book.dto;

import com.pucsp.alexandria.domain.book.Book;
import java.util.List;

public record BookOutput(
    Long id,
    String title,
    List<AuthorInfo> authors,
    Long gutendexId,
    String downloadUrl,
    String coverUrl,
    String languages,
    String subjects,
    Integer downloadCount,
    Long publisherId,
    String source
) {

  public record AuthorInfo(Long id, String name, Integer birthYear, Integer deathYear) {}

  public static BookOutput from(Book book, List<AuthorInfo> authors) {
    return new BookOutput(
        book.getId().getValue(),
        book.getTitle(),
        authors,
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
