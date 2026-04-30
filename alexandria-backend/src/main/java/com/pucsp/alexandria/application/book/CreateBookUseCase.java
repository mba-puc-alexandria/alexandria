package com.pucsp.alexandria.application.book;

import com.pucsp.alexandria.application.book.dto.CreateBookInput;
import com.pucsp.alexandria.application.book.dto.CreateBookOutput;
import com.pucsp.alexandria.domain.book.Book;
import com.pucsp.alexandria.domain.book.BookRepository;
import com.pucsp.alexandria.domain.book.external.BookApiClient;
import com.pucsp.alexandria.domain.book.external.BookData;
import java.util.ArrayList;

public class CreateBookUseCase {

  private final BookRepository bookRepository;
  private final BookApiClient bookApiClient;

  public CreateBookUseCase(BookRepository bookRepository, BookApiClient bookApiClient) {
    this.bookRepository = bookRepository;
    this.bookApiClient = bookApiClient;
  }

  public CreateBookOutput execute(CreateBookInput input) {
    var bookDataList = bookApiClient.getPage(input.page());
    if (bookDataList.isEmpty()) {
      throw new RuntimeException("Page not found in Gutendex: " + input.page());
    }

    ArrayList<Long> createdIds = new ArrayList<>();
    for (BookData bookData : bookDataList) {
      Book newBook = Book.createFromGutendex(
          bookData.gutendexId(),
          bookData.title(),
          bookData.authors(),
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
}
