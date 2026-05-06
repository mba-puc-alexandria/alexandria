package com.pucsp.alexandria.adapter.out.persistence.jpa;

import com.pucsp.alexandria.adapter.out.persistence.entity.BookEntity;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BookJpaRepository extends JpaRepository<BookEntity, Long> {

  boolean existsByGutendexId(Long gutendexId);

  Optional<BookEntity> findByGutendexId(Long gutendexId);

  @Query("SELECT b FROM BookEntity b WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
      "OR LOWER(b.author) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
  Page<BookEntity> searchByTitleOrAuthor(@Param("searchTerm") String query, Pageable pageable);
}

