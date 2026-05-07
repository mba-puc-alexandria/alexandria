package com.pucsp.alexandria.adapter.out.persistence.mapper;

import static org.junit.jupiter.api.Assertions.*;

import com.pucsp.alexandria.adapter.out.persistence.entity.UserEntity;
import com.pucsp.alexandria.domain.user.User;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class UserMapperTest {

    private final UserMapper mapper = new UserMapper();

    @Test
    void shouldMapEntityToDomain() {
        LocalDateTime now = LocalDateTime.now();
        UserEntity entity = new UserEntity(1L, "john_doe", "John", "Doe",
                "john@example.com", "encodedPass", now);

        User user = mapper.toDomain(entity);

        assertNotNull(user);
        assertEquals(1L, user.getId().getValue());
        assertEquals("john_doe", user.getUsername());
        assertEquals("John", user.getFirstName());
        assertEquals("Doe", user.getLastName());
        assertEquals("john@example.com", user.getEmail().getValue());
        assertEquals("encodedPass", user.getPassword());
        assertEquals(now, user.getCreatedAt());
    }

    @Test
    void shouldMapDomainToEntity() {
        User user = User.restore(1L, "jane_doe", "Jane", "Doe",
                "jane@example.com", "pass1234", LocalDateTime.now());

        UserEntity entity = mapper.toPersistence(user);

        assertNotNull(entity);
        assertEquals(1L, entity.getId());
        assertEquals("jane_doe", entity.getUsername());
        assertEquals("Jane", entity.getFirstName());
        assertEquals("jane@example.com", entity.getEmail());
    }

    @Test
    void shouldReturnNullWhenEntityIsNull() {
        assertNull(mapper.toDomain(null));
    }

    @Test
    void shouldReturnNullWhenDomainIsNull() {
        assertNull(mapper.toPersistence(null));
    }
}
