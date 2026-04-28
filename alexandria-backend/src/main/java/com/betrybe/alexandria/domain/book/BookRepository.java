package com.betrybe.alexandria.domain.book;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookRepository {

  Book save(Book book);

  Optional<Book> findById(Long id);

  Page<Book> findAll(Pageable pageable);

  void delete(Book book);

  boolean existsByTitle(String title);
}

