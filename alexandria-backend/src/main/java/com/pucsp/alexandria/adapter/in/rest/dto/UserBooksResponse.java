package com.pucsp.alexandria.adapter.in.rest.dto;

import com.pucsp.alexandria.application.userbooks.dto.UserBooksOutput;

public record UserBooksResponse(
    Long id,
    BookSummaryResponse book,
    String status,
    Integer progress,
    Integer rating
) {

  public static UserBooksResponse from(UserBooksOutput output) {
    return new UserBooksResponse(
        output.id(),
        BookSummaryResponse.from(output.book()),
        output.status(),
        output.progress(),
        output.rating()
    );
  }
}
