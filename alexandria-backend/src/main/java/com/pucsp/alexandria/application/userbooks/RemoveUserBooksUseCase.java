package com.pucsp.alexandria.application.userbooks;

import com.pucsp.alexandria.domain.userbook.UserBooks;
import com.pucsp.alexandria.domain.userbook.UserBooksRepository;
import com.pucsp.alexandria.domain.userbook.exception.UserBooksNotFoundException;

public class RemoveUserBooksUseCase {

  private final UserBooksRepository userBooksRepository;

  public RemoveUserBooksUseCase(UserBooksRepository userBooksRepository) {
    this.userBooksRepository = userBooksRepository;
  }

  public void execute(Long userId, Long id) {
    UserBooks userBooks = userBooksRepository.findById(id)
        .orElseThrow(() -> new UserBooksNotFoundException("UserBook not found with id: " + id));

    if (!userBooks.getUserId().getValue().equals(userId)) {
      throw new UserBooksNotFoundException("UserBook not found with id: " + id);
    }

    userBooksRepository.delete(userBooks);
  }
}
