package com.betrybe.alexandria.adapter.out.persistence.mapper;

import com.betrybe.alexandria.adapter.out.persistence.entity.BookEntity;
import com.betrybe.alexandria.domain.book.Book;
import com.betrybe.alexandria.domain.book.BookSource;
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
        entity.getGenre(),
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
        book.getGenre(),
        book.getPublisherId(),
        book.getSource().name()
    );
  }
}

