package com.pucsp.alexandria.application.userbooks;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.pucsp.alexandria.application.userbooks.dto.AddUserBooksInput;
import com.pucsp.alexandria.application.userbooks.dto.UserBooksOutput;
import com.pucsp.alexandria.domain.book.Book;
import com.pucsp.alexandria.domain.book.BookRepository;
import com.pucsp.alexandria.domain.book.BookSource;
import com.pucsp.alexandria.domain.userbook.UserBooks;
import com.pucsp.alexandria.domain.userbook.UserBooksRepository;
import com.pucsp.alexandria.domain.userbook.UserBooksStatus;
import com.pucsp.alexandria.domain.userbook.exception.DuplicateUserBooksException;
import com.pucsp.alexandria.domain.userbook.exception.InvalidUserBooksException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AddUserBooksUseCaseTest {

    @Mock
    private UserBooksRepository userBooksRepository;

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private AddUserBooksUseCase addUserBooksUseCase;

    @Test
    void shouldAddBookToUserLibrary() {
        Book book = Book.restore(10L, "Dom Casmurro", "Machado", 100L,
                "url", "url", "pt", "Fiction", 100, null, BookSource.GUTENDEX);
        when(bookRepository.findById(10L)).thenReturn(Optional.of(book));
        when(userBooksRepository.existsByUserIdAndBookId(any(), any())).thenReturn(false);
        when(userBooksRepository.save(any(UserBooks.class))).thenAnswer(invocation -> {
            UserBooks ub = invocation.getArgument(0);
            return UserBooks.restore(1L, 1L, 10L, "toread", null, null, java.time.LocalDateTime.now());
        });

        UserBooksOutput output = addUserBooksUseCase.execute(1L, new AddUserBooksInput(10L, null));

        assertEquals(1L, output.id());
        assertEquals("toread", output.status());
        assertEquals("Dom Casmurro", output.book().title());
    }

    @Test
    void shouldAddBookWithCustomStatus() {
        Book book = Book.restore(10L, "Title", "Author", 100L,
                "url", "url", "pt", "Fic", 100, null, BookSource.GUTENDEX);
        when(bookRepository.findById(10L)).thenReturn(Optional.of(book));
        when(userBooksRepository.existsByUserIdAndBookId(any(), any())).thenReturn(false);
        when(userBooksRepository.save(any(UserBooks.class))).thenAnswer(invocation -> {
            UserBooks ub = invocation.getArgument(0);
            return UserBooks.restore(1L, 1L, 10L, "toread", null, null, java.time.LocalDateTime.now());
        });

        UserBooksOutput output = addUserBooksUseCase.execute(1L, new AddUserBooksInput(10L, "toread"));

        assertEquals("toread", output.status());
        assertNull(output.progress());
    }

    @Test
    void shouldThrowExceptionWhenBookIdIsNull() {
        InvalidUserBooksException ex = assertThrows(InvalidUserBooksException.class,
                () -> addUserBooksUseCase.execute(1L, new AddUserBooksInput(null, null)));
        assertEquals("bookId is required", ex.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenBookNotFound() {
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> addUserBooksUseCase.execute(1L, new AddUserBooksInput(99L, null)));
    }

    @Test
    void shouldThrowExceptionWhenDuplicate() {
        Book book = Book.restore(10L, "Title", "Author", 100L,
                "url", "url", "pt", "Fic", 100, null, BookSource.GUTENDEX);
        when(bookRepository.findById(10L)).thenReturn(Optional.of(book));
        when(userBooksRepository.existsByUserIdAndBookId(any(), any())).thenReturn(true);

        assertThrows(DuplicateUserBooksException.class,
                () -> addUserBooksUseCase.execute(1L, new AddUserBooksInput(10L, null)));
    }
}
