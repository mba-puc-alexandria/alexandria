package com.pucsp.alexandria.domain.book;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookRepository {

  Book save(Book book);

  Optional<Book> findById(Long id);

  Optional<Book> findByGutenbergId(Long gutenbergId);

  Page<Book> findAll(Pageable pageable);

  void delete(Book book);

  boolean existsByGutenbergId(Long gutenbergId);
}

