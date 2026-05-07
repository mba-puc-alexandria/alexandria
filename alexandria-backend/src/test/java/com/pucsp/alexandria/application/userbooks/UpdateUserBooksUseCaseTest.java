package com.pucsp.alexandria.application.userbooks;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.pucsp.alexandria.application.userbooks.dto.UpdateUserBooksInput;
import com.pucsp.alexandria.application.userbooks.dto.UserBooksOutput;
import com.pucsp.alexandria.domain.book.Book;
import com.pucsp.alexandria.domain.book.BookRepository;
import com.pucsp.alexandria.domain.book.BookSource;
import com.pucsp.alexandria.domain.userbook.UserBooks;
import com.pucsp.alexandria.domain.userbook.UserBooksRepository;
import com.pucsp.alexandria.domain.userbook.exception.UserBooksNotFoundException;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdateUserBooksUseCaseTest {

    @Mock
    private UserBooksRepository userBooksRepository;

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private UpdateUserBooksUseCase updateUserBooksUseCase;

    @Test
    void shouldUpdateUserBook() {
        UserBooks existing = UserBooks.restore(1L, 1L, 10L, "toread", null, null, LocalDateTime.now());
        when(userBooksRepository.findById(1L)).thenReturn(Optional.of(existing));
        UserBooks updatedUb = UserBooks.restore(1L, 1L, 10L, "done", null, 4, LocalDateTime.now());
        when(userBooksRepository.save(any(UserBooks.class))).thenReturn(updatedUb);
        Book book = Book.restore(10L, "Title", "Author", 100L,
                "url", "url", "pt", "Fic", 100, null, BookSource.GUTENDEX);
        when(bookRepository.findById(10L)).thenReturn(Optional.of(book));

        UserBooksOutput output = updateUserBooksUseCase.execute(1L, 1L,
                new UpdateUserBooksInput("done", null, 4));

        assertEquals("done", output.status());
        assertEquals(4, output.rating());
    }

    @Test
    void shouldThrowExceptionWhenUserBookNotFound() {
        when(userBooksRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(UserBooksNotFoundException.class,
                () -> updateUserBooksUseCase.execute(1L, 99L,
                        new UpdateUserBooksInput("reading", 50, null)));
    }

    @Test
    void shouldThrowExceptionWhenUserIdDoesNotMatch() {
        UserBooks existing = UserBooks.restore(1L, 2L, 10L, "toread", null, null, LocalDateTime.now());
        when(userBooksRepository.findById(1L)).thenReturn(Optional.of(existing));

        assertThrows(UserBooksNotFoundException.class,
                () -> updateUserBooksUseCase.execute(1L, 1L,
                        new UpdateUserBooksInput("reading", 50, null)));
    }
}
