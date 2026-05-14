package com.pucsp.alexandria.adapter.out.persistence.jpa;

import static org.junit.jupiter.api.Assertions.*;

import com.pucsp.alexandria.adapter.out.persistence.entity.AuthorEntity;
import com.pucsp.alexandria.adapter.out.persistence.entity.BookEntity;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(properties = {
    "jwt.secret=test-secret-key-for-tests-min-256-bits",
    "jwt.expiration-ms=86400000"
})
class BookJpaRepositoryTest {

    @Autowired
    private BookJpaRepository bookJpaRepository;

    @Autowired
    private AuthorJpaRepository authorJpaRepository;

    @Test
    void shouldSaveAndFindBook() {
        AuthorEntity author = authorJpaRepository.save(new AuthorEntity(null, "Machado de Assis", 1839, 1908));
        BookEntity book = new BookEntity(null, "Dom Casmurro", Set.of(author), null, null, null, "pt", null, null, null, "GUTENDEX");
        BookEntity saved = bookJpaRepository.save(book);

        Optional<BookEntity> found = bookJpaRepository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals("Dom Casmurro", found.get().getTitle());
    }

    @Test
    void shouldCheckIfGutendexIdExists() {
        AuthorEntity author = authorJpaRepository.save(new AuthorEntity(null, "Author", null, null));
        BookEntity book = new BookEntity(null, "Book", Set.of(author), 100L, null, null, null, null, null, null, "GUTENDEX");
        bookJpaRepository.save(book);

        assertTrue(bookJpaRepository.existsByGutendexId(100L));
        assertFalse(bookJpaRepository.existsByGutendexId(999L));
    }

    @Test
    void shouldFindByGutendexId() {
        AuthorEntity author = authorJpaRepository.save(new AuthorEntity(null, "Author", null, null));
        bookJpaRepository.save(new BookEntity(null, "Book", Set.of(author), 200L, null, null, null, null, null, null, "GUTENDEX"));

        Optional<BookEntity> found = bookJpaRepository.findByGutendexId(200L);

        assertTrue(found.isPresent());
    }

    @Test
    void shouldSearchByTitleOrAuthor() {
        AuthorEntity author1 = authorJpaRepository.save(new AuthorEntity(null, "Machado de Assis", 1839, 1908));
        AuthorEntity author2 = authorJpaRepository.save(new AuthorEntity(null, "João Cabral", null, null));

        bookJpaRepository.save(new BookEntity(null, "Dom Casmurro", Set.of(author1), null, null, null, null, null, null, null, "GUTENDEX"));
        bookJpaRepository.save(new BookEntity(null, "Outro Livro", Set.of(author2), null, null, null, null, null, null, null, "GUTENDEX"));

        Page<BookEntity> results = bookJpaRepository.searchByTitleOrAuthor("Machado", PageRequest.of(0, 10));

        assertEquals(1, results.getContent().size());
        assertEquals("Dom Casmurro", results.getContent().get(0).getTitle());
    }

    @Test
    void shouldSearchByAuthorName() {
        AuthorEntity author = authorJpaRepository.save(new AuthorEntity(null, "Clarice Lispector", 1920, 1977));
        bookJpaRepository.save(new BookEntity(null, "A Hora da Estrela", Set.of(author), null, null, null, null, null, null, null, "GUTENDEX"));

        Page<BookEntity> results = bookJpaRepository.searchByTitleOrAuthor("Clarice", PageRequest.of(0, 10));

        assertEquals(1, results.getContent().size());
    }
}
