package com.pucsp.alexandria.adapter.in.rest.dto;

import com.pucsp.alexandria.application.book.dto.BookOutput;
import java.util.List;

public record BookResponse(
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

  public static BookResponse from(BookOutput output) {
    return new BookResponse(
        output.id(),
        output.title(),
        output.authors().stream()
            .map(a -> new AuthorInfo(a.id(), a.name(), a.birthYear(), a.deathYear()))
            .toList(),
        output.gutendexId(),
        output.downloadUrl(),
        output.coverUrl(),
        output.languages(),
        output.subjects(),
        output.downloadCount(),
        output.publisherId(),
        output.source()
    );
  }
}
