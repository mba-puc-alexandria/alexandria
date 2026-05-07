package com.pucsp.alexandria.adapter.out.persistence.jpa;

import static org.junit.jupiter.api.Assertions.*;

import com.pucsp.alexandria.adapter.out.persistence.entity.BookEntity;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@DataJpaTest
class BookJpaRepositoryTest {

    @Autowired
    private BookJpaRepository bookJpaRepository;

    @Test
    void shouldSaveAndFindBookById() {
        BookEntity entity = new BookEntity(null, "Dom Casmurro", "Machado de Assis",
                100L, "url", "url", "pt", "Fiction", 5000, null, "GUTENDEX");
        BookEntity saved = bookJpaRepository.save(entity);

        Optional<BookEntity> found = bookJpaRepository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals("Dom Casmurro", found.get().getTitle());
        assertEquals(100L, found.get().getGutendexId());
    }

    @Test
    void shouldCheckExistenceByGutendexId() {
        BookEntity entity = new BookEntity(null, "Title", "Author",
                200L, null, null, null, null, null, null, "GUTENDEX");
        bookJpaRepository.save(entity);

        assertTrue(bookJpaRepository.existsByGutendexId(200L));
        assertFalse(bookJpaRepository.existsByGutendexId(999L));
    }

    @Test
    void shouldFindByGutendexId() {
        BookEntity entity = new BookEntity(null, "Title", "Author",
                300L, null, null, null, null, null, null, "GUTENDEX");
        bookJpaRepository.save(entity);

        Optional<BookEntity> found = bookJpaRepository.findByGutendexId(300L);
        assertTrue(found.isPresent());
        assertEquals("Title", found.get().getTitle());
    }

    @Test
    void shouldSearchByTitleOrAuthor() {
        BookEntity book1 = new BookEntity(null, "Dom Casmurro", "Machado de Assis",
                null, null, null, null, null, null, 1L, "LOCAL");
        BookEntity book2 = new BookEntity(null, "Memorias Postumas", "Machado de Assis",
                null, null, null, null, null, null, 1L, "LOCAL");
        bookJpaRepository.save(book1);
        bookJpaRepository.save(book2);

        Page<BookEntity> result = bookJpaRepository.searchByTitleOrAuthor("Machado", PageRequest.of(0, 10));

        assertEquals(2, result.getTotalElements());
    }

    @Test
    void shouldReturnAllBooks() {
        bookJpaRepository.save(new BookEntity(null, "B1", "A1", null, null, null, null, null, null, 1L, "LOCAL"));
        bookJpaRepository.save(new BookEntity(null, "B2", "A2", null, null, null, null, null, null, 1L, "LOCAL"));

        Page<BookEntity> all = bookJpaRepository.findAll(PageRequest.of(0, 10));

        assertEquals(2, all.getTotalElements());
    }

    @Test
    void shouldDeleteBook() {
        BookEntity saved = bookJpaRepository.save(
                new BookEntity(null, "To Delete", "Author", null, null, null, null, null, null, 1L, "LOCAL"));

        bookJpaRepository.delete(saved);

        assertFalse(bookJpaRepository.findById(saved.getId()).isPresent());
    }
}
