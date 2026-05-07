package com.pucsp.alexandria.adapter.out.persistence.mapper;

import static org.junit.jupiter.api.Assertions.*;

import com.pucsp.alexandria.adapter.out.persistence.entity.BookEntity;
import com.pucsp.alexandria.adapter.out.persistence.entity.UserBooksEntity;
import com.pucsp.alexandria.adapter.out.persistence.entity.UserEntity;
import com.pucsp.alexandria.domain.userbook.UserBooks;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class UserBooksMapperTest {

    private final UserBooksMapper mapper = new UserBooksMapper();

    @Test
    void shouldMapEntityToDomain() {
        UserEntity userEntity = new UserEntity(1L, "user", "First", "Last", "user@test.com", "pass", LocalDateTime.now());
        BookEntity bookEntity = new BookEntity(10L, "Title", "Author", null, null, null, null, null, null, null, "LOCAL");
        LocalDateTime now = LocalDateTime.now();
        UserBooksEntity entity = new UserBooksEntity(1L, userEntity, bookEntity, "toread", null, null, now);

        UserBooks ub = mapper.toDomain(entity);

        assertNotNull(ub);
        assertEquals(1L, ub.getId());
        assertEquals(1L, ub.getUserId().getValue());
        assertEquals(10L, ub.getBookId().getValue());
        assertEquals("toread", ub.getStatus().getValue());
        assertEquals(now, ub.getCreatedAt());
    }

    @Test
    void shouldMapDomainToEntity() {
        UserEntity userEntity = new UserEntity(1L, "user", "First", "Last", "user@test.com", "pass", LocalDateTime.now());
        BookEntity bookEntity = new BookEntity(10L, "Title", "Author", null, null, null, null, null, null, null, "LOCAL");
        UserBooks ub = UserBooks.restore(1L, 1L, 10L, "reading", 50, null, LocalDateTime.now());

        UserBooksEntity entity = mapper.toPersistence(ub, userEntity, bookEntity);

        assertNotNull(entity);
        assertEquals(1L, entity.getId());
        assertEquals("reading", entity.getStatus());
        assertEquals(50, entity.getProgress());
        assertNull(entity.getRating());
    }

    @Test
    void shouldReturnNullWhenEntityIsNull() {
        assertNull(mapper.toDomain(null));
    }

    @Test
    void shouldReturnNullWhenDomainIsNull() {
        assertNull(mapper.toPersistence(null, null, null));
    }
}
