package com.pucsp.alexandria.domain.book;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class BookIdTest {

    @Test
    void shouldCreateBookIdFromValidId() {
        BookId bookId = BookId.from(1L);
        assertNotNull(bookId);
        assertEquals(1L, bookId.getValue());
    }

    @Test
    void shouldThrowExceptionWhenIdIsNull() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> BookId.from(null));
        assertEquals("BookId cannot be null", ex.getMessage());
    }

    @ParameterizedTest
    @ValueSource(longs = {0L, -1L, -100L})
    void shouldThrowExceptionWhenIdIsNotPositive(Long invalidId) {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> BookId.from(invalidId));
        assertEquals("BookId must be positive", ex.getMessage());
    }

    @Test
    void twoBookIdsWithSameValueShouldBeEqual() {
        BookId id1 = BookId.from(5L);
        BookId id2 = BookId.from(5L);
        assertEquals(id1, id2);
        assertEquals(id1.hashCode(), id2.hashCode());
    }

    @Test
    void twoBookIdsWithDifferentValuesShouldNotBeEqual() {
        BookId id1 = BookId.from(1L);
        BookId id2 = BookId.from(2L);
        assertNotEquals(id1, id2);
    }

    @Test
    void toStringShouldReturnValue() {
        BookId bookId = BookId.from(42L);
        assertEquals("42", bookId.toString());
    }
}
