package com.betrybe.alexandria.adapter.out.persistence;

import com.betrybe.alexandria.adapter.out.persistence.entity.PublisherEntity;
import com.betrybe.alexandria.adapter.out.persistence.jpa.PublisherJpaRepository;
import com.betrybe.alexandria.adapter.out.persistence.mapper.PublisherMapper;
import com.betrybe.alexandria.domain.publisher.Publisher;
import com.betrybe.alexandria.domain.publisher.PublisherRepository;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public class PublisherRepositoryImpl implements PublisherRepository {

  private final PublisherJpaRepository jpaRepository;
  private final PublisherMapper mapper;

  public PublisherRepositoryImpl(PublisherJpaRepository jpaRepository, PublisherMapper mapper) {
    this.jpaRepository = jpaRepository;
    this.mapper = mapper;
  }

  @Override
  public Publisher save(Publisher publisher) {
    PublisherEntity entity = mapper.toPersistence(publisher);
    PublisherEntity saved = jpaRepository.save(entity);
    return mapper.toDomain(saved);
  }

  @Override
  public Optional<Publisher> findById(Long id) {
    return jpaRepository.findById(id)
        .map(mapper::toDomain);
  }

  @Override
  public Page<Publisher> findAll(Pageable pageable) {
    return jpaRepository.findAll(pageable)
        .map(mapper::toDomain);
  }

  @Override
  public void delete(Publisher publisher) {
    PublisherEntity entity = mapper.toPersistence(publisher);
    jpaRepository.delete(entity);
  }

  @Override
  public boolean existsByName(String name) {
    return jpaRepository.existsByName(name);
  }
}

