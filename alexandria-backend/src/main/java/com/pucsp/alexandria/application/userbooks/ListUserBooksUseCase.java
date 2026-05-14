package com.pucsp.alexandria.application.userbooks;

import com.pucsp.alexandria.application.book.dto.BookOutput;
import com.pucsp.alexandria.application.book.dto.BookOutput.AuthorInfo;
import com.pucsp.alexandria.application.userbooks.dto.UserBooksOutput;
import com.pucsp.alexandria.domain.author.Author;
import com.pucsp.alexandria.domain.author.AuthorId;
import com.pucsp.alexandria.domain.author.AuthorRepository;
import com.pucsp.alexandria.domain.book.Book;
import com.pucsp.alexandria.domain.book.BookRepository;
import com.pucsp.alexandria.domain.user.UserId;
import com.pucsp.alexandria.domain.userbook.UserBooksRepository;
import com.pucsp.alexandria.domain.userbook.UserBooksStatus;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public class ListUserBooksUseCase {

  private final UserBooksRepository userBooksRepository;
  private final BookRepository bookRepository;
  private final AuthorRepository authorRepository;

  public ListUserBooksUseCase(
      UserBooksRepository userBooksRepository,
      BookRepository bookRepository,
      AuthorRepository authorRepository) {
    this.userBooksRepository = userBooksRepository;
    this.bookRepository = bookRepository;
    this.authorRepository = authorRepository;
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

    Set<Long> bookIds = userBooksPage.stream()
        .map(ub -> ub.getBookId().getValue())
        .collect(Collectors.toSet());

    List<Book> books = bookIds.stream()
        .map(id -> bookRepository.findById(id).orElse(null))
        .filter(b -> b != null)
        .toList();

    Set<AuthorId> allAuthorIds = books.stream()
        .flatMap(b -> b.getAuthorIds().stream())
        .collect(Collectors.toSet());

    List<Author> allAuthors = authorRepository.findAllById(allAuthorIds);
    Map<Long, String> authorMap = allAuthors.stream()
        .collect(Collectors.toMap(a -> a.getId().getValue(), Author::getName));

    Map<Long, BookOutput> bookOutputMap = books.stream()
        .collect(Collectors.toMap(
            b -> b.getId().getValue(),
            b -> {
              List<AuthorInfo> infos = b.getAuthorIds().stream()
                  .map(id -> new AuthorInfo(id.getValue(), authorMap.get(id.getValue())))
                  .toList();
              return BookOutput.from(b, infos);
            }
        ));

    return userBooksPage.map(ub -> {
      BookOutput bookOutput = bookOutputMap.get(ub.getBookId().getValue());
      return UserBooksOutput.from(ub, bookOutput);
    });
  }
}
