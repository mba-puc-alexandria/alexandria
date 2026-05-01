package com.pucsp.alexandria.adapter.out.persistence;

import com.pucsp.alexandria.adapter.out.persistence.entity.BookEntity;
import com.pucsp.alexandria.adapter.out.persistence.jpa.BookJpaRepository;
import com.pucsp.alexandria.adapter.out.persistence.mapper.BookMapper;
import com.pucsp.alexandria.domain.book.Book;
import com.pucsp.alexandria.domain.book.BookRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
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
    if (entity.getId() == null && book.getGutendexId() != null) {
      jpaRepository.findByGutendexId(book.getGutendexId())
          .ifPresent(existing -> entity.setId(existing.getId()));
    }
    BookEntity saved = jpaRepository.save(entity);
    return mapper.toDomain(saved);
  }

  @Override
  public Optional<Book> findById(Long id) {
    return jpaRepository.findById(id)
        .map(mapper::toDomain);
  }

  @Override
  public Optional<Book> findByGutendexId(Long gutendexId) {
    return jpaRepository.findByGutendexId(gutendexId)
        .map(mapper::toDomain);
  }

  @Override
  public Page<Book> findAll(Pageable pageable) {
    return jpaRepository.findAll(pageable)
        .map(mapper::toDomain);
  }

  @Override
  public List<Book> searchBookByQuery(String query) {
    return jpaRepository.searchByTitleOrAuthor(query)
        .stream()
        .map(mapper::toDomain)
        .collect(Collectors.toList());
  }

  @Override
  public void delete(Book book) {
    BookEntity entity = mapper.toPersistence(book);
    jpaRepository.delete(entity);
  }

  @Override
  public boolean existsByGutendexId(Long gutendexId) {
    return jpaRepository.existsByGutendexId(gutendexId);
  }
}
