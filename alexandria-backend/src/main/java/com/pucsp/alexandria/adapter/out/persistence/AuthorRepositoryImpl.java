package com.pucsp.alexandria.adapter.out.persistence;

import com.pucsp.alexandria.adapter.out.persistence.jpa.AuthorJpaRepository;
import com.pucsp.alexandria.adapter.out.persistence.mapper.AuthorMapper;
import com.pucsp.alexandria.domain.author.Author;
import com.pucsp.alexandria.domain.author.AuthorId;
import com.pucsp.alexandria.domain.author.AuthorRepository;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
    var entity = mapper.toPersistence(author);
    var saved = jpaRepository.save(entity);
    return mapper.toDomain(saved);
  }

  @Override
  public Optional<Author> findById(Long id) {
    return jpaRepository.findById(id).map(mapper::toDomain);
  }

  @Override
  public Optional<Author> findByName(String name) {
    return jpaRepository.findByName(name).map(mapper::toDomain);
  }

  @Override
  public List<Author> findAllById(Set<AuthorId> ids) {
    List<Long> longIds = ids.stream()
        .map(AuthorId::getValue)
        .toList();
    return jpaRepository.findAllById(longIds).stream()
        .map(mapper::toDomain)
        .toList();
  }

  @Override
  public boolean existsByName(String name) {
    return jpaRepository.existsByName(name);
  }
}
