package com.pucsp.alexandria.adapter.out.persistence.jpa;

import static org.junit.jupiter.api.Assertions.*;

import com.pucsp.alexandria.adapter.out.persistence.entity.UserEntity;
import com.pucsp.alexandria.domain.user.User.Role;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class UserJpaRepositoryTest {

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Test
    void shouldSaveAndFindUserById() {
                UserEntity entity = new UserEntity(null, "john_doe", "John", "Doe",
                "john@example.com", "password", java.time.LocalDateTime.now(), Role.USER);
        UserEntity saved = userJpaRepository.save(entity);

        Optional<UserEntity> found = userJpaRepository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals("john_doe", found.get().getUsername());
        assertEquals("John", found.get().getFirstName());
    }

    @Test
    void shouldFindByUsername() {
        userJpaRepository.save(new UserEntity(null, "jane_doe", "Jane", "Doe",
                "jane@example.com", "pass", java.time.LocalDateTime.now(), Role.USER));

        Optional<UserEntity> found = userJpaRepository.findByUsername("jane_doe");

        assertTrue(found.isPresent());
        assertEquals("jane@example.com", found.get().getEmail());
    }

    @Test
    void shouldCheckExistenceByUsername() {
        userJpaRepository.save(new UserEntity(null, "existing", "Ex", "User",
                "ex@test.com", "pass", java.time.LocalDateTime.now(), Role.USER));

        assertTrue(userJpaRepository.existsByUsername("existing"));
        assertFalse(userJpaRepository.existsByUsername("nonexistent"));
    }

    @Test
    void shouldCheckExistenceByEmail() {
        userJpaRepository.save(new UserEntity(null, "user1", "A", "B",
                "unique@test.com", "pass", java.time.LocalDateTime.now(), Role.USER));

        assertTrue(userJpaRepository.existsByEmail("unique@test.com"));
        assertFalse(userJpaRepository.existsByEmail("other@test.com"));
    }

    @Test
    void shouldDeleteUserById() {
        UserEntity saved = userJpaRepository.save(
                new UserEntity(null, "delete_me", "Del", "Eter",
                        "del@test.com", "pass", java.time.LocalDateTime.now(), Role.USER));

        userJpaRepository.deleteById(saved.getId());

        assertFalse(userJpaRepository.findById(saved.getId()).isPresent());
    }
}

