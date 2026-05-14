package com.pucsp.alexandria.application.book;

import com.pucsp.alexandria.application.book.dto.CreateBookInput;
import com.pucsp.alexandria.application.book.dto.CreateBookOutput;
import com.pucsp.alexandria.domain.author.Author;
import com.pucsp.alexandria.domain.author.AuthorId;
import com.pucsp.alexandria.domain.author.AuthorRepository;
import com.pucsp.alexandria.domain.book.Book;
import com.pucsp.alexandria.domain.book.BookRepository;
import com.pucsp.alexandria.domain.book.external.BookApiClient;
import com.pucsp.alexandria.domain.book.external.BookData;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import org.springframework.transaction.annotation.Transactional;

public class CreateBookUseCase {

  private final BookRepository bookRepository;
  private final AuthorRepository authorRepository;
  private final BookApiClient bookApiClient;

  public CreateBookUseCase(BookRepository bookRepository, AuthorRepository authorRepository, BookApiClient bookApiClient) {
    this.bookRepository = bookRepository;
    this.authorRepository = authorRepository;
    this.bookApiClient = bookApiClient;
  }

  @Transactional
  public CreateBookOutput execute(CreateBookInput input) {
    var bookDataList = bookApiClient.getPage(input.page());
    if (bookDataList.isEmpty()) {
      throw new RuntimeException("Page not found in Gutendex: " + input.page());
    }

    ArrayList<Long> createdIds = new ArrayList<>();
    for (BookData bookData : bookDataList) {
      if (bookRepository.existsByGutendexId(bookData.gutendexId())) {
        continue;
      }

      Set<AuthorId> authorIds = findOrCreateAuthors(bookData);

      Book newBook = Book.createFromGutendex(
          bookData.gutendexId(),
          bookData.title(),
          authorIds,
          bookData.downloadUrl(),
          bookData.coverUrl(),
          bookData.languages(),
          bookData.subjects(),
          bookData.downloadCount()
      );

      Book saved = bookRepository.save(newBook);
      createdIds.add(saved.getId().getValue());
    }

    return new CreateBookOutput(createdIds);
  }

  private Set<AuthorId> findOrCreateAuthors(BookData bookData) {
    Set<AuthorId> authorIds = new HashSet<>();
    for (int i = 0; i < bookData.authorNames().size(); i++) {
      String name = bookData.authorNames().get(i);
      Integer birthYear = !bookData.birthYears().isEmpty() ? bookData.birthYears().get(i) : null;
      Integer deathYear = !bookData.deathYears().isEmpty() ? bookData.deathYears().get(i) : null;

      Author author = authorRepository.findByName(name)
          .orElseGet(() -> authorRepository.save(Author.create(name, birthYear, deathYear)));

      authorIds.add(author.getId());
    }
    return authorIds;
  }
}
