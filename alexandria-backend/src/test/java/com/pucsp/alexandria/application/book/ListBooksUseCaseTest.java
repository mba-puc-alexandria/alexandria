package com.pucsp.alexandria.application.book;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.pucsp.alexandria.application.book.dto.BookOutput;
import com.pucsp.alexandria.domain.book.Book;
import com.pucsp.alexandria.domain.book.BookRepository;
import com.pucsp.alexandria.domain.book.BookSource;
import java.util.List;
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
class ListBooksUseCaseTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private ListBooksUseCase listBooksUseCase;

    @Test
    void shouldReturnAllBooksPaged() {
        Book book1 = Book.restore(1L, "Book 1", "Author 1", null, null, null, null, null, null, 1L, BookSource.LOCAL);
        Book book2 = Book.restore(2L, "Book 2", "Author 2", null, null, null, null, null, null, 1L, BookSource.LOCAL);
        Page<Book> bookPage = new PageImpl<>(List.of(book1, book2));
        Pageable pageable = PageRequest.of(0, 10);

        when(bookRepository.findAll(pageable)).thenReturn(bookPage);

        Page<BookOutput> result = listBooksUseCase.execute(pageable);

        assertEquals(2, result.getContent().size());
        assertEquals("Book 1", result.getContent().get(0).title());
        assertEquals("Book 2", result.getContent().get(1).title());
    }

    @Test
    void shouldReturnEmptyPageWhenNoBooks() {
        Pageable pageable = PageRequest.of(0, 10);
        when(bookRepository.findAll(pageable)).thenReturn(Page.empty());

        Page<BookOutput> result = listBooksUseCase.execute(pageable);

        assertTrue(result.isEmpty());
    }
}
