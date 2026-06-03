package com.pucsp.alexandria.adapter.out.persistence.jpa;

import com.pucsp.alexandria.adapter.out.persistence.entity.AuthorEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthorJpaRepository extends JpaRepository<AuthorEntity, Long> {

  Optional<AuthorEntity> findByName(String name);

  List<AuthorEntity> findByNameIn(List<String> names);

  boolean existsByName(String name);
}
