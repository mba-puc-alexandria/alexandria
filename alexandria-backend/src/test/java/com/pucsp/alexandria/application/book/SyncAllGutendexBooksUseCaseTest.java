package com.pucsp.alexandria.application.book;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.pucsp.alexandria.application.book.dto.CreateBookInput;
import com.pucsp.alexandria.application.book.dto.CreateBookOutput;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SyncAllGutendexBooksUseCaseTest {

    @Mock
    private CreateBookUseCase createBookUseCase;

    @InjectMocks
    private SyncAllGutendexBooksUseCase syncUseCase;

    @Test
    void shouldIterateUntilPageNotFound() {
        when(createBookUseCase.execute(any(CreateBookInput.class)))
                .thenReturn(new CreateBookOutput(List.of(1L, 2L)))
                .thenReturn(new CreateBookOutput(List.of(3L)))
                .thenThrow(new RuntimeException("Page not found in Gutendex: 3"));

        syncUseCase.execute();

        verify(createBookUseCase, times(3)).execute(any(CreateBookInput.class));
    }

    @Test
    void shouldSkipPageOnTransientError() {
        when(createBookUseCase.execute(any(CreateBookInput.class)))
                .thenReturn(new CreateBookOutput(List.of(1L)))
                .thenThrow(new RuntimeException("Read timed out"))
                .thenReturn(new CreateBookOutput(List.of(2L)))
                .thenThrow(new RuntimeException("Page not found in Gutendex: 4"));

        syncUseCase.execute();

        verify(createBookUseCase, times(4)).execute(any(CreateBookInput.class));
    }
}
