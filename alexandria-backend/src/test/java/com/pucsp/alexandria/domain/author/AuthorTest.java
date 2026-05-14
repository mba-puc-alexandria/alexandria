package com.pucsp.alexandria.domain.author;

import static org.junit.jupiter.api.Assertions.*;

import com.pucsp.alexandria.domain.author.exception.InvalidAuthorException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

class AuthorTest {

    @Test
    void shouldCreateAuthor() {
        Author author = Author.create("Machado de Assis", 1839, 1908);
        assertNull(author.getId());
        assertEquals("Machado de Assis", author.getName());
        assertEquals(1839, author.getBirthYear());
        assertEquals(1908, author.getDeathYear());
    }

    @Test
    void shouldCreateAuthorWithoutBirthDeath() {
        Author author = Author.create("Desconhecido", null, null);
        assertEquals("Desconhecido", author.getName());
        assertNull(author.getBirthYear());
        assertNull(author.getDeathYear());
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldThrowExceptionWhenCreatingWithInvalidName(String invalidName) {
        InvalidAuthorException ex = assertThrows(InvalidAuthorException.class,
                () -> Author.create(invalidName, null, null));
        assertEquals("Author name is required", ex.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenCreatingWithNameTooLong() {
        String longName = "a".repeat(256);
        InvalidAuthorException ex = assertThrows(InvalidAuthorException.class,
                () -> Author.create(longName, null, null));
        assertEquals("Author name must not exceed 255 characters", ex.getMessage());
    }

    @Test
    void shouldRestoreAuthor() {
        Author author = Author.restore(1L, "Machado de Assis", 1839, 1908);
        assertNotNull(author.getId());
        assertEquals(1L, author.getId().getValue());
        assertEquals("Machado de Assis", author.getName());
        assertEquals(1839, author.getBirthYear());
        assertEquals(1908, author.getDeathYear());
    }

    @Test
    void shouldUpdateName() {
        Author author = Author.create("Old Name", null, null);
        Author updated = author.updateName("New Name");
        assertEquals("New Name", updated.getName());
        assertEquals("Old Name", author.getName());
    }

    @Test
    void shouldThrowExceptionWhenUpdatingWithInvalidName() {
        Author author = Author.create("Valid Name", null, null);
        assertThrows(InvalidAuthorException.class, () -> author.updateName(""));
    }

    @Test
    void authorsWithSameIdShouldBeEqual() {
        Author author1 = Author.restore(1L, "Machado de Assis", 1839, 1908);
        Author author2 = Author.restore(1L, "Other Name", 1900, 2000);
        assertEquals(author1, author2);
        assertEquals(author1.hashCode(), author2.hashCode());
    }

    @Test
    void authorsWithDifferentIdsShouldNotBeEqual() {
        Author author1 = Author.restore(1L, "Name", null, null);
        Author author2 = Author.restore(2L, "Name", null, null);
        assertNotEquals(author1, author2);
    }
}
