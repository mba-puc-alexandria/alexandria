package com.pucsp.alexandria.application.book;

import com.pucsp.alexandria.application.book.dto.BookOutput;
import com.pucsp.alexandria.application.book.dto.BookOutput.AuthorInfo;
import com.pucsp.alexandria.application.book.dto.UpdateBookInput;
import com.pucsp.alexandria.domain.author.Author;
import com.pucsp.alexandria.domain.author.AuthorRepository;
import com.pucsp.alexandria.domain.book.Book;
import com.pucsp.alexandria.domain.book.BookRepository;
import com.pucsp.alexandria.domain.book.exception.BookNotFoundException;
import java.util.List;

public class UpdateBookUseCase {

  private final BookRepository bookRepository;
  private final AuthorRepository authorRepository;

  public UpdateBookUseCase(BookRepository bookRepository, AuthorRepository authorRepository) {
    this.bookRepository = bookRepository;
    this.authorRepository = authorRepository;
  }

  public BookOutput execute(Long id, UpdateBookInput input) {
    Book book = bookRepository.findById(id)
        .orElseThrow(() -> new BookNotFoundException(id));

    Book updated = book.updateWith(input.title());
    Book saved = bookRepository.save(updated);

    List<Author> authors = authorRepository.findAllById(saved.getAuthorIds());
    List<AuthorInfo> authorInfos = authors.stream()
        .map(a -> new AuthorInfo(a.getId().getValue(), a.getName()))
        .toList();

    return BookOutput.from(saved, authorInfos);
  }
}
