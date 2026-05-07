package com.pucsp.alexandria.application.userbooks;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.pucsp.alexandria.application.userbooks.dto.UserBooksOutput;
import com.pucsp.alexandria.domain.book.Book;
import com.pucsp.alexandria.domain.book.BookRepository;
import com.pucsp.alexandria.domain.book.BookSource;
import com.pucsp.alexandria.domain.userbook.UserBooks;
import com.pucsp.alexandria.domain.userbook.UserBooksRepository;
import com.pucsp.alexandria.domain.userbook.UserBooksStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
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

    @InjectMocks
    private ListUserBooksUseCase listUserBooksUseCase;

    @Test
    void shouldListAllUserBooks() {
        UserBooks ub = UserBooks.restore(1L, 1L, 10L, "toread", null, null, LocalDateTime.now());
        Page<UserBooks> ubPage = new PageImpl<>(List.of(ub));
        Pageable pageable = PageRequest.of(0, 10);

        when(userBooksRepository.findByUserId(any(), eq(pageable))).thenReturn(ubPage);
        Book book = Book.restore(10L, "Dom Casmurro", "Machado", 100L,
                "url", "url", "pt", "Fiction", 100, null, BookSource.GUTENDEX);
        when(bookRepository.findById(10L)).thenReturn(Optional.of(book));

        Page<UserBooksOutput> result = listUserBooksUseCase.execute(1L, null, pageable);

        assertEquals(1, result.getContent().size());
        assertEquals("toread", result.getContent().get(0).status());
        assertEquals("Dom Casmurro", result.getContent().get(0).book().title());
    }

    @Test
    void shouldListUserBooksFilteredByStatus() {
        UserBooks ub = UserBooks.restore(1L, 1L, 10L, "reading", 50, null, LocalDateTime.now());
        Page<UserBooks> ubPage = new PageImpl<>(List.of(ub));
        Pageable pageable = PageRequest.of(0, 10);

        when(userBooksRepository.findByUserIdAndStatus(any(), eq(UserBooksStatus.READING), eq(pageable)))
                .thenReturn(ubPage);
        Book book = Book.restore(10L, "Title", "Author", 100L,
                "url", "url", "pt", "Fic", 100, null, BookSource.GUTENDEX);
        when(bookRepository.findById(10L)).thenReturn(Optional.of(book));

        Page<UserBooksOutput> result = listUserBooksUseCase.execute(1L, "reading", pageable);

        assertEquals(1, result.getContent().size());
        assertEquals("reading", result.getContent().get(0).status());
        assertEquals(50, result.getContent().get(0).progress());
    }
}
