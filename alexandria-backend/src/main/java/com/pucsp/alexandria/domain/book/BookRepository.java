package com.pucsp.alexandria.domain.book;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookRepository {

  Book save(Book book);

  Optional<Book> findById(Long id);

  Optional<Book> findByGutendexId(Long gutendexId);

  Page<Book> findAll(Pageable pageable);

  Page<Book> findByLanguage(String language, Pageable pageable);

  Page<Book> searchBookByQuery(String query, Pageable pageable);

  void delete(Book book);

  boolean existsByGutendexId(Long gutendexId);
}

