package com.pucsp.alexandria.adapter.out.persistence.entity;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class UserBooksEntityTest {

    @Test
    void shouldCreateEntityWithAllFields() {
        UserEntity user = new UserEntity(1L, "user", "First", "Last", "u@t.com", "pass", LocalDateTime.now());
        BookEntity book = new BookEntity(10L, "Title", "Author", null, null, null, null, null, null, null, "LOCAL");
        LocalDateTime now = LocalDateTime.now();
        UserBooksEntity entity = new UserBooksEntity(1L, user, book, "reading", 50, 4, now);

        assertEquals(1L, entity.getId());
        assertEquals(user, entity.getUser());
        assertEquals(book, entity.getBook());
        assertEquals("reading", entity.getStatus());
        assertEquals(50, entity.getProgress());
        assertEquals(4, entity.getRating());
        assertEquals(now, entity.getCreatedAt());
    }

    @Test
    void shouldUseNoArgsConstructor() {
        UserBooksEntity entity = new UserBooksEntity();
        entity.setStatus("toread");
        assertEquals("toread", entity.getStatus());
        assertNull(entity.getProgress());
        assertNull(entity.getRating());
    }
}
