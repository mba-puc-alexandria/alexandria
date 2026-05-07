package com.pucsp.alexandria.adapter.out.persistence.mapper;

import static org.junit.jupiter.api.Assertions.*;

import com.pucsp.alexandria.adapter.out.persistence.entity.BookEntity;
import com.pucsp.alexandria.domain.book.Book;
import com.pucsp.alexandria.domain.book.BookSource;
import org.junit.jupiter.api.Test;

class BookMapperTest {

    private final BookMapper mapper = new BookMapper();

    @Test
    void shouldMapEntityToDomain() {
        BookEntity entity = new BookEntity(1L, "Dom Casmurro", "Machado de Assis",
                100L, "http://download.com", "http://cover.com",
                "pt", "Fiction", 5000, null, "GUTENDEX");

        Book book = mapper.toDomain(entity);

        assertNotNull(book);
        assertEquals(1L, book.getId().getValue());
        assertEquals("Dom Casmurro", book.getTitle());
        assertEquals("Machado de Assis", book.getAuthor());
        assertEquals(100L, book.getGutendexId());
        assertEquals(BookSource.GUTENDEX, book.getSource());
    }

    @Test
    void shouldMapDomainToEntity() {
        Book book = Book.restore(1L, "Clean Code", "Robert Martin",
                null, null, null, null, null, null, 5L, BookSource.LOCAL);

        BookEntity entity = mapper.toPersistence(book);

        assertNotNull(entity);
        assertEquals(1L, entity.getId());
        assertEquals("Clean Code", entity.getTitle());
        assertEquals("Robert Martin", entity.getAuthor());
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
        BookEntity entity = new BookEntity(2L, "Local Book", "", null, null, null, null, null, null, 3L, "LOCAL");
        Book book = mapper.toDomain(entity);
        assertNotNull(book);
        assertEquals(2L, book.getId().getValue());
        assertEquals("LOCAL", book.getSource().name());
        assertEquals(3L, book.getPublisherId());
    }
}
