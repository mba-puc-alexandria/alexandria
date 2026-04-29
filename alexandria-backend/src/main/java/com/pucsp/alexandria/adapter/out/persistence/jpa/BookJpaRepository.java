package com.pucsp.alexandria.adapter.out.persistence.jpa;

import com.pucsp.alexandria.adapter.out.persistence.entity.BookEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookJpaRepository extends JpaRepository<BookEntity, Long> {

  boolean existsByTitle(String title);
}

