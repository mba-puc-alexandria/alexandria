package com.pucsp.alexandria.adapter.out.persistence.mapper;

import com.pucsp.alexandria.adapter.out.persistence.entity.AuthorEntity;
import com.pucsp.alexandria.adapter.out.persistence.entity.BookEntity;
import com.pucsp.alexandria.domain.book.Book;
import com.pucsp.alexandria.domain.book.BookSource;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class BookMapper {

  public Book toDomain(BookEntity entity) {
    if (entity == null) {
      return null;
    }
    BookSource source = BookSource.valueOf(entity.getSource());
    Set<Long> authorIds = entity.getAuthors().stream()
        .map(AuthorEntity::getId)
        .collect(Collectors.toSet());
    return Book.restore(
        entity.getId(),
        entity.getTitle(),
        authorIds,
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
        new HashSet<>(),
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
