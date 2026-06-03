package com.pucsp.alexandria.adapter.out.persistence.jpa;

import com.pucsp.alexandria.adapter.out.persistence.entity.UserBooksEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserBooksJpaRepository extends JpaRepository<UserBooksEntity, Long> {

  Page<UserBooksEntity> findByUserId(Long userId, Pageable pageable);

  Page<UserBooksEntity> findByUserIdAndStatus(Long userId, String status, Pageable pageable);

  Optional<UserBooksEntity> findByUserIdAndBookId(Long userId, Long bookId);

  boolean existsByUserIdAndBookId(Long userId, Long bookId);
}
