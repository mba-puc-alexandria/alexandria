package com.pucsp.alexandria.adapter.out.persistence.jpa;

import static org.junit.jupiter.api.Assertions.*;

import com.pucsp.alexandria.adapter.out.persistence.entity.BookEntity;
import com.pucsp.alexandria.adapter.out.persistence.entity.UserBooksEntity;
import com.pucsp.alexandria.adapter.out.persistence.entity.UserEntity;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@DataJpaTest
class UserBooksJpaRepositoryTest {

    @Autowired
    private UserBooksJpaRepository userBooksJpaRepository;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private BookJpaRepository bookJpaRepository;

    private UserEntity user;
    private BookEntity book;

    @BeforeEach
    void setUp() {
        user = userJpaRepository.save(
                new UserEntity(null, "test_user", "Test", "User",
                        "test@test.com", "pass", LocalDateTime.now()));
        book = bookJpaRepository.save(
                new BookEntity(null, "Test Book", "Author", null, null, null, null, null, null, 1L, "LOCAL"));
    }

    @Test
    void shouldSaveAndFindUserBookById() {
        UserBooksEntity ub = new UserBooksEntity(null, user, book, "toread", null, null, LocalDateTime.now());
        UserBooksEntity saved = userBooksJpaRepository.save(ub);

        Optional<UserBooksEntity> found = userBooksJpaRepository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals("toread", found.get().getStatus());
        assertEquals(user.getId(), found.get().getUser().getId());
        assertEquals(book.getId(), found.get().getBook().getId());
    }

    @Test
    void shouldFindByUserId() {
        userBooksJpaRepository.save(new UserBooksEntity(null, user, book, "toread", null, null, LocalDateTime.now()));

        Page<UserBooksEntity> result = userBooksJpaRepository.findByUserId(user.getId(), PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void shouldFindByUserIdAndStatus() {
        userBooksJpaRepository.save(new UserBooksEntity(null, user, book, "reading", 50, null, LocalDateTime.now()));

        Page<UserBooksEntity> result = userBooksJpaRepository.findByUserIdAndStatus(
                user.getId(), "reading", PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        assertEquals(50, result.getContent().get(0).getProgress());
    }

    @Test
    void shouldFindByUserIdAndBookId() {
        userBooksJpaRepository.save(new UserBooksEntity(null, user, book, "done", null, 5, LocalDateTime.now()));

        Optional<UserBooksEntity> found = userBooksJpaRepository.findByUserIdAndBookId(user.getId(), book.getId());

        assertTrue(found.isPresent());
        assertEquals("done", found.get().getStatus());
        assertEquals(5, found.get().getRating());
    }

    @Test
    void shouldCheckExistenceByUserIdAndBookId() {
        userBooksJpaRepository.save(new UserBooksEntity(null, user, book, "toread", null, null, LocalDateTime.now()));

        assertTrue(userBooksJpaRepository.existsByUserIdAndBookId(user.getId(), book.getId()));
        assertFalse(userBooksJpaRepository.existsByUserIdAndBookId(999L, 999L));
    }

    @Test
    void shouldDeleteUserBook() {
        UserBooksEntity saved = userBooksJpaRepository.save(
                new UserBooksEntity(null, user, book, "toread", null, null, LocalDateTime.now()));

        userBooksJpaRepository.deleteById(saved.getId());

        assertFalse(userBooksJpaRepository.findById(saved.getId()).isPresent());
    }

    @Test
    void shouldEnforceUniqueConstraintOnUserIdAndBookId() {
        userBooksJpaRepository.save(new UserBooksEntity(null, user, book, "toread", null, null, LocalDateTime.now()));

        assertThrows(Exception.class, () ->
                userBooksJpaRepository.saveAndFlush(
                        new UserBooksEntity(null, user, book, "reading", 50, null, LocalDateTime.now())));
    }
}
