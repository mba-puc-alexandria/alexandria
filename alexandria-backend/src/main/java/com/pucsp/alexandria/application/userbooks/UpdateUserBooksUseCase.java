package com.pucsp.alexandria.application.userbooks;

import com.pucsp.alexandria.application.book.dto.BookOutput;
import com.pucsp.alexandria.application.book.dto.BookOutput.AuthorInfo;
import com.pucsp.alexandria.application.userbooks.dto.UpdateUserBooksInput;
import com.pucsp.alexandria.application.userbooks.dto.UserBooksOutput;
import com.pucsp.alexandria.domain.author.Author;
import com.pucsp.alexandria.domain.author.AuthorRepository;
import com.pucsp.alexandria.domain.book.BookRepository;
import com.pucsp.alexandria.domain.book.exception.BookNotFoundException;
import com.pucsp.alexandria.domain.userbook.UserBooks;
import com.pucsp.alexandria.domain.userbook.UserBooksRepository;
import com.pucsp.alexandria.domain.userbook.UserBooksStatus;
import com.pucsp.alexandria.domain.userbook.exception.UserBooksNotFoundException;
import java.util.List;

public class UpdateUserBooksUseCase {

  private final UserBooksRepository userBooksRepository;
  private final BookRepository bookRepository;
  private final AuthorRepository authorRepository;

  public UpdateUserBooksUseCase(
      UserBooksRepository userBooksRepository,
      BookRepository bookRepository,
      AuthorRepository authorRepository) {
    this.userBooksRepository = userBooksRepository;
    this.bookRepository = bookRepository;
    this.authorRepository = authorRepository;
  }

  public UserBooksOutput execute(Long userId, Long id, UpdateUserBooksInput input) {
    UserBooks userBooks = userBooksRepository.findById(id)
        .orElseThrow(() -> new UserBooksNotFoundException("UserBook not found with id: " + id));

    if (!userBooks.getUserId().getValue().equals(userId)) {
      throw new UserBooksNotFoundException("UserBook not found with id: " + id);
    }

    UserBooksStatus newStatus = input.status() != null
        ? UserBooksStatus.fromString(input.status()) : null;

    UserBooks updated = userBooks.updateWith(newStatus, input.progress(), input.rating());
    UserBooks saved = userBooksRepository.save(updated);

    var book = bookRepository.findById(saved.getBookId().getValue())
        .orElseThrow(() -> new BookNotFoundException("Book not found with id: " + saved.getBookId()));

    List<Author> authors = authorRepository.findAllById(book.getAuthorIds());
    List<AuthorInfo> authorInfos = authors.stream()
        .map(a -> new AuthorInfo(a.getId().getValue(), a.getName()))
        .toList();

    return UserBooksOutput.from(saved, BookOutput.from(book, authorInfos));
  }
}
