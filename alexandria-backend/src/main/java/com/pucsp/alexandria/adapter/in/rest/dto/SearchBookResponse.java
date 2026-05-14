package com.pucsp.alexandria.adapter.in.rest.dto;

import com.pucsp.alexandria.application.book.dto.SearchBookOutput;
import java.util.List;

public record SearchBookResponse(
    Long id,
    Long gutendexId,
    String title,
    String authors,
    List<Long> authorIds,
    String downloadUrl,
    String coverUrl,
    String languages,
    String subjects,
    Integer downloadCount
) {

  public static SearchBookResponse from(SearchBookOutput output) {
    return new SearchBookResponse(
        output.id(),
        output.gutendexId(),
        output.title(),
        output.authorsDisplay(),
        output.authorIds(),
        output.downloadUrl(),
        output.coverUrl(),
        output.languages(),
        output.subjects(),
        output.downloadCount()
    );
  }
}
