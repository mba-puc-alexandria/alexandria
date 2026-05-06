package com.pucsp.alexandria.application.userbooks;

import com.pucsp.alexandria.application.book.dto.BookOutput;
import com.pucsp.alexandria.application.userbooks.dto.AddUserBooksInput;
import com.pucsp.alexandria.application.userbooks.dto.UserBooksOutput;
import com.pucsp.alexandria.domain.book.BookRepository;
import com.pucsp.alexandria.domain.book.BookId;
import com.pucsp.alexandria.domain.book.exception.BookNotFoundException;
import com.pucsp.alexandria.domain.user.UserId;
import com.pucsp.alexandria.domain.userbook.UserBooks;
import com.pucsp.alexandria.domain.userbook.UserBooksRepository;
import com.pucsp.alexandria.domain.userbook.UserBooksStatus;
import com.pucsp.alexandria.domain.userbook.exception.DuplicateUserBooksException;
import com.pucsp.alexandria.domain.userbook.exception.InvalidUserBooksException;

public class AddUserBooksUseCase {

  private final UserBooksRepository userBooksRepository;
  private final BookRepository bookRepository;

  public AddUserBooksUseCase(UserBooksRepository userBooksRepository, BookRepository bookRepository) {
    this.userBooksRepository = userBooksRepository;
    this.bookRepository = bookRepository;
  }

  public UserBooksOutput execute(Long userId, AddUserBooksInput input) {
    if (input.bookId() == null) {
      throw new InvalidUserBooksException("bookId is required");
    }

    UserId userIdVO = UserId.from(userId);

    var book = bookRepository.findById(input.bookId())
        .orElseThrow(() -> new BookNotFoundException("Book not found with id: " + input.bookId()));

    BookId bookId = book.getId();

    if (userBooksRepository.existsByUserIdAndBookId(userIdVO, bookId)) {
      throw new DuplicateUserBooksException("Book already in user's library");
    }

    UserBooksStatus status = input.status() != null
        ? UserBooksStatus.fromString(input.status())
        : UserBooksStatus.TOREAD;

    UserBooks userBooks = UserBooks.create(userIdVO, bookId, status);
    UserBooks saved = userBooksRepository.save(userBooks);

    BookOutput bookOutput = BookOutput.from(book);
    return UserBooksOutput.from(saved, bookOutput);
  }
}
