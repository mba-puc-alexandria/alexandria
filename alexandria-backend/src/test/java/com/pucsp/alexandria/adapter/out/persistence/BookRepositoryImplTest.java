package com.pucsp.alexandria.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.*;

import com.pucsp.alexandria.adapter.out.persistence.mapper.BookMapper;
import com.pucsp.alexandria.domain.book.Book;
import com.pucsp.alexandria.domain.book.BookSource;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@DataJpaTest
@Import({BookRepositoryImpl.class, BookMapper.class})
class BookRepositoryImplTest {

    @Autowired
    private BookRepositoryImpl bookRepository;

    @Test
    void shouldSaveAndFindBook() {
        Book book = Book.createFromGutendex(1L, "Clean Code", "Robert C. Martin", null, null, "en", null, 0);
        Book saved = bookRepository.save(book);

        assertNotNull(saved.getId());
        assertEquals("Clean Code", saved.getTitle());
        assertEquals(BookSource.GUTENDEX, saved.getSource());
    }

    @Test
    void shouldFindById() {
        Book saved = bookRepository.save(Book.createFromGutendex(2L, "Title", "Author", null, null, "en", null, 0));

        Optional<Book> found = bookRepository.findById(saved.getId().getValue());

        assertTrue(found.isPresent());
        assertEquals("Title", found.get().getTitle());
    }

    @Test
    void shouldReturnEmptyWhenNotFound() {
        Optional<Book> found = bookRepository.findById(999L);
        assertFalse(found.isPresent());
    }

    @Test
    void shouldFindAllPaged() {
        bookRepository.save(Book.createFromGutendex(3L, "Book 1", "Author A", null, null, "en", null, 0));
        bookRepository.save(Book.createFromGutendex(4L, "Book 2", "Author B", null, null, "en", null, 0));

        Page<Book> all = bookRepository.findAll(PageRequest.of(0, 10));

        assertEquals(2, all.getTotalElements());
    }

    @Test
    void shouldSearchByQuery() {
        bookRepository.save(Book.createFromGutendex(5L, "Dom Casmurro", "Machado de Assis", null, null, "pt", null, 0));
        bookRepository.save(Book.createFromGutendex(6L, "Memorias Postumas", "Machado de Assis", null, null, "pt", null, 0));

        Page<Book> result = bookRepository.searchBookByQuery("dom", PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        assertEquals("Dom Casmurro", result.getContent().get(0).getTitle());
    }

    @Test
    void shouldDeleteBook() {
        Book saved = bookRepository.save(Book.createFromGutendex(7L, "To Delete", "Author", null, null, "en", null, 0));

        bookRepository.delete(saved);

        assertFalse(bookRepository.findById(saved.getId().getValue()).isPresent());
    }

    @Test
    void shouldSaveGutendexBook() {
        Book book = Book.createFromGutendex(500L, "Gutendex Book", "Author",
                "url", "url", "en", "Fiction", 100);
        Book saved = bookRepository.save(book);

        assertNotNull(saved.getId());
        assertTrue(bookRepository.existsByGutendexId(500L));
    }

    @Test
    void shouldFindByGutendexId() {
        bookRepository.save(Book.createFromGutendex(600L, "Title", "Author",
                null, null, "en", null, 0));

        Optional<Book> found = bookRepository.findByGutendexId(600L);

        assertTrue(found.isPresent());
    }

    @Test
    void shouldCheckExistenceByGutendexId() {
        assertFalse(bookRepository.existsByGutendexId(999L));

        bookRepository.save(Book.createFromGutendex(700L, "Title", "Author",
                null, null, "en", null, 0));

        assertTrue(bookRepository.existsByGutendexId(700L));
    }
}
