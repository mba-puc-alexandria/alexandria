package com.pucsp.alexandria.application.book;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.pucsp.alexandria.application.book.dto.SearchBookOutput;
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
class SearchBookByTitleUseCaseTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private SearchBookByTitleUseCase searchBookByTitleUseCase;

    @Test
    void shouldSearchBooksByQuery() {
        Book book = Book.restore(1L, "Dom Casmurro", "Machado de Assis", 100L,
                "url", "url", "pt", "Fiction", 5000, null, BookSource.GUTENDEX);
        Page<Book> bookPage = new PageImpl<>(List.of(book));
        Pageable pageable = PageRequest.of(0, 10);

        when(bookRepository.searchBookByQuery("Dom", pageable)).thenReturn(bookPage);

        Page<SearchBookOutput> result = searchBookByTitleUseCase.execute("Dom", pageable);

        assertEquals(1, result.getContent().size());
        assertEquals("Dom Casmurro", result.getContent().get(0).title());
    }

    @Test
    void shouldReturnEmptyPageForBlankQuery() {
        Page<SearchBookOutput> result = searchBookByTitleUseCase.execute("", PageRequest.of(0, 10));
        assertTrue(result.isEmpty());
        verifyNoInteractions(bookRepository);
    }

    @Test
    void shouldReturnEmptyPageForNullQuery() {
        Page<SearchBookOutput> result = searchBookByTitleUseCase.execute(null, PageRequest.of(0, 10));
        assertTrue(result.isEmpty());
        verifyNoInteractions(bookRepository);
    }
}
