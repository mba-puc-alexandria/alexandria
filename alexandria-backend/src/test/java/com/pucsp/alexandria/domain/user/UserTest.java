package com.pucsp.alexandria.domain.user;

import static org.junit.jupiter.api.Assertions.*;

import com.pucsp.alexandria.domain.user.exception.InvalidUserException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

class UserTest {

    @Test
    void shouldCreateUser() {
        User user = User.create("john_doe", "John", "Doe", "john@example.com", "password123");
        assertNull(user.getId());
        assertEquals("john_doe", user.getUsername());
        assertEquals("John", user.getFirstName());
        assertEquals("Doe", user.getLastName());
                assertEquals("john@example.com", user.getEmail().getValue());
        assertEquals("password123", user.getPassword());
        assertNotNull(user.getCreatedAt());
        assertEquals(User.Role.USER, user.getRole());
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldThrowExceptionForInvalidUsername(String invalidUsername) {
        assertThrows(InvalidUserException.class,
                () -> User.create(invalidUsername, "John", "Doe", "john@example.com", "password123"));
    }

    @Test
    void shouldThrowExceptionForUsernameTooLong() {
        String longUsername = "a".repeat(256);
        assertThrows(InvalidUserException.class,
                () -> User.create(longUsername, "John", "Doe", "john@example.com", "password123"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldThrowExceptionForInvalidFirstName(String invalidFirstName) {
        assertThrows(InvalidUserException.class,
                () -> User.create("user", invalidFirstName, "Doe", "john@example.com", "password123"));
    }

    @Test
    void shouldThrowExceptionForFirstNameTooLong() {
        String longName = "a".repeat(256);
        assertThrows(InvalidUserException.class,
                () -> User.create("user", longName, "Doe", "john@example.com", "password123"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldThrowExceptionForInvalidLastName(String invalidLastName) {
        assertThrows(InvalidUserException.class,
                () -> User.create("user", "John", invalidLastName, "john@example.com", "password123"));
    }

    @Test
    void shouldThrowExceptionForLastNameTooLong() {
        String longName = "a".repeat(256);
        assertThrows(InvalidUserException.class,
                () -> User.create("user", "John", longName, "john@example.com", "password123"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldThrowExceptionForInvalidPassword(String invalidPassword) {
        assertThrows(InvalidUserException.class,
                () -> User.create("user", "John", "Doe", "john@example.com", invalidPassword));
    }

    @Test
    void shouldThrowExceptionForShortPassword() {
        assertThrows(InvalidUserException.class,
                () -> User.create("user", "John", "Doe", "john@example.com", "short1"));
    }

    @Test
    void shouldThrowExceptionForLongPassword() {
        String longPassword = "a".repeat(256);
        assertThrows(InvalidUserException.class,
                () -> User.create("user", "John", "Doe", "john@example.com", longPassword));
    }

    @Test
    void shouldThrowExceptionForInvalidEmail() {
        assertThrows(IllegalArgumentException.class,
                () -> User.create("user", "John", "Doe", "invalid-email", "password123"));
    }

        @Test
    void shouldRestoreUser() {
        User user = User.restore(1L, "john_doe", "John", "Doe",
                "john@example.com", "password123", java.time.LocalDateTime.now(), User.Role.USER);
        assertNotNull(user.getId());
        assertEquals(1L, user.getId().getValue());
        assertEquals("john_doe", user.getUsername());
    }

    @Test
    void shouldUpdateUser() {
        User user = User.create("john_doe", "John", "Doe", "john@example.com", "password123");
        User updated = user.updateWith("jane_doe", "Jane", "Smith", "newpassword123");
        assertEquals("jane_doe", updated.getUsername());
        assertEquals("Jane", updated.getFirstName());
        assertEquals("Smith", updated.getLastName());
        assertEquals("newpassword123", updated.getPassword());
        assertEquals("john@example.com", updated.getEmail().getValue());
    }

    @Test
    void shouldKeepOriginalValuesWhenUpdatingWithNull() {
        User user = User.create("john_doe", "John", "Doe", "john@example.com", "password123");
        User updated = user.updateWith(null, null, null, null);
        assertEquals("john_doe", updated.getUsername());
        assertEquals("John", updated.getFirstName());
        assertEquals("Doe", updated.getLastName());
        assertEquals("password123", updated.getPassword());
    }

    @Test
    void usersWithSameIdShouldBeEqual() {
                User user1 = User.restore(1L, "user1", "A", "B", "a@b.com", "pass1234", java.time.LocalDateTime.now(), User.Role.USER);
        User user2 = User.restore(1L, "user2", "C", "D", "c@d.com", "otherpass", java.time.LocalDateTime.now(), User.Role.USER);
        assertEquals(user1, user2);
        assertEquals(user1.hashCode(), user2.hashCode());
    }

    @Test
    void usersWithDifferentIdsShouldNotBeEqual() {
        User user1 = User.restore(1L, "user1", "A", "B", "a@b.com", "pass1234", java.time.LocalDateTime.now(), User.Role.USER);
        User user2 = User.restore(2L, "user1", "A", "B", "a@b.com", "pass1234", java.time.LocalDateTime.now(), User.Role.USER);
        assertNotEquals(user1, user2);
    }
}
