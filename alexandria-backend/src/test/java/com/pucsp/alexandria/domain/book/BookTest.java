package com.pucsp.alexandria.domain.book;

import static org.junit.jupiter.api.Assertions.*;

import com.pucsp.alexandria.domain.author.AuthorId;
import com.pucsp.alexandria.domain.book.exception.InvalidBookException;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

class BookTest {

    private final Set<AuthorId> authorIds = Set.of(AuthorId.from(1L));

    @Test
    void shouldCreateLocalBook() {
        Book book = Book.createLocal("Clean Code", authorIds, 1L);
        assertNull(book.getId());
        assertEquals("Clean Code", book.getTitle());
        assertEquals(authorIds, book.getAuthorIds());
        assertEquals(BookSource.LOCAL, book.getSource());
        assertEquals(1L, book.getPublisherId());
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldThrowExceptionWhenCreatingLocalBookWithInvalidTitle(String invalidTitle) {
        InvalidBookException ex = assertThrows(InvalidBookException.class,
                () -> Book.createLocal(invalidTitle, authorIds, 1L));
        assertEquals("Book title is required", ex.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenCreatingLocalBookWithTitleTooLong() {
        String longTitle = "a".repeat(501);
        InvalidBookException ex = assertThrows(InvalidBookException.class,
                () -> Book.createLocal(longTitle, authorIds, 1L));
        assertEquals("Book title must not exceed 500 characters", ex.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenCreatingLocalBookWithEmptyAuthorIds() {
        InvalidBookException ex = assertThrows(InvalidBookException.class,
                () -> Book.createLocal("Title", Set.of(), 1L));
        assertEquals("Book must have at least one author", ex.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenCreatingLocalBookWithNullAuthorIds() {
        InvalidBookException ex = assertThrows(InvalidBookException.class,
                () -> Book.createLocal("Title", null, 1L));
        assertEquals("Book must have at least one author", ex.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenCreatingLocalBookWithNullPublisherId() {
        InvalidBookException ex = assertThrows(InvalidBookException.class,
                () -> Book.createLocal("Title", authorIds, null));
        assertEquals("Valid publisher ID is required for LOCAL books", ex.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenCreatingLocalBookWithInvalidPublisherId() {
        InvalidBookException ex = assertThrows(InvalidBookException.class,
                () -> Book.createLocal("Title", authorIds, 0L));
        assertEquals("Valid publisher ID is required for LOCAL books", ex.getMessage());
    }

    @Test
    void shouldCreateBookFromGutendex() {
        Book book = Book.createFromGutendex(
                100L, "Dom Casmurro", authorIds,
                "http://download.com", "http://cover.com",
                "pt", "Fiction", 5000
        );
        assertNull(book.getId());
        assertEquals("Dom Casmurro", book.getTitle());
        assertEquals(authorIds, book.getAuthorIds());
        assertEquals(100L, book.getGutendexId());
        assertEquals("http://download.com", book.getDownloadUrl());
        assertEquals("http://cover.com", book.getCoverUrl());
        assertEquals("pt", book.getLanguages());
        assertEquals("Fiction", book.getSubjects());
        assertEquals(5000, book.getDownloadCount());
        assertEquals(BookSource.GUTENDEX, book.getSource());
        assertNull(book.getPublisherId());
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldThrowExceptionWhenCreatingGutendexBookWithInvalidTitle(String invalidTitle) {
        InvalidBookException ex = assertThrows(InvalidBookException.class,
                () -> Book.createFromGutendex(100L, invalidTitle, authorIds,
                        "url", "url", "pt", "Fiction", 100));
        assertEquals("Book title is required", ex.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenCreatingGutendexBookWithEmptyAuthorIds() {
        InvalidBookException ex = assertThrows(InvalidBookException.class,
                () -> Book.createFromGutendex(100L, "Title", Set.of(),
                        "url", "url", "pt", "Fiction", 100));
        assertEquals("Book must have at least one author", ex.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenCreatingGutendexBookWithNullGutendexId() {
        InvalidBookException ex = assertThrows(InvalidBookException.class,
                () -> Book.createFromGutendex(null, "Title", authorIds,
                        "url", "url", "pt", "Fiction", 100));
        assertEquals("Valid Gutendex ID is required", ex.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenCreatingGutendexBookWithInvalidGutendexId() {
        InvalidBookException ex = assertThrows(InvalidBookException.class,
                () -> Book.createFromGutendex(0L, "Title", authorIds,
                        "url", "url", "pt", "Fiction", 100));
        assertEquals("Valid Gutendex ID is required", ex.getMessage());
    }

    @Test
    void shouldRestoreBook() {
        Set<Long> ids = Set.of(1L);
        Book book = Book.restore(1L, "Title", ids, 100L,
                "url", "url", "pt", "Fiction", 100, null, BookSource.GUTENDEX);
        assertNotNull(book.getId());
        assertEquals(1L, book.getId().getValue());
        assertEquals("Title", book.getTitle());
        assertEquals(BookSource.GUTENDEX, book.getSource());
        assertEquals(1, book.getAuthorIds().size());
    }

    @Test
    void shouldRestoreLocalBookWithPublisher() {
        Set<Long> ids = Set.of(1L);
        Book book = Book.restore(1L, "Title", ids, null,
                null, null, null, null, null, 5L, BookSource.LOCAL);
        assertEquals(1L, book.getId().getValue());
        assertEquals(5L, book.getPublisherId());
        assertEquals(BookSource.LOCAL, book.getSource());
    }

    @Test
    void shouldThrowExceptionWhenRestoringLocalBookWithoutPublisher() {
        Set<Long> ids = Set.of(1L);
        assertThrows(InvalidBookException.class,
                () -> Book.restore(1L, "Title", ids, null,
                        null, null, null, null, null, null, BookSource.LOCAL));
    }

    @Test
    void shouldThrowExceptionWhenRestoringWithEmptyAuthorIds() {
        assertThrows(InvalidBookException.class,
                () -> Book.restore(1L, "Title", Set.of(), null,
                        null, null, null, null, null, 1L, BookSource.LOCAL));
    }

    @Test
    void shouldUpdateBookTitle() {
        Book book = Book.createLocal("Original Title", authorIds, 1L);
        Book updated = book.updateWith("New Title");
        assertEquals("New Title", updated.getTitle());
        assertEquals(authorIds, updated.getAuthorIds());
    }

    @Test
    void shouldKeepOriginalTitleWhenUpdatingWithNull() {
        Book book = Book.createLocal("Original Title", authorIds, 1L);
        Book updated = book.updateWith(null);
        assertEquals("Original Title", updated.getTitle());
    }

    @Test
    void shouldThrowExceptionWhenUpdatingWithInvalidTitle() {
        Book book = Book.createLocal("Original Title", authorIds, 1L);
        assertThrows(InvalidBookException.class, () -> book.updateWith(""));
    }

    @Test
    void booksWithSameIdShouldBeEqual() {
        Set<Long> ids = Set.of(1L);
        Book book1 = Book.restore(1L, "Title", ids, null, null, null, null, null, null, 1L, BookSource.LOCAL);
        Book book2 = Book.restore(1L, "Other", ids, null, null, null, null, null, null, 1L, BookSource.LOCAL);
        assertEquals(book1, book2);
        assertEquals(book1.hashCode(), book2.hashCode());
    }

    @Test
    void booksWithDifferentIdsShouldNotBeEqual() {
        Set<Long> ids = Set.of(1L);
        Book book1 = Book.restore(1L, "Title", ids, null, null, null, null, null, null, 1L, BookSource.LOCAL);
        Book book2 = Book.restore(2L, "Title", ids, null, null, null, null, null, null, 1L, BookSource.LOCAL);
        assertNotEquals(book1, book2);
    }
}
