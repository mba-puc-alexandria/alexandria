package com.pucsp.alexandria.adapter.out.persistence.mapper;

import static org.junit.jupiter.api.Assertions.*;

import com.pucsp.alexandria.adapter.out.persistence.entity.AuthorEntity;
import com.pucsp.alexandria.adapter.out.persistence.entity.BookEntity;
import com.pucsp.alexandria.adapter.out.persistence.entity.UserBooksEntity;
import com.pucsp.alexandria.adapter.out.persistence.entity.UserEntity;
import com.pucsp.alexandria.domain.userbook.UserBooks;
import java.time.LocalDateTime;
import java.util.Set;
import org.junit.jupiter.api.Test;

class UserBooksMapperTest {

    private final UserBooksMapper mapper = new UserBooksMapper();

    @Test
    void shouldMapEntityToDomain() {
        AuthorEntity author = new AuthorEntity(1L, "Author", null, null);
        BookEntity book = new BookEntity(1L, "Title", Set.of(author), null, null, null, null, null, null, null, "GUTENDEX");
        UserEntity user = new UserEntity(1L, "user", "First", "Last", "email", "pass", LocalDateTime.now());
        UserBooksEntity entity = new UserBooksEntity(1L, user, book, "done", null, 4, LocalDateTime.now());

        UserBooks userBooks = mapper.toDomain(entity);

        assertNotNull(userBooks);
        assertEquals(1L, userBooks.getId());
        assertEquals("done", userBooks.getStatus().getValue());
        assertNull(userBooks.getProgress());
        assertEquals(4, userBooks.getRating());
    }

    @Test
    void shouldReturnNullWhenEntityIsNull() {
        assertNull(mapper.toDomain(null));
    }
}
