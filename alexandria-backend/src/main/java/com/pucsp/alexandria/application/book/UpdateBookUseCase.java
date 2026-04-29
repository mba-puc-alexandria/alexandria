package com.pucsp.alexandria.application.book;

import com.pucsp.alexandria.application.book.dto.UpdateBookInput;
import com.pucsp.alexandria.application.book.dto.BookOutput;
import com.pucsp.alexandria.domain.book.Book;
import com.pucsp.alexandria.domain.book.BookRepository;
import com.pucsp.alexandria.domain.book.exception.BookNotFoundException;
import org.springframework.stereotype.Service;

/**
 * @deprecated Gutendex books are immutable after import. Only LOCAL books can be updated.
 * This use case is kept for backward compatibility but recommend deprecating updates for GUTENDEX books.
 */
@Deprecated(forRemoval = true, since = "1.0")
@Service
public class UpdateBookUseCase {

  private final BookRepository bookRepository;

  public UpdateBookUseCase(BookRepository bookRepository) {
    this.bookRepository = bookRepository;
  }

  public BookOutput execute(Long id, UpdateBookInput input) {
    Book book = bookRepository.findById(id)
        .orElseThrow(() -> new BookNotFoundException(id));

    // Only allow title updates for now
    String finalTitle = input.title() != null ? input.title() : book.getTitle();

    // For LOCAL books, create a new updated instance
    // For GUTENDEX books, consider them immutable
    Book updated = Book.restore(
        book.getId().getValue(),
        finalTitle,
        book.getGutenbergId(),
        book.getDownloadUrl(),
        book.getCoverUrl(),
        book.getLanguages(),
        book.getSubjects(),
        book.getDownloadCount(),
        book.getPublisherId(),
        book.getSource()
    );

    Book saved = bookRepository.save(updated);

    return BookOutput.from(saved);
  }
}

