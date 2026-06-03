package com.pucsp.alexandria.application.userbooks;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.pucsp.alexandria.application.userbooks.dto.UpdateUserBooksInput;
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
class UpdateUserBooksUseCaseTest {

    @Mock
    private UserBooksRepository userBooksRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private AuthorRepository authorRepository;

    @InjectMocks
    private UpdateUserBooksUseCase updateUserBooksUseCase;

    @Test
    void shouldUpdateUserBookFromToreadToReading() {
        UserBooks existing = UserBooks.restore(1L, 1L, 1L, "toread", null, null, LocalDateTime.now());
        UserBooks updated = UserBooks.restore(1L, 1L, 1L, "reading", 25, null, LocalDateTime.now());

        Book book = Book.restore(1L, "Title", Set.of(1L), null, null, null, null, null, null, null, BookSource.GUTENDEX);
        Author author = Author.restore(1L, "Author", null, null);

        when(userBooksRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userBooksRepository.save(any())).thenReturn(updated);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(authorRepository.findAllById(anySet())).thenReturn(List.of(author));

        UserBooksOutput output = updateUserBooksUseCase.execute(1L, 1L, new UpdateUserBooksInput("reading", 25, null));

        assertEquals("reading", output.status());
        assertEquals(25, output.progress());
    }
}
