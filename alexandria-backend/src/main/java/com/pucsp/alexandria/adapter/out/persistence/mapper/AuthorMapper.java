package com.pucsp.alexandria.adapter.out.persistence.mapper;

import com.pucsp.alexandria.adapter.out.persistence.entity.AuthorEntity;
import com.pucsp.alexandria.domain.author.Author;
import org.springframework.stereotype.Component;

@Component
public class AuthorMapper {

  public Author toDomain(AuthorEntity entity) {
    if (entity == null) {
      return null;
    }
    return Author.restore(
        entity.getId(),
        entity.getName(),
        entity.getBiography()
    );
  }

  public AuthorEntity toPersistence(Author author) {
    if (author == null) {
      return null;
    }
    return new AuthorEntity(
        author.getId() != null ? author.getId().getValue() : null,
        author.getName(),
        author.getBiography()
    );
  }
}

