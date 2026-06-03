package com.pucsp.alexandria.domain.shared.valueobject;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class EmailTest {

    @Test
    void shouldCreateValidEmail() {
        Email email = new Email("user@example.com");
        assertEquals("user@example.com", email.getValue());
    }

    @Test
    void shouldConvertToLowerCase() {
        Email email = new Email("User@Example.com");
        assertEquals("user@example.com", email.getValue());
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldThrowExceptionForNullOrBlank(String invalidEmail) {
        assertThrows(IllegalArgumentException.class, () -> new Email(invalidEmail));
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalid", "no-at", "@empty", "user@"})
    void shouldThrowExceptionForInvalidFormat(String invalidEmail) {
        assertThrows(IllegalArgumentException.class, () -> new Email(invalidEmail));
    }

    @Test
    void sameEmailsShouldBeEqual() {
        Email email1 = new Email("test@example.com");
        Email email2 = new Email("test@example.com");
        assertEquals(email1, email2);
        assertEquals(email1.hashCode(), email2.hashCode());
    }

    @Test
    void differentEmailsShouldNotBeEqual() {
        Email email1 = new Email("test@example.com");
        Email email2 = new Email("other@example.com");
        assertNotEquals(email1, email2);
    }

    @Test
    void toStringShouldReturnValue() {
        Email email = new Email("user@test.com");
        assertEquals("user@test.com", email.toString());
    }
}
