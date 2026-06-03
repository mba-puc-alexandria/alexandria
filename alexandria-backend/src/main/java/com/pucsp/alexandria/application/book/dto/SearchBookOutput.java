package com.pucsp.alexandria.application.book.dto;

import com.pucsp.alexandria.domain.book.Book;
import com.pucsp.alexandria.domain.book.external.BookData;
import java.util.List;
import java.util.stream.Collectors;

public record SearchBookOutput(
    Long id,
    Long gutendexId,
    String title,
    String authorsDisplay,
    List<Long> authorIds,
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
        List.of(),
        bookData.downloadUrl(),
        bookData.coverUrl(),
        bookData.languages(),
        bookData.subjects(),
        bookData.downloadCount()
    );
  }

  public static SearchBookOutput from(Book book, List<com.pucsp.alexandria.domain.author.Author> authors) {
    String names = authors.stream()
        .map(com.pucsp.alexandria.domain.author.Author::getName)
        .collect(Collectors.joining(", "));
    List<Long> ids = authors.stream()
        .map(a -> a.getId().getValue())
        .toList();

    return new SearchBookOutput(
        book.getId().getValue(),
        book.getGutendexId(),
        book.getTitle(),
        names,
        ids,
        book.getDownloadUrl(),
        book.getCoverUrl(),
        book.getLanguages(),
        book.getSubjects(),
        book.getDownloadCount()
    );
  }
}
