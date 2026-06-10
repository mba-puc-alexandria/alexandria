package com.pucsp.alexandria.adapter.out.persistence;

import com.pucsp.alexandria.adapter.out.persistence.jpa.UserJpaRepository;
import com.pucsp.alexandria.adapter.out.persistence.mapper.UserMapper;
import com.pucsp.alexandria.domain.user.User;
import com.pucsp.alexandria.domain.user.UserRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserRepositoryImpl implements UserRepository {

  private final UserJpaRepository jpaRepository;
  private final UserMapper mapper;

  public UserRepositoryImpl(UserJpaRepository jpaRepository, UserMapper mapper) {
    this.jpaRepository = jpaRepository;
    this.mapper = mapper;
  }

  @Override
  public User save(User user) {
    var entity = mapper.toPersistence(user);
    var saved = jpaRepository.save(entity);
    return mapper.toDomain(saved);
  }

  @Override
  public Optional<User> findById(Long id) {
    return jpaRepository.findById(id).map(mapper::toDomain);
  }

  @Override
  public Optional<User> findByUsername(String username) {
    return jpaRepository.findByUsername(username).map(mapper::toDomain);
  }

  @Override
  public Optional<User> findByEmail(String email) {
    return jpaRepository.findByEmail(email).map(mapper::toDomain);
  }

  @Override
  public boolean existsByUsername(String username) {
    return jpaRepository.existsByUsername(username);
  }

  @Override
  public boolean existsByEmail(String email) {
    return jpaRepository.existsByEmail(email);
  }

  @Override
  public void delete(User user) {
    jpaRepository.deleteById(user.getId().getValue());
  }
}
