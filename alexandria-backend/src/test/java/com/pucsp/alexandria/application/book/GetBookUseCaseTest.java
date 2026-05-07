package com.pucsp.alexandria.application.book;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.pucsp.alexandria.application.book.dto.BookOutput;
import com.pucsp.alexandria.domain.book.Book;
import com.pucsp.alexandria.domain.book.BookRepository;
import com.pucsp.alexandria.domain.book.BookSource;
import com.pucsp.alexandria.domain.book.exception.BookNotFoundException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetBookUseCaseTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private GetBookUseCase getBookUseCase;

    @Test
    void shouldReturnBookWhenFound() {
        Book book = Book.restore(1L, "Dom Casmurro", "Machado de Assis", 100L,
                "url", "url", "pt", "Fiction", 5000, null, BookSource.GUTENDEX);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        BookOutput output = getBookUseCase.execute(1L);

        assertNotNull(output);
        assertEquals(1L, output.id().getValue());
        assertEquals("Dom Casmurro", output.title());
        assertEquals("Machado de Assis", output.author());
        assertEquals(BookSource.GUTENDEX.name(), output.source());
    }

    @Test
    void shouldThrowExceptionWhenBookNotFound() {
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        BookNotFoundException ex = assertThrows(BookNotFoundException.class,
                () -> getBookUseCase.execute(99L));
        assertEquals("Book with id 99 not found", ex.getMessage());
    }
}
