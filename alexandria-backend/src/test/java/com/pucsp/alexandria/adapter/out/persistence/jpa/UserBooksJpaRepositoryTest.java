package com.pucsp.alexandria.adapter.out.persistence.jpa;

import static org.junit.jupiter.api.Assertions.*;

import com.pucsp.alexandria.adapter.out.persistence.entity.AuthorEntity;
import com.pucsp.alexandria.adapter.out.persistence.entity.BookEntity;
import com.pucsp.alexandria.adapter.out.persistence.entity.UserBooksEntity;
import com.pucsp.alexandria.adapter.out.persistence.entity.UserEntity;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
    "jwt.secret=test-secret-key-for-tests-min-256-bits",
    "jwt.expiration-ms=86400000"
})
class UserBooksJpaRepositoryTest {

    @Autowired
    private UserBooksJpaRepository userBooksJpaRepository;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private BookJpaRepository bookJpaRepository;

    @Autowired
    private AuthorJpaRepository authorJpaRepository;

    @Test
    void shouldSaveAndFindUserBooks() {
        UserEntity user = userJpaRepository.save(
                new UserEntity(null, "user", "First", "Last", "email@test.com", "pass", LocalDateTime.now()));
        AuthorEntity author = authorJpaRepository.save(new AuthorEntity(null, "Author", null, null));
        BookEntity book = bookJpaRepository.save(
                new BookEntity(null, "Title", Set.of(author), null, null, null, null, null, null, null, "GUTENDEX"));

        UserBooksEntity userBook = new UserBooksEntity(null, user, book, "reading", 50, 4, LocalDateTime.now());
        UserBooksEntity saved = userBooksJpaRepository.save(userBook);

        Optional<UserBooksEntity> found = userBooksJpaRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("reading", found.get().getStatus());
    }
}
