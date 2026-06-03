package com.pucsp.alexandria.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.*;

import com.pucsp.alexandria.adapter.out.persistence.mapper.UserMapper;
import com.pucsp.alexandria.domain.user.User;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import({UserRepositoryImpl.class, UserMapper.class})
class UserRepositoryImplTest {

    @Autowired
    private UserRepositoryImpl userRepository;

    @Test
    void shouldSaveAndFindUser() {
        User user = User.create("john_doe", "John", "Doe", "john@example.com", "password123");
        User saved = userRepository.save(user);

        assertNotNull(saved.getId());
        assertEquals("john_doe", saved.getUsername());
        assertEquals("john@example.com", saved.getEmail().getValue());
    }

    @Test
    void shouldFindById() {
        User saved = userRepository.save(User.create("user1", "A", "B", "a@b.com", "pass1234"));

        Optional<User> found = userRepository.findById(saved.getId().getValue());

        assertTrue(found.isPresent());
        assertEquals("user1", found.get().getUsername());
    }

    @Test
    void shouldFindByUsername() {
        userRepository.save(User.create("unique_user", "U", "User", "u@test.com", "pass1234"));

        Optional<User> found = userRepository.findByUsername("unique_user");

        assertTrue(found.isPresent());
        assertEquals("u@test.com", found.get().getEmail().getValue());
    }

    @Test
    void shouldCheckExistenceByUsername() {
        userRepository.save(User.create("existing", "Ex", "User", "ex@test.com", "pass1234"));

        assertTrue(userRepository.existsByUsername("existing"));
        assertFalse(userRepository.existsByUsername("not_existing"));
    }

    @Test
    void shouldCheckExistenceByEmail() {
        userRepository.save(User.create("user_a", "A", "B", "email@test.com", "pass1234"));

        assertTrue(userRepository.existsByEmail("email@test.com"));
        assertFalse(userRepository.existsByEmail("other@test.com"));
    }

    @Test
    void shouldDeleteUser() {
        User saved = userRepository.save(User.create("delete_me", "Del", "Eter", "del@test.com", "pass1234"));

        userRepository.delete(saved);

        assertFalse(userRepository.findById(saved.getId().getValue()).isPresent());
    }
}

