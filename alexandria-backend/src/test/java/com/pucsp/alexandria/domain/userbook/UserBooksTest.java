package com.pucsp.alexandria.domain.userbook;

import static org.junit.jupiter.api.Assertions.*;

import com.pucsp.alexandria.domain.book.BookId;
import com.pucsp.alexandria.domain.user.UserId;
import com.pucsp.alexandria.domain.userbook.exception.InvalidUserBooksException;
import org.junit.jupiter.api.Test;

class UserBooksTest {

    private final UserId userId = UserId.from(1L);
    private final BookId bookId = BookId.from(10L);

    @Test
    void shouldCreateWithDefaultStatusToread() {
        UserBooks ub = UserBooks.create(userId, bookId, null);
        assertNull(ub.getId());
        assertEquals(userId, ub.getUserId());
        assertEquals(bookId, ub.getBookId());
        assertEquals(UserBooksStatus.TOREAD, ub.getStatus());
        assertNull(ub.getProgress());
        assertNull(ub.getRating());
        assertNotNull(ub.getCreatedAt());
    }

    @Test
    void shouldCreateWithSpecifiedStatus() {
        UserBooks ub = UserBooks.create(userId, bookId, UserBooksStatus.TOREAD);
        assertEquals(UserBooksStatus.TOREAD, ub.getStatus());
    }

    @Test
    void shouldThrowExceptionWhenUserIdIsNull() {
        assertThrows(NullPointerException.class,
                () -> UserBooks.create(null, bookId, UserBooksStatus.TOREAD));
    }

    @Test
    void shouldThrowExceptionWhenBookIdIsNull() {
        assertThrows(NullPointerException.class,
                () -> UserBooks.create(userId, null, UserBooksStatus.TOREAD));
    }

    @Test
    void shouldThrowExceptionWhenCreatingReadingWithoutProgress() {
        InvalidUserBooksException ex = assertThrows(InvalidUserBooksException.class,
                () -> UserBooks.create(userId, bookId, UserBooksStatus.READING));
        assertEquals("Progress (0-100) is required for READING status", ex.getMessage());
    }

    @Test
    void shouldRestoreUserBooks() {
        UserBooks ub = UserBooks.restore(1L, 1L, 10L, "reading", 50, null,
                java.time.LocalDateTime.now());
        assertEquals(1L, ub.getId());
        assertEquals(userId, ub.getUserId());
        assertEquals(bookId, ub.getBookId());
        assertEquals(UserBooksStatus.READING, ub.getStatus());
        assertEquals(50, ub.getProgress());
        assertNull(ub.getRating());
    }

    @Test
    void shouldRestoreDoneUserBooks() {
        UserBooks ub = UserBooks.restore(1L, 1L, 10L, "done", null, 4,
                java.time.LocalDateTime.now());
        assertEquals(UserBooksStatus.DONE, ub.getStatus());
        assertNull(ub.getProgress());
        assertEquals(4, ub.getRating());
    }

    @Test
    void shouldThrowExceptionWhenRestoringToreadWithProgress() {
        assertThrows(InvalidUserBooksException.class,
                () -> UserBooks.restore(1L, 1L, 10L, "toread", 50, null,
                        java.time.LocalDateTime.now()));
    }

    @Test
    void shouldThrowExceptionForReadingWithInvalidProgress() {
        assertThrows(InvalidUserBooksException.class,
                () -> UserBooks.restore(1L, 1L, 10L, "reading", -1, null,
                        java.time.LocalDateTime.now()));
    }

    @Test
    void shouldThrowExceptionForReadingWithProgressAbove100() {
        assertThrows(InvalidUserBooksException.class,
                () -> UserBooks.restore(1L, 1L, 10L, "reading", 101, null,
                        java.time.LocalDateTime.now()));
    }

    @Test
    void shouldThrowExceptionForDoneWithNullRating() {
        assertThrows(InvalidUserBooksException.class,
                () -> UserBooks.restore(1L, 1L, 10L, "done", null, null,
                        java.time.LocalDateTime.now()));
    }

    @Test
    void shouldThrowExceptionForDoneWithInvalidRating() {
        assertThrows(InvalidUserBooksException.class,
                () -> UserBooks.restore(1L, 1L, 10L, "done", null, -1,
                        java.time.LocalDateTime.now()));
    }

    @Test
    void shouldThrowExceptionForDoneWithRatingAbove5() {
        assertThrows(InvalidUserBooksException.class,
                () -> UserBooks.restore(1L, 1L, 10L, "done", null, 6,
                        java.time.LocalDateTime.now()));
    }

    @Test
    void shouldUpdateUserBooks() {
        UserBooks ub = UserBooks.create(userId, bookId, UserBooksStatus.TOREAD);
        UserBooks updated = ub.updateWith(UserBooksStatus.DONE, null, 5);
        assertEquals(UserBooksStatus.DONE, updated.getStatus());
        assertNull(updated.getProgress());
        assertEquals(5, updated.getRating());
    }

    @Test
    void shouldKeepOriginalValuesWhenUpdatingWithNull() {
        UserBooks ub = UserBooks.restore(1L, 1L, 10L, "reading", 50, null,
                java.time.LocalDateTime.now());
        UserBooks updated = ub.updateWith(null, null, null);
        assertEquals(UserBooksStatus.READING, updated.getStatus());
        assertEquals(50, updated.getProgress());
        assertNull(updated.getRating());
    }

    @Test
    void twoUserBooksWithSameIdShouldBeEqual() {
        UserBooks ub1 = UserBooks.restore(1L, 1L, 10L, "toread", null, null,
                java.time.LocalDateTime.now());
        UserBooks ub2 = UserBooks.restore(1L, 2L, 20L, "done", null, 4,
                java.time.LocalDateTime.now());
        assertEquals(ub1, ub2);
        assertEquals(ub1.hashCode(), ub2.hashCode());
    }

    @Test
    void userBooksWithDifferentIdsShouldNotBeEqual() {
        UserBooks ub1 = UserBooks.restore(1L, 1L, 10L, "toread", null, null,
                java.time.LocalDateTime.now());
        UserBooks ub2 = UserBooks.restore(2L, 1L, 10L, "toread", null, null,
                java.time.LocalDateTime.now());
        assertNotEquals(ub1, ub2);
    }
}
