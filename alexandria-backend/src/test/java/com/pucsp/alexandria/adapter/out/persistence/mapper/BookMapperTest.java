package com.pucsp.alexandria.adapter.out.persistence.mapper;

import static org.junit.jupiter.api.Assertions.*;

import com.pucsp.alexandria.adapter.out.persistence.entity.AuthorEntity;
import com.pucsp.alexandria.adapter.out.persistence.entity.BookEntity;
import com.pucsp.alexandria.domain.book.Book;
import com.pucsp.alexandria.domain.book.BookSource;
import java.util.Set;
import org.junit.jupiter.api.Test;

class BookMapperTest {

    private final BookMapper mapper = new BookMapper();

    @Test
    void shouldMapEntityToDomain() {
        AuthorEntity author = new AuthorEntity(1L, "Machado de Assis", null, null);
        BookEntity entity = new BookEntity(1L, "Dom Casmurro", Set.of(author),
                100L, "http://download.com", "http://cover.com",
                "pt", "Fiction", 5000, null, "GUTENDEX");

        Book book = mapper.toDomain(entity);

        assertNotNull(book);
        assertEquals(1L, book.getId().getValue());
        assertEquals("Dom Casmurro", book.getTitle());
        assertEquals(Set.of(1L), Set.copyOf(book.getAuthorIds().stream().map(id -> id.getValue()).toList()));
        assertEquals(100L, book.getGutendexId());
        assertEquals(BookSource.GUTENDEX, book.getSource());
    }

    @Test
    void shouldMapDomainToEntity() {
        Set<Long> authorIds = Set.of(1L);
        Book book = Book.restore(1L, "Clean Code", authorIds,
                null, null, null, null, null, null, 5L, BookSource.LOCAL);

        BookEntity entity = mapper.toPersistence(book);

        assertNotNull(entity);
        assertEquals(1L, entity.getId());
        assertEquals("Clean Code", entity.getTitle());
        assertTrue(entity.getAuthors().isEmpty());
        assertNull(entity.getGutendexId());
        assertEquals(5L, entity.getPublisherId());
        assertEquals("LOCAL", entity.getSource());
    }

    @Test
    void shouldReturnNullWhenEntityIsNull() {
        assertNull(mapper.toDomain(null));
    }

    @Test
    void shouldReturnNullWhenDomainIsNull() {
        assertNull(mapper.toPersistence(null));
    }

    @Test
    void shouldMapLocalEntityWithoutGutendexFields() {
        AuthorEntity author = new AuthorEntity(1L, "Author", null, null);
        BookEntity entity = new BookEntity(2L, "Local Book", Set.of(author),
                null, null, null, null, null, null, 3L, "LOCAL");
        Book book = mapper.toDomain(entity);
        assertNotNull(book);
        assertEquals(2L, book.getId().getValue());
        assertEquals("LOCAL", book.getSource().name());
        assertEquals(3L, book.getPublisherId());
        assertFalse(book.getAuthorIds().isEmpty());
    }
}
