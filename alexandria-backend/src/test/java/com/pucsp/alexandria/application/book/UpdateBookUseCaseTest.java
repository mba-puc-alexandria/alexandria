package com.pucsp.alexandria.application.book;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.pucsp.alexandria.application.book.dto.BookOutput;
import com.pucsp.alexandria.application.book.dto.UpdateBookInput;
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
class UpdateBookUseCaseTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private UpdateBookUseCase updateBookUseCase;

    @Test
    void shouldUpdateBookTitle() {
        Book existing = Book.restore(1L, "Old Title", "Author", null, null, null, null, null, null, 1L, BookSource.LOCAL);
        Book updatedBook = Book.restore(1L, "New Title", "Author", null, null, null, null, null, null, 1L, BookSource.LOCAL);

        when(bookRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(bookRepository.save(any(Book.class))).thenReturn(updatedBook);

        BookOutput output = updateBookUseCase.execute(1L, new UpdateBookInput("New Title"));

        assertEquals("New Title", output.title());
        verify(bookRepository).save(any(Book.class));
    }

    @Test
    void shouldThrowExceptionWhenBookNotFound() {
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(BookNotFoundException.class,
                () -> updateBookUseCase.execute(99L, new UpdateBookInput("New Title")));
    }
}
