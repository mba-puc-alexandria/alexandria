package com.pucsp.alexandria.application.book;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.pucsp.alexandria.domain.book.Book;
import com.pucsp.alexandria.domain.book.BookRepository;
import com.pucsp.alexandria.domain.book.BookSource;
import com.pucsp.alexandria.domain.book.exception.BookNotFoundException;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DeleteBookUseCaseTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private DeleteBookUseCase deleteBookUseCase;

    @Test
    void shouldDeleteExistingBook() {
        Book book = Book.restore(1L, "Title", Set.of(1L), null, null, null, null, null, null, 1L, BookSource.LOCAL);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        deleteBookUseCase.execute(1L);

        verify(bookRepository).delete(book);
    }

    @Test
    void shouldThrowExceptionWhenBookNotFound() {
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(BookNotFoundException.class, () -> deleteBookUseCase.execute(99L));
        verify(bookRepository, never()).delete(any());
    }
}
