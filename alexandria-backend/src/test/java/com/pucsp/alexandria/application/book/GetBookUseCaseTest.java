package com.pucsp.alexandria.application.book;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.pucsp.alexandria.application.book.dto.BookOutput;
import com.pucsp.alexandria.domain.author.Author;
import com.pucsp.alexandria.domain.author.AuthorId;
import com.pucsp.alexandria.domain.author.AuthorRepository;
import com.pucsp.alexandria.domain.book.Book;
import com.pucsp.alexandria.domain.book.BookRepository;
import com.pucsp.alexandria.domain.book.BookSource;
import com.pucsp.alexandria.domain.book.exception.BookNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetBookUseCaseTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private AuthorRepository authorRepository;

    @InjectMocks
    private GetBookUseCase getBookUseCase;

    @Test
    void shouldReturnBookWhenFound() {
        Set<Long> authorIds = Set.of(1L);
        Book book = Book.restore(1L, "Dom Casmurro", authorIds, 100L,
                "url", "url", "pt", "Fiction", 5000, null, BookSource.GUTENDEX);

        Author author = Author.restore(1L, "Machado de Assis", 1839, 1908);

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(authorRepository.findAllById(anySet())).thenReturn(List.of(author));

        BookOutput output = getBookUseCase.execute(1L);

        assertNotNull(output);
        assertEquals(1L, output.id());
        assertEquals("Dom Casmurro", output.title());
        assertEquals(1, output.authors().size());
        assertEquals("Machado de Assis", output.authors().get(0).name());
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
