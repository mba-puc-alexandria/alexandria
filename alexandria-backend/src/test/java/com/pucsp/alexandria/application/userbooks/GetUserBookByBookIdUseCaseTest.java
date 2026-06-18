package com.pucsp.alexandria.application.userbooks;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.pucsp.alexandria.application.userbooks.dto.UserBooksOutput;
import com.pucsp.alexandria.domain.author.Author;
import com.pucsp.alexandria.domain.author.AuthorRepository;
import com.pucsp.alexandria.domain.book.Book;
import com.pucsp.alexandria.domain.book.BookRepository;
import com.pucsp.alexandria.domain.book.BookSource;
import com.pucsp.alexandria.domain.userbook.UserBooks;
import com.pucsp.alexandria.domain.userbook.UserBooksRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetUserBookByBookIdUseCaseTest {

    @Mock
    private UserBooksRepository userBooksRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private AuthorRepository authorRepository;

    @InjectMocks
    private GetUserBookByBookIdUseCase getUserBookByBookIdUseCase;

    @Test
    void shouldReturnUserBookWhenBookExistsInUserLibrary() {
        Book book = Book.restore(1L, "Dom Casmurro", Set.of(1L),
                null, null, null, null, null, null, null, BookSource.GUTENDEX);
        Author author = Author.restore(1L, "Machado de Assis", 1839, 1908);
        UserBooks userBook = UserBooks.restore(10L, 42L, 1L, "reading", 36, null, LocalDateTime.now());

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(authorRepository.findAllById(anySet())).thenReturn(List.of(author));
        when(userBooksRepository.findByUserIdAndBookId(any(), any())).thenReturn(Optional.of(userBook));

        UserBooksOutput output = getUserBookByBookIdUseCase.execute(42L, 1L);

        assertNotNull(output);
        assertEquals(10L, output.id());
        assertEquals("reading", output.status());
        assertEquals(36, output.progress());
        assertNull(output.rating());
        assertNotNull(output.book());
        assertEquals("Dom Casmurro", output.book().title());
        assertEquals(1, output.book().authors().size());
        assertEquals("Machado de Assis", output.book().authors().get(0).name());
    }

    @Test
    void shouldReturnUserBookWithNullFieldsWhenBookNotInUserLibrary() {
        Book book = Book.restore(1L, "Dom Casmurro", Set.of(1L),
                null, null, null, null, null, null, null, BookSource.GUTENDEX);
        Author author = Author.restore(1L, "Machado de Assis", 1839, 1908);

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(authorRepository.findAllById(anySet())).thenReturn(List.of(author));
        when(userBooksRepository.findByUserIdAndBookId(any(), any())).thenReturn(Optional.empty());

        UserBooksOutput output = getUserBookByBookIdUseCase.execute(42L, 1L);

        assertNotNull(output);
        assertNull(output.id());
        assertNull(output.status());
        assertNull(output.progress());
        assertNull(output.rating());
        assertNotNull(output.book());
        assertEquals("Dom Casmurro", output.book().title());
    }

    @Test
    void shouldThrowExceptionWhenBookNotFound() {
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> getUserBookByBookIdUseCase.execute(42L, 99L));
    }

    @Test
    void shouldReturnUserBookWithMultipleAuthors() {
        Set<Long> authorIds = Set.of(1L, 2L);
        Book book = Book.restore(1L, "Co-authored Book", authorIds,
                null, null, null, null, null, null, null, BookSource.GUTENDEX);
        Author author1 = Author.restore(1L, "Author One", 1900, 1980);
        Author author2 = Author.restore(2L, "Author Two", 1910, 1990);
        UserBooks userBook = UserBooks.restore(10L, 42L, 1L, "reading", 50, null, LocalDateTime.now());

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(authorRepository.findAllById(anySet())).thenReturn(List.of(author1, author2));
        when(userBooksRepository.findByUserIdAndBookId(any(), any())).thenReturn(Optional.of(userBook));

        UserBooksOutput output = getUserBookByBookIdUseCase.execute(42L, 1L);

        assertNotNull(output);
        assertEquals(2, output.book().authors().size());
        assertTrue(output.book().authors().stream().anyMatch(a -> a.name().equals("Author One")));
        assertTrue(output.book().authors().stream().anyMatch(a -> a.name().equals("Author Two")));
    }

    @Test
    void shouldReturnUserBookWithDoneStatusAndRating() {
        Book book = Book.restore(1L, "Moby Dick", Set.of(1L),
                null, null, null, null, null, null, null, BookSource.GUTENDEX);
        Author author = Author.restore(1L, "Herman Melville", 1819, 1891);
        UserBooks userBook = UserBooks.restore(10L, 42L, 1L, "done", null, 4, LocalDateTime.now());

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(authorRepository.findAllById(anySet())).thenReturn(List.of(author));
        when(userBooksRepository.findByUserIdAndBookId(any(), any())).thenReturn(Optional.of(userBook));

        UserBooksOutput output = getUserBookByBookIdUseCase.execute(42L, 1L);

        assertNotNull(output);
        assertEquals("done", output.status());
        assertNull(output.progress());
        assertEquals(4, output.rating());
    }
}
