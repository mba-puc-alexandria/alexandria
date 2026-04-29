package com.pucsp.alexandria.adapter.in.rest.dto;

import com.pucsp.alexandria.application.book.dto.SearchBookOutput;

public record SearchBookResponse(
    Long gutenbergId,
    String title,
    String authors,
    String downloadUrl,
    String coverUrl,
    String languages,
    String subjects,
    Integer downloadCount
) {

  public static SearchBookResponse from(SearchBookOutput output) {
    return new SearchBookResponse(
        output.gutenbergId(),
        output.title(),
        output.authors(),
        output.downloadUrl(),
        output.coverUrl(),
        output.languages(),
        output.subjects(),
        output.downloadCount()
    );
  }
}

