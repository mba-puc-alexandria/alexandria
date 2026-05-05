package com.pucsp.alexandria.adapter.out.persistence;

import com.pucsp.alexandria.adapter.out.persistence.entity.BookEntity;
import com.pucsp.alexandria.adapter.out.persistence.entity.UserBooksEntity;
import com.pucsp.alexandria.adapter.out.persistence.entity.UserEntity;
import com.pucsp.alexandria.adapter.out.persistence.jpa.BookJpaRepository;
import com.pucsp.alexandria.adapter.out.persistence.jpa.UserBooksJpaRepository;
import com.pucsp.alexandria.adapter.out.persistence.jpa.UserJpaRepository;
import com.pucsp.alexandria.adapter.out.persistence.mapper.UserBooksMapper;
import com.pucsp.alexandria.domain.user.UserId;
import com.pucsp.alexandria.domain.userbook.UserBooks;
import com.pucsp.alexandria.domain.userbook.UserBooksRepository;
import com.pucsp.alexandria.domain.userbook.UserBooksStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserBooksRepositoryImpl implements UserBooksRepository {

  private final UserBooksJpaRepository jpaRepository;
  private final UserJpaRepository userJpaRepository;
  private final BookJpaRepository bookJpaRepository;
  private final UserBooksMapper mapper;

  public UserBooksRepositoryImpl(
      UserBooksJpaRepository jpaRepository,
      UserJpaRepository userJpaRepository,
      BookJpaRepository bookJpaRepository,
      UserBooksMapper mapper) {
    this.jpaRepository = jpaRepository;
    this.userJpaRepository = userJpaRepository;
    this.bookJpaRepository = bookJpaRepository;
    this.mapper = mapper;
  }

  @Override
  public UserBooks save(UserBooks userBooks) {
    UserEntity userEntity = userJpaRepository.getReferenceById(userBooks.getUserId().getValue());
    BookEntity bookEntity = bookJpaRepository.getReferenceById(userBooks.getBookId());
    UserBooksEntity entity = mapper.toPersistence(userBooks, userEntity, bookEntity);

    if (userBooks.getId() != null) {
      entity.setId(userBooks.getId());
    }

    UserBooksEntity saved = jpaRepository.save(entity);
    return mapper.toDomain(saved);
  }

  @Override
  public Optional<UserBooks> findById(Long id) {
    return jpaRepository.findById(id).map(mapper::toDomain);
  }

  @Override
  public Page<UserBooks> findByUserId(UserId userId, Pageable pageable) {
    return jpaRepository.findByUserId(userId.getValue(), pageable)
        .map(mapper::toDomain);
  }

  @Override
  public Page<UserBooks> findByUserIdAndStatus(UserId userId, UserBooksStatus status, Pageable pageable) {
    return jpaRepository.findByUserIdAndStatus(userId.getValue(), status.getValue(), pageable)
        .map(mapper::toDomain);
  }

  @Override
  public Optional<UserBooks> findByUserIdAndBookId(UserId userId, Long bookId) {
    return jpaRepository.findByUserIdAndBookId(userId.getValue(), bookId)
        .map(mapper::toDomain);
  }

  @Override
  public boolean existsByUserIdAndBookId(UserId userId, Long bookId) {
    return jpaRepository.existsByUserIdAndBookId(userId.getValue(), bookId);
  }

  @Override
  public void delete(UserBooks userBooks) {
    jpaRepository.deleteById(userBooks.getId());
  }
}
