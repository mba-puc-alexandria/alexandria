package com.pucsp.alexandria.adapter.out.persistence.entity;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class UserEntityTest {

    @Test
    void shouldCreateEntityWithAllFields() {
        LocalDateTime now = LocalDateTime.now();
        UserEntity entity = new UserEntity(1L, "john_doe", "John", "Doe",
                "john@example.com", "password", now);

        assertEquals(1L, entity.getId());
        assertEquals("john_doe", entity.getUsername());
        assertEquals("John", entity.getFirstName());
        assertEquals("Doe", entity.getLastName());
        assertEquals("john@example.com", entity.getEmail());
        assertEquals("password", entity.getPassword());
        assertEquals(now, entity.getCreatedAt());
    }

    @Test
    void shouldUseNoArgsConstructor() {
        UserEntity entity = new UserEntity();
        entity.setUsername("test");
        entity.setFirstName("First");
        entity.setLastName("Last");
        entity.setEmail("test@test.com");
        entity.setPassword("pass");

        assertEquals("test", entity.getUsername());
        assertEquals("First", entity.getFirstName());
        assertEquals("test@test.com", entity.getEmail());
    }
}
