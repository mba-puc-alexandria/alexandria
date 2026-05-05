package com.pucsp.alexandria.domain.userbook;

import com.pucsp.alexandria.domain.user.UserId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

public interface UserBooksRepository {

  UserBooks save(UserBooks userBooks);

  Optional<UserBooks> findById(Long id);

  Page<UserBooks> findByUserId(UserId userId, Pageable pageable);

  Page<UserBooks> findByUserIdAndStatus(UserId userId, UserBooksStatus status, Pageable pageable);

  Optional<UserBooks> findByUserIdAndBookId(UserId userId, Long bookId);

  boolean existsByUserIdAndBookId(UserId userId, Long bookId);

  void delete(UserBooks userBooks);
}
