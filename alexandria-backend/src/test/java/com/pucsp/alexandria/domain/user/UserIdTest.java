package com.pucsp.alexandria.domain.user;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class UserIdTest {

    @Test
    void shouldCreateUserIdFromValidId() {
        UserId userId = UserId.from(1L);
        assertNotNull(userId);
        assertEquals(1L, userId.getValue());
    }

    @ParameterizedTest
    @ValueSource(longs = {0L, -1L})
    void shouldThrowExceptionForNonPositiveId(Long invalidId) {
        assertThrows(IllegalArgumentException.class, () -> UserId.from(invalidId));
    }

    @Test
    void shouldThrowExceptionForNullId() {
        assertThrows(IllegalArgumentException.class, () -> UserId.from(null));
    }

    @Test
    void twoUserIdsWithSameValueShouldBeEqual() {
        UserId id1 = UserId.from(5L);
        UserId id2 = UserId.from(5L);
        assertEquals(id1, id2);
        assertEquals(id1.hashCode(), id2.hashCode());
    }
}
