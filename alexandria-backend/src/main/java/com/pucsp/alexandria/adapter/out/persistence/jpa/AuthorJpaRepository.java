package com.pucsp.alexandria.adapter.out.persistence.jpa;

import com.pucsp.alexandria.adapter.out.persistence.entity.AuthorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthorJpaRepository extends JpaRepository<AuthorEntity, Long> {

  boolean existsByName(String name);
}

