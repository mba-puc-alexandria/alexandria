package com.pucsp.alexandria.adapter.out.persistence.entity;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.Set;
import org.junit.jupiter.api.Test;

class UserBooksEntityTest {

    @Test
    void shouldCreateUserBooksEntity() {
        AuthorEntity author = new AuthorEntity(1L, "Author", null, null);
        BookEntity book = new BookEntity(1L, "Title", Set.of(author), null, null, null, null, null, null, null, "GUTENDEX");
        UserEntity user = new UserEntity(1L, "user", "First", "Last", "email@test.com", "password", LocalDateTime.now(), com.pucsp.alexandria.domain.user.User.Role.USER);
        UserBooksEntity entity = new UserBooksEntity(1L, user, book, "reading", 50, 4, LocalDateTime.now());

        assertEquals(1L, entity.getId());
        assertEquals("reading", entity.getStatus());
        assertEquals(50, entity.getProgress());
        assertEquals(4, entity.getRating());
        assertNotNull(entity.getUser());
        assertNotNull(entity.getBook());
    }

    @Test
    void shouldUseDefaultStatus() {
        UserBooksEntity entity = new UserBooksEntity();
        assertEquals("toread", entity.getStatus());
    }
}
