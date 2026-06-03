package com.pucsp.alexandria.application.userbooks.dto;

import com.pucsp.alexandria.application.book.dto.BookOutput;
import com.pucsp.alexandria.domain.userbook.UserBooks;

public record UserBooksOutput(
    Long id,
    BookOutput book,
    String status,
    Integer progress,
    Integer rating
) {

  public static UserBooksOutput from(UserBooks userBooks, BookOutput bookOutput) {
    return new UserBooksOutput(
        userBooks.getId(),
        bookOutput,
        userBooks.getStatus().getValue(),
        userBooks.getProgress(),
        userBooks.getRating()
    );
  }
}
