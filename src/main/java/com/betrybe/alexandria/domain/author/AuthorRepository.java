package com.betrybe.alexandria.domain.author;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AuthorRepository {

  Author save(Author author);

  Optional<Author> findById(Long id);

  Page<Author> findAll(Pageable pageable);

  void delete(Author author);

  boolean existsByName(String name);
}

