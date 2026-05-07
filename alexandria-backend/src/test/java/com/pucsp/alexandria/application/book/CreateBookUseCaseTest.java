package com.pucsp.alexandria.application.book;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.pucsp.alexandria.application.book.dto.CreateBookInput;
import com.pucsp.alexandria.application.book.dto.CreateBookOutput;
import com.pucsp.alexandria.domain.book.Book;
import com.pucsp.alexandria.domain.book.BookRepository;
import com.pucsp.alexandria.domain.book.BookSource;
import com.pucsp.alexandria.domain.book.external.BookApiClient;
import com.pucsp.alexandria.domain.book.external.BookData;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateBookUseCaseTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BookApiClient bookApiClient;

    @InjectMocks
    private CreateBookUseCase createBookUseCase;

    @Captor
    private ArgumentCaptor<Book> bookCaptor;

    @Test
    void shouldCreateBooksFromGutendexPage() {
        BookData bookData = new BookData(-1L, 100L, "Dom Casmurro", "Machado de Assis",
                "http://download.com", "http://cover.com", "pt", "Fiction", 5000);
        when(bookApiClient.getPage(1)).thenReturn(List.of(bookData));
        when(bookRepository.existsByGutendexId(100L)).thenReturn(false);
        Book savedBook = Book.restore(1L, "Dom Casmurro", "Machado de Assis", 100L,
                "http://download.com", "http://cover.com", "pt", "Fiction", 5000, null, BookSource.GUTENDEX);
        when(bookRepository.save(any(Book.class))).thenReturn(savedBook);

        CreateBookOutput output = createBookUseCase.execute(new CreateBookInput(1));

        assertEquals(1, output.ids().size());
        assertEquals(1L, output.ids().get(0));
        verify(bookRepository).save(bookCaptor.capture());
        assertEquals("Dom Casmurro", bookCaptor.getValue().getTitle());
    }

    @Test
    void shouldSkipExistingGutendexBooks() {
        BookData bookData1 = new BookData(-1L, 100L, "Book 1", "Author 1",
                "url1", "url1", "pt", "Fiction", 100);
        BookData bookData2 = new BookData(-1L, 200L, "Book 2", "Author 2",
                "url2", "url2", "en", "Drama", 200);
        when(bookApiClient.getPage(1)).thenReturn(List.of(bookData1, bookData2));
        when(bookRepository.existsByGutendexId(100L)).thenReturn(true);
        when(bookRepository.existsByGutendexId(200L)).thenReturn(false);
        Book savedBook = Book.restore(2L, "Book 2", "Author 2", 200L,
                "url2", "url2", "en", "Drama", 200, null, BookSource.GUTENDEX);
        when(bookRepository.save(any(Book.class))).thenReturn(savedBook);

        CreateBookOutput output = createBookUseCase.execute(new CreateBookInput(1));

        assertEquals(1, output.ids().size());
        assertEquals(2L, output.ids().get(0));
    }

    @Test
    void shouldThrowExceptionWhenPageNotFound() {
        when(bookApiClient.getPage(99)).thenReturn(List.of());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> createBookUseCase.execute(new CreateBookInput(99)));
        assertEquals("Page not found in Gutendex: 99", ex.getMessage());
    }

    @Test
    void shouldHandleEmptyResultFromApi() {
        when(bookApiClient.getPage(1)).thenReturn(List.of());

        assertThrows(RuntimeException.class,
                () -> createBookUseCase.execute(new CreateBookInput(1)));
    }
}
