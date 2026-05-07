package com.pucsp.alexandria.config.jwt;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(
            "0123456789012345678901234567890123456789012345678901234567890123",
            86400000L
        );
    }

    @Test
    void shouldGenerateToken() {
        String token = jwtTokenProvider.generateToken(1L, "john_doe");
        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void shouldExtractUserIdFromToken() {
        String token = jwtTokenProvider.generateToken(42L, "test_user");
        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        assertEquals(42L, userId);
    }

    @Test
    void shouldExtractUsernameFromToken() {
        String token = jwtTokenProvider.generateToken(1L, "john_doe");
        String username = jwtTokenProvider.getUsernameFromToken(token);
        assertEquals("john_doe", username);
    }

    @Test
    void shouldValidateValidToken() {
        String token = jwtTokenProvider.generateToken(1L, "john_doe");
        assertTrue(jwtTokenProvider.validateToken(token));
    }

    @Test
    void shouldNotValidateInvalidToken() {
        assertFalse(jwtTokenProvider.validateToken("invalid.token.here"));
    }

    @Test
    void shouldNotValidateEmptyToken() {
        assertFalse(jwtTokenProvider.validateToken(""));
    }

    @Test
    void shouldNotValidateNullToken() {
        assertFalse(jwtTokenProvider.validateToken(null));
    }

    @Test
    void differentUsersShouldHaveDifferentTokens() {
        String token1 = jwtTokenProvider.generateToken(1L, "user1");
        String token2 = jwtTokenProvider.generateToken(2L, "user2");
        assertNotEquals(token1, token2);
    }
}
