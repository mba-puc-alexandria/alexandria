package com.pucsp.alexandria.domain.author;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class AuthorIdTest {

    @Test
    void shouldCreateAuthorIdFromValidId() {
        AuthorId authorId = AuthorId.from(1L);
        assertNotNull(authorId);
        assertEquals(1L, authorId.getValue());
    }

    @Test
    void shouldThrowExceptionWhenIdIsNull() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> AuthorId.from(null));
        assertEquals("AuthorId must be positive", ex.getMessage());
    }

    @ParameterizedTest
    @ValueSource(longs = {0L, -1L, -100L})
    void shouldThrowExceptionWhenIdIsNotPositive(Long invalidId) {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> AuthorId.from(invalidId));
        assertEquals("AuthorId must be positive", ex.getMessage());
    }

    @Test
    void twoAuthorIdsWithSameValueShouldBeEqual() {
        AuthorId id1 = AuthorId.from(5L);
        AuthorId id2 = AuthorId.from(5L);
        assertEquals(id1, id2);
        assertEquals(id1.hashCode(), id2.hashCode());
    }

    @Test
    void twoAuthorIdsWithDifferentValuesShouldNotBeEqual() {
        AuthorId id1 = AuthorId.from(1L);
        AuthorId id2 = AuthorId.from(2L);
        assertNotEquals(id1, id2);
    }

    @Test
    void toStringShouldReturnValue() {
        AuthorId authorId = AuthorId.from(42L);
        assertEquals("42", authorId.toString());
    }
}
