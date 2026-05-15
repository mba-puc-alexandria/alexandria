package com.pucsp.alexandria.domain.author;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface AuthorRepository {

  Author save(Author author);

  Optional<Author> findById(Long id);

  Optional<Author> findByName(String name);

  List<Author> findAllById(Set<AuthorId> ids);

  boolean existsByName(String name);
}
