package com.pucsp.alexandria.application.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.pucsp.alexandria.application.auth.dto.RegisterInput;
import com.pucsp.alexandria.application.auth.dto.RegisterOutput;
import com.pucsp.alexandria.domain.user.User;
import com.pucsp.alexandria.domain.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RegisterUserUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RegisterUserUseCase registerUserUseCase;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    @Test
    void shouldRegisterUser() {
        RegisterInput input = new RegisterInput("john_doe", "John", "Doe", "john@example.com", "password123");
                User savedUser = User.restore(1L, "john_doe", "John", "Doe",
                "john@example.com", "password123", java.time.LocalDateTime.now(), com.pucsp.alexandria.domain.user.User.Role.USER);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        RegisterOutput output = registerUserUseCase.execute(input);

        assertEquals(1L, output.id());
        assertEquals("john_doe", output.username());
        assertEquals("john@example.com", output.email());

        verify(userRepository).save(userCaptor.capture());
        assertEquals("john_doe", userCaptor.getValue().getUsername());
    }

    @Test
    void shouldThrowExceptionForInvalidInput() {
        RegisterInput input = new RegisterInput("", "John", "Doe", "john@example.com", "password123");
        assertThrows(Exception.class, () -> registerUserUseCase.execute(input));
        verify(userRepository, never()).save(any());
    }
}
