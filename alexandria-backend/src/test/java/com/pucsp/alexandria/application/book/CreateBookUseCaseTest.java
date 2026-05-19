package com.pucsp.alexandria.application.book;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.pucsp.alexandria.application.book.dto.CreateBookInput;
import com.pucsp.alexandria.application.book.dto.CreateBookOutput;
import com.pucsp.alexandria.domain.author.Author;
import com.pucsp.alexandria.domain.author.AuthorRepository;
import com.pucsp.alexandria.domain.book.Book;
import com.pucsp.alexandria.domain.book.BookRepository;
import com.pucsp.alexandria.domain.book.BookSource;
import com.pucsp.alexandria.domain.book.external.AuthorData;
import com.pucsp.alexandria.domain.book.external.BookApiClient;
import com.pucsp.alexandria.domain.book.external.BookData;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
    private AuthorRepository authorRepository;

    @Mock
    private BookApiClient bookApiClient;

    @InjectMocks
    private CreateBookUseCase createBookUseCase;

    @Captor
    private ArgumentCaptor<Book> bookCaptor;

    @Test
    void shouldCreateBooksFromGutendexPage() {
        BookData bookData = new BookData(-1L, 100L, "Dom Casmurro", "Machado de Assis",
                List.of(new AuthorData("Machado de Assis", 1839, 1908)),
                "http://download.com", "http://cover.com", "pt", "Fiction", 5000);

        Author author = Author.restore(1L, "Machado de Assis", 1839, 1908);

        when(bookApiClient.getPage(1)).thenReturn(List.of(bookData));
        when(bookRepository.existsByGutendexId(100L)).thenReturn(false);
        when(authorRepository.findByName("Machado de Assis")).thenReturn(Optional.empty());
        when(authorRepository.save(any(Author.class))).thenReturn(author);

        Book savedBook = Book.restore(1L, "Dom Casmurro", java.util.Set.of(1L), 100L,
                "http://download.com", "http://cover.com", "pt", "Fiction", 5000, null, BookSource.GUTENDEX);
        when(bookRepository.save(any(Book.class))).thenReturn(savedBook);

        CreateBookOutput output = createBookUseCase.execute(new CreateBookInput(1));

        assertEquals(1, output.ids().size());
        assertEquals(1L, output.ids().get(0));
        verify(bookRepository).save(bookCaptor.capture());
        assertEquals("Dom Casmurro", bookCaptor.getValue().getTitle());
        verify(authorRepository).save(any(Author.class));
    }

    @Test
    void shouldSkipExistingGutendexBooks() {
        BookData bookData1 = new BookData(-1L, 100L, "Book 1", "Author 1",
                List.of(new AuthorData("Author 1", null, null)),
                "url1", "url1", "pt", "Fiction", 100);
        BookData bookData2 = new BookData(-1L, 200L, "Book 2", "Author 2",
                List.of(new AuthorData("Author 2", null, null)),
                "url2", "url2", "en", "Drama", 200);

        Author author = Author.restore(2L, "Author 2", null, null);

        when(bookApiClient.getPage(1)).thenReturn(List.of(bookData1, bookData2));
        when(bookRepository.existsByGutendexId(100L)).thenReturn(true);
        when(bookRepository.existsByGutendexId(200L)).thenReturn(false);
        when(authorRepository.findByName("Author 2")).thenReturn(Optional.empty());
        when(authorRepository.save(any(Author.class))).thenReturn(author);

        Book savedBook = Book.restore(2L, "Book 2", java.util.Set.of(2L), 200L,
                "url2", "url2", "en", "Drama", 200, null, BookSource.GUTENDEX);
        when(bookRepository.save(any(Book.class))).thenReturn(savedBook);

        CreateBookOutput output = createBookUseCase.execute(new CreateBookInput(1));

        assertEquals(1, output.ids().size());
        assertEquals(2L, output.ids().get(0));
    }

    @Test
    void shouldReuseExistingAuthor() {
        BookData bookData = new BookData(-1L, 100L, "Dom Casmurro", "Machado de Assis",
                List.of(new AuthorData("Machado de Assis", 1839, 1908)),
                "http://download.com", "http://cover.com", "pt", "Fiction", 5000);

        Author existingAuthor = Author.restore(1L, "Machado de Assis", 1839, 1908);

        when(bookApiClient.getPage(1)).thenReturn(List.of(bookData));
        when(bookRepository.existsByGutendexId(100L)).thenReturn(false);
        when(authorRepository.findByName("Machado de Assis")).thenReturn(Optional.of(existingAuthor));

        Book savedBook = Book.restore(1L, "Dom Casmurro", java.util.Set.of(1L), 100L,
                "http://download.com", "http://cover.com", "pt", "Fiction", 5000, null, BookSource.GUTENDEX);
        when(bookRepository.save(any(Book.class))).thenReturn(savedBook);

        CreateBookOutput output = createBookUseCase.execute(new CreateBookInput(1));

        assertEquals(1, output.ids().size());
        verify(authorRepository, never()).save(any(Author.class));
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

    @Test
    void shouldContinueWhenSingleBookFails() {
        BookData validBook = new BookData(-1L, 100L, "Dom Casmurro",
                "Machado de Assis",
                List.of(new AuthorData("Machado de Assis", 1839, 1908)),
                "url", "url", "pt", "Fiction", 5000);

        BookData invalidBook = new BookData(-1L, 200L, "",
                "Unknown", List.of(), null, null, null, null, null);

        when(bookApiClient.getPage(1)).thenReturn(List.of(validBook, invalidBook));
        when(bookRepository.existsByGutendexId(100L)).thenReturn(false);
        when(bookRepository.existsByGutendexId(200L)).thenReturn(false);

        Author author = Author.restore(1L, "Machado de Assis", 1839, 1908);
        when(authorRepository.findByName("Machado de Assis")).thenReturn(Optional.of(author));

        Book saved = Book.restore(1L, "Dom Casmurro", Set.of(1L), 100L,
                "url", "url", "pt", "Fiction", 5000, null, BookSource.GUTENDEX);
        when(bookRepository.save(any(Book.class))).thenReturn(saved);

        CreateBookOutput output = createBookUseCase.execute(new CreateBookInput(1));

        assertEquals(1, output.ids().size());
    }
}
