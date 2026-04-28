package com.betrybe.alexandria.domain.publisher;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PublisherRepository {

  Publisher save(Publisher publisher);

  Optional<Publisher> findById(Long id);

  Page<Publisher> findAll(Pageable pageable);

  void delete(Publisher publisher);

  boolean existsByName(String name);
}

