package com.pucsp.alexandria.domain.userbook;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class UserBooksStatusTest {

    @Test
    void toreadShouldHaveCorrectValue() {
        assertEquals("toread", UserBooksStatus.TOREAD.getValue());
    }

    @Test
    void readingShouldHaveCorrectValue() {
        assertEquals("reading", UserBooksStatus.READING.getValue());
    }

    @Test
    void doneShouldHaveCorrectValue() {
        assertEquals("done", UserBooksStatus.DONE.getValue());
    }

    @Test
    void shouldParseValidString() {
        assertEquals(UserBooksStatus.TOREAD, UserBooksStatus.fromString("toread"));
        assertEquals(UserBooksStatus.READING, UserBooksStatus.fromString("reading"));
        assertEquals(UserBooksStatus.DONE, UserBooksStatus.fromString("done"));
    }

    @Test
    void shouldParseCaseInsensitive() {
        assertEquals(UserBooksStatus.TOREAD, UserBooksStatus.fromString("TOREAD"));
        assertEquals(UserBooksStatus.READING, UserBooksStatus.fromString("READING"));
    }

    @Test
    void shouldThrowExceptionForInvalidString() {
        assertThrows(IllegalArgumentException.class,
                () -> UserBooksStatus.fromString("invalid"));
    }
}
