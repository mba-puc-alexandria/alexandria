package com.pucsp.alexandria.application.book;

import com.pucsp.alexandria.application.book.dto.BookOutput;
import com.pucsp.alexandria.application.book.dto.BookOutput.AuthorInfo;
import com.pucsp.alexandria.domain.author.Author;
import com.pucsp.alexandria.domain.author.AuthorId;
import com.pucsp.alexandria.domain.author.AuthorRepository;
import com.pucsp.alexandria.domain.book.Book;
import com.pucsp.alexandria.domain.book.BookRepository;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public class ListBooksUseCase {

  private final BookRepository bookRepository;
  private final AuthorRepository authorRepository;

  public ListBooksUseCase(BookRepository bookRepository, AuthorRepository authorRepository) {
    this.bookRepository = bookRepository;
    this.authorRepository = authorRepository;
  }

  public Page<BookOutput> execute(Pageable pageable) {
    Page<Book> books = bookRepository.findAll(pageable);

    Set<AuthorId> allAuthorIds = books.stream()
        .flatMap(b -> b.getAuthorIds().stream())
        .collect(Collectors.toSet());

    List<Author> allAuthors = authorRepository.findAllById(allAuthorIds);
    Map<Long, String> authorMap = allAuthors.stream()
        .collect(Collectors.toMap(a -> a.getId().getValue(), Author::getName));

    return books.map(book -> {
      List<AuthorInfo> authorInfos = book.getAuthorIds().stream()
          .map(id -> new AuthorInfo(id.getValue(), authorMap.get(id.getValue())))
          .toList();
      return BookOutput.from(book, authorInfos);
    });
  }
}
