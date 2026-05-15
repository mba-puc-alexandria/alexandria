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
import com.pucsp.alexandria.domain.userbook.UserBooksStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class ListUserBooksUseCaseTest {

    @Mock
    private UserBooksRepository userBooksRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private AuthorRepository authorRepository;

    @InjectMocks
    private ListUserBooksUseCase listUserBooksUseCase;

    @Test
    void shouldListUserBooks() {
        UserBooks userBook = UserBooks.restore(1L, 1L, 1L, "reading", 50, null, LocalDateTime.now());
        Page<UserBooks> userBooksPage = new PageImpl<>(List.of(userBook));

        Book book = Book.restore(1L, "Title", Set.of(1L), null, null, null, null, null, null, null, BookSource.GUTENDEX);
        Author author = Author.restore(1L, "Author Name", null, null);

        when(userBooksRepository.findByUserId(any(), any(Pageable.class))).thenReturn(userBooksPage);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(authorRepository.findAllById(anySet())).thenReturn(List.of(author));

        Page<UserBooksOutput> result = listUserBooksUseCase.execute(1L, null, PageRequest.of(0, 10));

        assertEquals(1, result.getContent().size());
        assertEquals("reading", result.getContent().get(0).status());
    }

    @Test
    void shouldFilterByStatus() {
        UserBooks userBook = UserBooks.restore(1L, 1L, 1L, "reading", 50, null, LocalDateTime.now());
        Page<UserBooks> userBooksPage = new PageImpl<>(List.of(userBook));

        Book book = Book.restore(1L, "Title", Set.of(1L), null, null, null, null, null, null, null, BookSource.GUTENDEX);
        Author author = Author.restore(1L, "Author", null, null);

        when(userBooksRepository.findByUserIdAndStatus(any(), any(), any(Pageable.class))).thenReturn(userBooksPage);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(authorRepository.findAllById(anySet())).thenReturn(List.of(author));

        Page<UserBooksOutput> result = listUserBooksUseCase.execute(1L, "reading", PageRequest.of(0, 10));

        assertEquals(1, result.getContent().size());
    }
}
