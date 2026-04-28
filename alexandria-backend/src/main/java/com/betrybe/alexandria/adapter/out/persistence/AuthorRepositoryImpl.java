package com.betrybe.alexandria.adapter.out.persistence;

import com.betrybe.alexandria.adapter.out.persistence.entity.AuthorEntity;
import com.betrybe.alexandria.adapter.out.persistence.jpa.AuthorJpaRepository;
import com.betrybe.alexandria.adapter.out.persistence.mapper.AuthorMapper;
import com.betrybe.alexandria.domain.author.Author;
import com.betrybe.alexandria.domain.author.AuthorRepository;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public class AuthorRepositoryImpl implements AuthorRepository {

  private final AuthorJpaRepository jpaRepository;
  private final AuthorMapper mapper;

  public AuthorRepositoryImpl(AuthorJpaRepository jpaRepository, AuthorMapper mapper) {
    this.jpaRepository = jpaRepository;
    this.mapper = mapper;
  }

  @Override
  public Author save(Author author) {
    AuthorEntity entity = mapper.toPersistence(author);
    AuthorEntity saved = jpaRepository.save(entity);
    return mapper.toDomain(saved);
  }

  @Override
  public Optional<Author> findById(Long id) {
    return jpaRepository.findById(id)
        .map(mapper::toDomain);
  }

  @Override
  public Page<Author> findAll(Pageable pageable) {
    return jpaRepository.findAll(pageable)
        .map(mapper::toDomain);
  }

  @Override
  public void delete(Author author) {
    AuthorEntity entity = mapper.toPersistence(author);
    jpaRepository.delete(entity);
  }

  @Override
  public boolean existsByName(String name) {
    return jpaRepository.existsByName(name);
  }
}

