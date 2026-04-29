package com.pucsp.alexandria.application.book;

import com.pucsp.alexandria.application.book.dto.CreateBookInput;
import com.pucsp.alexandria.application.book.dto.CreateBookOutput;
import com.pucsp.alexandria.domain.book.Book;
import com.pucsp.alexandria.domain.book.BookRepository;
import com.pucsp.alexandria.domain.book.exception.DuplicateBookException;
import com.pucsp.alexandria.domain.book.external.BookApiClient;
import com.pucsp.alexandria.domain.book.external.BookData;
import org.springframework.stereotype.Service;

@Service
public class CreateBookUseCase {

  private final BookRepository bookRepository;
  private final BookApiClient bookApiClient;

  public CreateBookUseCase(BookRepository bookRepository, BookApiClient bookApiClient) {
    this.bookRepository = bookRepository;
    this.bookApiClient = bookApiClient;
  }

  public CreateBookOutput execute(CreateBookInput input) {
    var bookDataList = bookApiClient.searchByTitle(input.title());
    if (bookDataList.isEmpty()) {
      throw new RuntimeException("Book not found in Gutendex: " + input.title());
    }

    BookData bookData = bookDataList.get(0);

    if (bookRepository.existsByGutenbergId(bookData.gutenbergId())) {
      throw new DuplicateBookException("Book with Gutenberg ID " + bookData.gutenbergId() + " already exists");
    }

    Book book = Book.createFromGutendex(
        bookData.gutenbergId(),
        bookData.title(),
        bookData.downloadUrl(),
        bookData.coverUrl(),
        bookData.languages(),
        bookData.subjects(),
        bookData.downloadCount()
    );

    Book saved = bookRepository.save(book);

    return new CreateBookOutput(saved.getId().getValue());
  }
}
