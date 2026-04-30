package com.pucsp.alexandria.application.book;

import com.pucsp.alexandria.application.book.dto.UpdateBookInput;
import com.pucsp.alexandria.application.book.dto.BookOutput;
import com.pucsp.alexandria.domain.book.Book;
import com.pucsp.alexandria.domain.book.BookRepository;
import com.pucsp.alexandria.domain.book.exception.BookNotFoundException;
import org.springframework.stereotype.Service;

// melhorar método de update para atualização do livro na base
public class UpdateBookUseCase {

  private final BookRepository bookRepository;

  public UpdateBookUseCase(BookRepository bookRepository) {
    this.bookRepository = bookRepository;
  }

  public BookOutput execute(Long id, UpdateBookInput input) {
    Book book = bookRepository.findById(id)
        .orElseThrow(() -> new BookNotFoundException(id));

    String finalTitle = input.title() != null ? input.title() : book.getTitle();

    Book updated = Book.restore(
        book.getId().getValue(),
        finalTitle,
        book.getAuthor(),
        book.getGutendexId(),
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

