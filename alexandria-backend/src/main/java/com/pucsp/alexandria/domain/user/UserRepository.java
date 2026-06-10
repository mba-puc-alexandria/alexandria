package com.pucsp.alexandria.domain.user;

import java.util.Optional;

public interface UserRepository {

  User save(User user);

  Optional<User> findById(Long id);

  Optional<User> findByUsername(String username);

  Optional<User> findByEmail(String email);

  boolean existsByUsername(String username);

  boolean existsByEmail(String email);

  void delete(User user);
}
