package com.pucsp.alexandria.application.book;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.pucsp.alexandria.application.book.dto.BookOutput;
import com.pucsp.alexandria.application.book.dto.UpdateBookInput;
import com.pucsp.alexandria.domain.author.Author;
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
class UpdateBookUseCaseTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private AuthorRepository authorRepository;

    @InjectMocks
    private UpdateBookUseCase updateBookUseCase;

    @Test
    void shouldUpdateBookTitle() {
        Set<Long> authorIds = Set.of(1L);
        Book existing = Book.restore(1L, "Old Title", authorIds, null, null, null, null, null, null, 1L, BookSource.LOCAL);
        Book updatedBook = Book.restore(1L, "New Title", authorIds, null, null, null, null, null, null, 1L, BookSource.LOCAL);

        Author author = Author.restore(1L, "Author Name", null, null);

        when(bookRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(bookRepository.save(any(Book.class))).thenReturn(updatedBook);
        when(authorRepository.findAllById(anySet())).thenReturn(List.of(author));

        BookOutput output = updateBookUseCase.execute(1L, new UpdateBookInput("New Title"));

        assertEquals("New Title", output.title());
        assertEquals("Author Name", output.authors().get(0).name());
        verify(bookRepository).save(any(Book.class));
    }

    @Test
    void shouldThrowExceptionWhenBookNotFound() {
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(BookNotFoundException.class,
                () -> updateBookUseCase.execute(99L, new UpdateBookInput("New Title")));
    }
}
