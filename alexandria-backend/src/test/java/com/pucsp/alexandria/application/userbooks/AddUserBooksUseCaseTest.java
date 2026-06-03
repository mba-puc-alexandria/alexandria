package com.pucsp.alexandria.application.userbooks;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.pucsp.alexandria.application.userbooks.dto.AddUserBooksInput;
import com.pucsp.alexandria.application.userbooks.dto.UserBooksOutput;
import com.pucsp.alexandria.domain.author.Author;
import com.pucsp.alexandria.domain.author.AuthorRepository;
import com.pucsp.alexandria.domain.book.Book;
import com.pucsp.alexandria.domain.book.BookRepository;
import com.pucsp.alexandria.domain.book.BookSource;
import com.pucsp.alexandria.domain.userbook.UserBooksRepository;
import com.pucsp.alexandria.domain.userbook.exception.DuplicateUserBooksException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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

    @Mock
    private AuthorRepository authorRepository;

    @InjectMocks
    private AddUserBooksUseCase addUserBooksUseCase;

    @Test
    void shouldAddBookToUserLibrary() {
        Set<Long> authorIds = Set.of(1L);
        Book book = Book.restore(1L, "Title", authorIds, null, null, null, null, null, null, null, BookSource.GUTENDEX);
        Author author = Author.restore(1L, "Author Name", null, null);

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(userBooksRepository.existsByUserIdAndBookId(any(), any())).thenReturn(false);
        when(userBooksRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(authorRepository.findAllById(anySet())).thenReturn(List.of(author));

        UserBooksOutput output = addUserBooksUseCase.execute(1L, new AddUserBooksInput(1L, null));

        assertNotNull(output);
        assertEquals("toread", output.status());
        verify(bookRepository).findById(1L);
    }

    @Test
    void shouldThrowExceptionWhenBookNotFound() {
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> addUserBooksUseCase.execute(1L, new AddUserBooksInput(99L, null)));
    }

    @Test
    void shouldThrowExceptionWhenBookAlreadyInLibrary() {
        Book book = Book.restore(1L, "Title", Set.of(1L), null, null, null, null, null, null, null, BookSource.GUTENDEX);

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(userBooksRepository.existsByUserIdAndBookId(any(), any())).thenReturn(true);

        assertThrows(DuplicateUserBooksException.class,
                () -> addUserBooksUseCase.execute(1L, new AddUserBooksInput(1L, null)));
    }
}
