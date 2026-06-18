package com.pucsp.alexandria.application.userbooks;

import com.pucsp.alexandria.application.book.dto.BookOutput;
import com.pucsp.alexandria.application.book.dto.BookOutput.AuthorInfo;
import com.pucsp.alexandria.application.userbooks.dto.UserBooksOutput;
import com.pucsp.alexandria.domain.author.Author;
import com.pucsp.alexandria.domain.author.AuthorRepository;
import com.pucsp.alexandria.domain.book.BookId;
import com.pucsp.alexandria.domain.book.BookRepository;
import com.pucsp.alexandria.domain.book.exception.BookNotFoundException;
import com.pucsp.alexandria.domain.user.UserId;
import com.pucsp.alexandria.domain.userbook.UserBooksRepository;
import java.util.List;

public class GetUserBookByBookIdUseCase {

  private final UserBooksRepository userBooksRepository;
  private final BookRepository bookRepository;
  private final AuthorRepository authorRepository;

  public GetUserBookByBookIdUseCase(
      UserBooksRepository userBooksRepository,
      BookRepository bookRepository,
      AuthorRepository authorRepository) {
    this.userBooksRepository = userBooksRepository;
    this.bookRepository = bookRepository;
    this.authorRepository = authorRepository;
  }

  public UserBooksOutput execute(Long userId, Long bookId) {
    var book = bookRepository.findById(bookId)
        .orElseThrow(() -> new BookNotFoundException(bookId));

    List<Author> authors = authorRepository.findAllById(book.getAuthorIds());
    List<AuthorInfo> authorInfos = authors.stream()
        .map(a -> new AuthorInfo(a.getId().getValue(), a.getName(), a.getBirthYear(), a.getDeathYear()))
        .toList();

    BookOutput bookOutput = BookOutput.from(book, authorInfos);

    var userBookOpt = userBooksRepository.findByUserIdAndBookId(
        UserId.from(userId), BookId.from(bookId));

    if (userBookOpt.isPresent()) {
      return UserBooksOutput.from(userBookOpt.get(), bookOutput);
    }

    return new UserBooksOutput(null, bookOutput, null, null, null);
  }
}
