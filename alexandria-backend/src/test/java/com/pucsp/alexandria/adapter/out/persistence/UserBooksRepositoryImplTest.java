package com.pucsp.alexandria.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.pucsp.alexandria.adapter.out.persistence.entity.BookEntity;
import com.pucsp.alexandria.adapter.out.persistence.entity.UserBooksEntity;
import com.pucsp.alexandria.adapter.out.persistence.entity.UserEntity;
import com.pucsp.alexandria.adapter.out.persistence.jpa.BookJpaRepository;
import com.pucsp.alexandria.adapter.out.persistence.jpa.UserBooksJpaRepository;
import com.pucsp.alexandria.adapter.out.persistence.jpa.UserJpaRepository;
import com.pucsp.alexandria.adapter.out.persistence.mapper.UserBooksMapper;
import com.pucsp.alexandria.domain.book.BookId;
import com.pucsp.alexandria.domain.user.UserId;
import com.pucsp.alexandria.domain.userbook.UserBooks;
import com.pucsp.alexandria.domain.userbook.UserBooksRepository;
import com.pucsp.alexandria.domain.userbook.UserBooksStatus;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserBooksRepositoryImplTest {

    @Mock
    private UserBooksJpaRepository jpaRepository;

    @Mock
    private UserJpaRepository userJpaRepository;

    @Mock
    private BookJpaRepository bookJpaRepository;

    @Mock
    private UserBooksMapper mapper;

    @InjectMocks
    private UserBooksRepositoryImpl userBooksRepository;

    @Test
    void shouldSaveUserBook() {
        UserId userId = UserId.from(1L);
        BookId bookId = BookId.from(1L);
        UserBooks userBook = UserBooks.create(userId, bookId, UserBooksStatus.TOREAD);

        UserEntity userEntity = new UserEntity();
        BookEntity bookEntity = new BookEntity(1L, "Title", Set.of(), null, null, null, null, null, null, 1L, "LOCAL");
        UserBooksEntity entity = new UserBooksEntity(null, userEntity, bookEntity, "toread", null, null, LocalDateTime.now());
        UserBooksEntity savedEntity = new UserBooksEntity(1L, userEntity, bookEntity, "toread", null, null, LocalDateTime.now());
        UserBooks savedUserBook = UserBooks.restore(1L, 1L, 1L, "toread", null, null, LocalDateTime.now());

        when(userJpaRepository.getReferenceById(1L)).thenReturn(userEntity);
        when(bookJpaRepository.getReferenceById(1L)).thenReturn(bookEntity);
        when(mapper.toPersistence(userBook, userEntity, bookEntity)).thenReturn(entity);
        when(jpaRepository.save(entity)).thenReturn(savedEntity);
        when(mapper.toDomain(savedEntity)).thenReturn(savedUserBook);

        UserBooks result = userBooksRepository.save(userBook);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void shouldFindById() {
        UserBooksEntity entity = new UserBooksEntity();
        UserBooks userBook = UserBooks.restore(1L, 1L, 1L, "toread", null, null, LocalDateTime.now());

        when(jpaRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(userBook);

        Optional<UserBooks> result = userBooksRepository.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
    }

    @Test
    void translatesOnlyDuplicateKeyForConfirmedUserBookPair() {
        var userBook = UserBooks.create(UserId.from(1L), BookId.from(2L), UserBooksStatus.TOREAD);
        var entity = new UserBooksEntity();
        when(mapper.toPersistence(eq(userBook), any(), any())).thenReturn(entity);
        var duplicate = new org.springframework.dao.DataIntegrityViolationException("duplicate", new java.sql.SQLException("duplicate", "23000", 1062));
        when(jpaRepository.save(entity)).thenThrow(duplicate);
        when(jpaRepository.existsByUserIdAndBookId(1L, 2L)).thenReturn(true);
        assertThrows(com.pucsp.alexandria.domain.userbook.exception.DuplicateUserBooksException.class,
            () -> userBooksRepository.save(userBook));
    }

    @Test
    void foreignKeyAndUnrelatedUniqueViolationsAreNotReportedAsDuplicate() {
        var userBook = UserBooks.create(UserId.from(1L), BookId.from(2L), UserBooksStatus.TOREAD);
        var entity = new UserBooksEntity();
        when(mapper.toPersistence(eq(userBook), any(), any())).thenReturn(entity);
        var foreignKey = new org.springframework.dao.DataIntegrityViolationException("fk", new java.sql.SQLException("fk", "23000", 1452));
        when(jpaRepository.save(entity)).thenThrow(foreignKey);
        assertSame(foreignKey, assertThrows(org.springframework.dao.DataIntegrityViolationException.class,
            () -> userBooksRepository.save(userBook)));
        verify(jpaRepository, never()).existsByUserIdAndBookId(anyLong(), anyLong());
        var unrelated = new org.springframework.dao.DataIntegrityViolationException("other unique", new java.sql.SQLException("other", "23000", 1062));
        doThrow(unrelated).when(jpaRepository).save(entity);
        when(jpaRepository.existsByUserIdAndBookId(1L, 2L)).thenReturn(false);
        assertSame(unrelated, assertThrows(org.springframework.dao.DataIntegrityViolationException.class,
            () -> userBooksRepository.save(userBook)));
    }
}
