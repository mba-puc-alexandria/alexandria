package com.pucsp.alexandria.adapter.out.persistence.mapper;

import com.pucsp.alexandria.adapter.out.persistence.entity.BookEntity;
import com.pucsp.alexandria.domain.book.Book;
import com.pucsp.alexandria.domain.book.BookSource;
import org.springframework.stereotype.Component;

@Component
public class BookMapper {

  public Book toDomain(BookEntity entity) {
    if (entity == null) {
      return null;
    }
    BookSource source = BookSource.valueOf(entity.getSource());
    return Book.restore(
        entity.getId(),
        entity.getTitle(),
        entity.getGutendexId(),
        entity.getDownloadUrl(),
        entity.getCoverUrl(),
        entity.getLanguages(),
        entity.getSubjects(),
        entity.getDownloadCount(),
        entity.getPublisherId(),
        source
    );
  }

  public BookEntity toPersistence(Book book) {
    if (book == null) {
      return null;
    }
    return new BookEntity(
        book.getId() != null ? book.getId().getValue() : null,
        book.getTitle(),
        book.getGutendexId(),
        book.getDownloadUrl(),
        book.getCoverUrl(),
        book.getLanguages(),
        book.getSubjects(),
        book.getDownloadCount(),
        book.getPublisherId(),
        book.getSource().name()
    );
  }
}

