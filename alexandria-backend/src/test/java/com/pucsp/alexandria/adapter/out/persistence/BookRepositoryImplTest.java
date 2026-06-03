package com.pucsp.alexandria.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.pucsp.alexandria.adapter.out.persistence.entity.AuthorEntity;
import com.pucsp.alexandria.adapter.out.persistence.entity.BookEntity;
import com.pucsp.alexandria.adapter.out.persistence.jpa.AuthorJpaRepository;
import com.pucsp.alexandria.adapter.out.persistence.jpa.BookJpaRepository;
import com.pucsp.alexandria.adapter.out.persistence.mapper.BookMapper;
import com.pucsp.alexandria.domain.author.AuthorId;
import com.pucsp.alexandria.domain.book.Book;
import com.pucsp.alexandria.domain.book.BookRepository;
import com.pucsp.alexandria.domain.book.BookSource;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BookRepositoryImplTest {

    @Mock
    private BookJpaRepository jpaRepository;

    @Mock
    private AuthorJpaRepository authorJpaRepository;

    @Mock
    private BookMapper mapper;

    @InjectMocks
    private BookRepositoryImpl bookRepository;

    @Test
    void shouldSaveBook() {
        Set<AuthorId> authorIdSet = Set.of(AuthorId.from(1L));
        Book book = Book.createLocal("Title", authorIdSet, 1L);
        BookEntity entity = new BookEntity();
        AuthorEntity authorEntity = new AuthorEntity(1L, "Author", null, null);
        BookEntity savedEntity = new BookEntity(1L, "Title", Set.of(authorEntity), null, null, null, null, null, null, 1L, "LOCAL");
        Book savedBook = Book.restore(1L, "Title", Set.of(1L), null, null, null, null, null, null, 1L, BookSource.LOCAL);

        when(mapper.toPersistence(book)).thenReturn(entity);
        when(authorJpaRepository.getReferenceById(1L)).thenReturn(authorEntity);
        when(jpaRepository.save(entity)).thenReturn(savedEntity);
        when(mapper.toDomain(savedEntity)).thenReturn(savedBook);

        Book result = bookRepository.save(book);

        assertNotNull(result);
        assertEquals(1L, result.getId().getValue());
    }

    @Test
    void shouldFindById() {
        BookEntity entity = new BookEntity(1L, "Title", Set.of(), null, null, null, null, null, null, 1L, "LOCAL");
        Book book = Book.restore(1L, "Title", Set.of(1L), null, null, null, null, null, null, 1L, BookSource.LOCAL);

        when(jpaRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(book);

        Optional<Book> result = bookRepository.findById(1L);

        assertTrue(result.isPresent());
        assertEquals("Title", result.get().getTitle());
    }

    @Test
    void shouldReturnEmptyWhenNotFound() {
        when(jpaRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Book> result = bookRepository.findById(99L);

        assertTrue(result.isEmpty());
    }
}
