package com.pucsp.alexandria.adapter.out.persistence.entity;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class BookEntityTest {

    @Test
    void shouldCreateEntityWithAllFields() {
        BookEntity entity = new BookEntity(1L, "Dom Casmurro", "Machado de Assis",
                100L, "http://download.com", "http://cover.com",
                "pt", "Fiction", 5000, null, "GUTENDEX");

        assertEquals(1L, entity.getId());
        assertEquals("Dom Casmurro", entity.getTitle());
        assertEquals("Machado de Assis", entity.getAuthor());
        assertEquals(100L, entity.getGutendexId());
        assertEquals("http://download.com", entity.getDownloadUrl());
        assertEquals("http://cover.com", entity.getCoverUrl());
        assertEquals("pt", entity.getLanguages());
        assertEquals("Fiction", entity.getSubjects());
        assertEquals(5000, entity.getDownloadCount());
        assertNull(entity.getPublisherId());
        assertEquals("GUTENDEX", entity.getSource());
    }

    @Test
    void shouldUseNoArgsConstructor() {
        BookEntity entity = new BookEntity();
        entity.setId(5L);
        entity.setTitle("Test");
        entity.setAuthor("Author");
        entity.setSource("LOCAL");

        assertEquals(5L, entity.getId());
        assertEquals("Test", entity.getTitle());
        assertEquals("Author", entity.getAuthor());
        assertEquals("LOCAL", entity.getSource());
    }

    @Test
    void shouldCreateLocalEntity() {
        BookEntity entity = new BookEntity(2L, "Local Book", null, null, null, null, null, null, null, 3L, "LOCAL");
        assertEquals("LOCAL", entity.getSource());
        assertEquals(3L, entity.getPublisherId());
        assertNull(entity.getAuthor());
    }
}
