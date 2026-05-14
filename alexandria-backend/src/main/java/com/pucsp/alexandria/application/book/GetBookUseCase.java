package com.pucsp.alexandria.application.book;

import com.pucsp.alexandria.application.book.dto.BookOutput;
import com.pucsp.alexandria.application.book.dto.BookOutput.AuthorInfo;
import com.pucsp.alexandria.domain.author.Author;
import com.pucsp.alexandria.domain.author.AuthorRepository;
import com.pucsp.alexandria.domain.book.Book;
import com.pucsp.alexandria.domain.book.BookRepository;
import com.pucsp.alexandria.domain.book.exception.BookNotFoundException;
import java.util.List;

public class GetBookUseCase {

  private final BookRepository bookRepository;
  private final AuthorRepository authorRepository;

  public GetBookUseCase(BookRepository bookRepository, AuthorRepository authorRepository) {
    this.bookRepository = bookRepository;
    this.authorRepository = authorRepository;
  }

  public BookOutput execute(Long id) {
    Book book = bookRepository.findById(id)
        .orElseThrow(() -> new BookNotFoundException(id));

    List<Author> authors = authorRepository.findAllById(book.getAuthorIds());
    List<AuthorInfo> authorInfos = authors.stream()
        .map(a -> new AuthorInfo(a.getId().getValue(), a.getName()))
        .toList();

    return BookOutput.from(book, authorInfos);
  }
}
