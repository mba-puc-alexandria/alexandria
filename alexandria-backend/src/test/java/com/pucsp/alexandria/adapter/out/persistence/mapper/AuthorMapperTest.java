package com.pucsp.alexandria.adapter.out.persistence.mapper;

import static org.junit.jupiter.api.Assertions.*;

import com.pucsp.alexandria.adapter.out.persistence.entity.AuthorEntity;
import com.pucsp.alexandria.domain.author.Author;
import org.junit.jupiter.api.Test;

class AuthorMapperTest {

    private final AuthorMapper mapper = new AuthorMapper();

    @Test
    void shouldMapEntityToDomain() {
        AuthorEntity entity = new AuthorEntity(1L, "Machado de Assis", 1839, 1908);
        Author author = mapper.toDomain(entity);

        assertNotNull(author);
        assertEquals(1L, author.getId().getValue());
        assertEquals("Machado de Assis", author.getName());
        assertEquals(1839, author.getBirthYear());
        assertEquals(1908, author.getDeathYear());
    }

    @Test
    void shouldMapDomainToEntity() {
        Author author = Author.restore(1L, "Machado de Assis", 1839, 1908);
        AuthorEntity entity = mapper.toPersistence(author);

        assertNotNull(entity);
        assertEquals(1L, entity.getId());
        assertEquals("Machado de Assis", entity.getName());
        assertEquals(1839, entity.getBirthYear());
        assertEquals(1908, entity.getDeathYear());
    }

    @Test
    void shouldMapDomainWithoutIdToEntity() {
        Author author = Author.create("New Author", null, null);
        AuthorEntity entity = mapper.toPersistence(author);

        assertNull(entity.getId());
        assertEquals("New Author", entity.getName());
        assertNull(entity.getBirthYear());
        assertNull(entity.getDeathYear());
    }

    @Test
    void shouldReturnNullWhenEntityIsNull() {
        assertNull(mapper.toDomain(null));
    }

    @Test
    void shouldReturnNullWhenDomainIsNull() {
        assertNull(mapper.toPersistence(null));
    }
}
