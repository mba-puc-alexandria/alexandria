package com.pucsp.alexandria.application.userbooks;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.pucsp.alexandria.domain.userbook.UserBooks;
import com.pucsp.alexandria.domain.userbook.UserBooksRepository;
import com.pucsp.alexandria.domain.userbook.exception.UserBooksNotFoundException;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RemoveUserBooksUseCaseTest {

    @Mock
    private UserBooksRepository userBooksRepository;

    @InjectMocks
    private RemoveUserBooksUseCase removeUserBooksUseCase;

    @Test
    void shouldRemoveUserBook() {
        UserBooks ub = UserBooks.restore(1L, 1L, 10L, "toread", null, null, LocalDateTime.now());
        when(userBooksRepository.findById(1L)).thenReturn(Optional.of(ub));

        removeUserBooksUseCase.execute(1L, 1L);

        verify(userBooksRepository).delete(ub);
    }

    @Test
    void shouldThrowExceptionWhenUserBookNotFound() {
        when(userBooksRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(UserBooksNotFoundException.class,
                () -> removeUserBooksUseCase.execute(1L, 99L));
    }

    @Test
    void shouldThrowExceptionWhenUserIdDoesNotMatch() {
        UserBooks ub = UserBooks.restore(1L, 2L, 10L, "toread", null, null, LocalDateTime.now());
        when(userBooksRepository.findById(1L)).thenReturn(Optional.of(ub));

        assertThrows(UserBooksNotFoundException.class,
                () -> removeUserBooksUseCase.execute(1L, 1L));
    }
}
