package com.pucsp.alexandria.application.userbooks;

import com.pucsp.alexandria.application.book.dto.BookOutput;
import com.pucsp.alexandria.application.userbooks.dto.UserBooksOutput;
import com.pucsp.alexandria.domain.book.BookRepository;
import com.pucsp.alexandria.domain.user.UserId;
import com.pucsp.alexandria.domain.userbook.UserBooksRepository;
import com.pucsp.alexandria.domain.userbook.UserBooksStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public class ListUserBooksUseCase {

  private final UserBooksRepository userBooksRepository;
  private final BookRepository bookRepository;

  public ListUserBooksUseCase(UserBooksRepository userBooksRepository, BookRepository bookRepository) {
    this.userBooksRepository = userBooksRepository;
    this.bookRepository = bookRepository;
  }

  public Page<UserBooksOutput> execute(Long userId, String status, Pageable pageable) {
    UserId userIdVO = UserId.from(userId);

    Page<com.pucsp.alexandria.domain.userbook.UserBooks> userBooksPage;
    if (status != null && !status.isBlank()) {
      UserBooksStatus statusEnum = UserBooksStatus.fromString(status);
      userBooksPage = userBooksRepository.findByUserIdAndStatus(userIdVO, statusEnum, pageable);
    } else {
      userBooksPage = userBooksRepository.findByUserId(userIdVO, pageable);
    }

    return userBooksPage.map(ub -> {
      var book = bookRepository.findById(ub.getBookId().getValue())
          .orElseThrow(() -> new RuntimeException("Book not found for user book: " + ub.getId()));
      return UserBooksOutput.from(ub, BookOutput.from(book));
    });
  }
}
