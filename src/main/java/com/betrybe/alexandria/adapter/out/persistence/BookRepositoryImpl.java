package com.betrybe.alexandria.adapter.out.persistence;

import com.betrybe.alexandria.adapter.out.persistence.entity.BookEntity;
import com.betrybe.alexandria.adapter.out.persistence.jpa.BookJpaRepository;
import com.betrybe.alexandria.adapter.out.persistence.mapper.BookMapper;
import com.betrybe.alexandria.domain.book.Book;
import com.betrybe.alexandria.domain.book.BookRepository;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public class BookRepositoryImpl implements BookRepository {

  private final BookJpaRepository jpaRepository;
  private final BookMapper mapper;

  public BookRepositoryImpl(BookJpaRepository jpaRepository, BookMapper mapper) {
    this.jpaRepository = jpaRepository;
    this.mapper = mapper;
  }

  @Override
  public Book save(Book book) {
    BookEntity entity = mapper.toPersistence(book);
    BookEntity saved = jpaRepository.save(entity);
    return mapper.toDomain(saved);
  }

  @Override
  public Optional<Book> findById(Long id) {
    return jpaRepository.findById(id)
        .map(mapper::toDomain);
  }

  @Override
  public Page<Book> findAll(Pageable pageable) {
    return jpaRepository.findAll(pageable)
        .map(mapper::toDomain);
  }

  @Override
  public void delete(Book book) {
    BookEntity entity = mapper.toPersistence(book);
    jpaRepository.delete(entity);
  }

  @Override
  public boolean existsByTitle(String title) {
    return jpaRepository.existsByTitle(title);
  }
}

