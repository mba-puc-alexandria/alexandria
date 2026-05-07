package com.pucsp.alexandria.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.*;

import com.pucsp.alexandria.adapter.out.persistence.mapper.UserBooksMapper;
import com.pucsp.alexandria.domain.book.Book;
import com.pucsp.alexandria.domain.book.BookId;
import com.pucsp.alexandria.domain.user.User;
import com.pucsp.alexandria.domain.user.UserId;
import com.pucsp.alexandria.domain.userbook.UserBooks;
import com.pucsp.alexandria.domain.userbook.UserBooksStatus;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@DataJpaTest
@Import({UserBooksRepositoryImpl.class, UserBooksMapper.class, BookRepositoryImpl.class,
    com.pucsp.alexandria.adapter.out.persistence.mapper.BookMapper.class,
    UserRepositoryImpl.class, com.pucsp.alexandria.adapter.out.persistence.mapper.UserMapper.class})
class UserBooksRepositoryImplTest {

    @Autowired
    private UserBooksRepositoryImpl userBooksRepository;

    @Autowired
    private BookRepositoryImpl bookRepository;

    @Autowired
    private UserRepositoryImpl userRepository;

    private UserId userId;
    private BookId bookId;

    @BeforeEach
    void setUp() {
        User user = userRepository.save(
                User.create("test_user", "Test", "User", "test@test.com", "password123"));
        userId = user.getId();

        Book book = bookRepository.save(
                Book.createFromGutendex(100L, "Test Book", "Author",
                        null, null, "en", null, 0));
        bookId = book.getId();
    }

    @Test
    void shouldSaveNewUserBook() {
        UserBooks ub = UserBooks.create(userId, bookId, UserBooksStatus.TOREAD);
        UserBooks saved = userBooksRepository.save(ub);

        assertNotNull(saved.getId());
        assertEquals(userId.getValue(), saved.getUserId().getValue());
        assertEquals(bookId.getValue(), saved.getBookId().getValue());
        assertEquals(UserBooksStatus.TOREAD, saved.getStatus());
        assertNull(saved.getProgress());
        assertNull(saved.getRating());
    }

    @Test
    void shouldSaveUserBookWithReadingStatus() {
        UserBooks ub = UserBooks.restore(null, userId.getValue(), bookId.getValue(),
                "reading", 50, null, java.time.LocalDateTime.now());
        UserBooks saved = userBooksRepository.save(ub);

        assertNotNull(saved.getId());
        assertEquals(UserBooksStatus.READING, saved.getStatus());
        assertEquals(50, saved.getProgress());
    }

    @Test
    void shouldSaveUserBookWithDoneStatus() {
        UserBooks ub = UserBooks.restore(null, userId.getValue(), bookId.getValue(),
                "done", null, 4, java.time.LocalDateTime.now());
        UserBooks saved = userBooksRepository.save(ub);

        assertNotNull(saved.getId());
        assertEquals(UserBooksStatus.DONE, saved.getStatus());
        assertEquals(4, saved.getRating());
    }

    @Test
    void shouldUpdateExistingUserBook() {
        UserBooks ub = UserBooks.create(userId, bookId, UserBooksStatus.TOREAD);
        UserBooks saved = userBooksRepository.save(ub);

        UserBooks updated = saved.updateWith(UserBooksStatus.DONE, null, 5);
        UserBooks savedUpdated = userBooksRepository.save(updated);

        assertEquals(saved.getId(), savedUpdated.getId());
        assertEquals(UserBooksStatus.DONE, savedUpdated.getStatus());
        assertEquals(5, savedUpdated.getRating());
    }

    @Test
    void shouldFindById() {
        UserBooks saved = userBooksRepository.save(
                UserBooks.create(userId, bookId, UserBooksStatus.TOREAD));

        Optional<UserBooks> found = userBooksRepository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals(saved.getId(), found.get().getId());
        assertEquals(UserBooksStatus.TOREAD, found.get().getStatus());
    }

    @Test
    void shouldReturnEmptyWhenNotFoundById() {
        Optional<UserBooks> found = userBooksRepository.findById(999L);
        assertFalse(found.isPresent());
    }

    @Test
    void shouldFindByUserId() {
        userBooksRepository.save(UserBooks.create(userId, bookId, UserBooksStatus.TOREAD));

        Page<UserBooks> result = userBooksRepository.findByUserId(userId, PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void shouldFindByUserIdAndStatus() {
        userBooksRepository.save(UserBooks.create(userId, bookId, UserBooksStatus.TOREAD));

        Page<UserBooks> result = userBooksRepository.findByUserIdAndStatus(
                userId, UserBooksStatus.TOREAD, PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void shouldNotFindByUserIdAndDifferentStatus() {
        userBooksRepository.save(UserBooks.create(userId, bookId, UserBooksStatus.TOREAD));

        Page<UserBooks> result = userBooksRepository.findByUserIdAndStatus(
                userId, UserBooksStatus.READING, PageRequest.of(0, 10));

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldFindByUserIdAndBookId() {
        userBooksRepository.save(UserBooks.create(userId, bookId, UserBooksStatus.TOREAD));

        Optional<UserBooks> found = userBooksRepository.findByUserIdAndBookId(userId, bookId);

        assertTrue(found.isPresent());
    }

    @Test
    void shouldNotFindByUserIdAndBookIdWhenNotExists() {
        Optional<UserBooks> found = userBooksRepository.findByUserIdAndBookId(userId, bookId);
        assertFalse(found.isPresent());
    }

    @Test
    void shouldCheckExistenceByUserIdAndBookId() {
        assertFalse(userBooksRepository.existsByUserIdAndBookId(userId, bookId));

        userBooksRepository.save(UserBooks.create(userId, bookId, UserBooksStatus.TOREAD));

        assertTrue(userBooksRepository.existsByUserIdAndBookId(userId, bookId));
    }

    @Test
    void shouldDeleteUserBook() {
        UserBooks saved = userBooksRepository.save(
                UserBooks.create(userId, bookId, UserBooksStatus.TOREAD));

        userBooksRepository.delete(saved);

        assertFalse(userBooksRepository.findById(saved.getId()).isPresent());
    }
}
