package com.betrybe.alexandria.adapter.out.persistence.jpa;

import com.betrybe.alexandria.adapter.out.persistence.entity.PublisherEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PublisherJpaRepository extends JpaRepository<PublisherEntity, Long> {

  boolean existsByName(String name);
}

