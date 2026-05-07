package com.pucsp.alexandria.application.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.pucsp.alexandria.application.auth.dto.AuthInput;
import com.pucsp.alexandria.application.auth.dto.AuthOutput;
import com.pucsp.alexandria.domain.user.User;
import com.pucsp.alexandria.domain.user.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthenticateUserUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthenticateUserUseCase authenticateUserUseCase;

    @Test
    void shouldAuthenticateValidUser() {
        User user = User.restore(1L, "john_doe", "John", "Doe",
                "john@example.com", "encodedPassword", java.time.LocalDateTime.now());
        when(userRepository.findByUsername("john_doe")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);

        AuthOutput output = authenticateUserUseCase.execute(new AuthInput("john_doe", "password123"));

        assertEquals(1L, output.userId());
        assertEquals("john_doe", output.username());
        assertNull(output.token());
        assertEquals("Bearer", output.type());
    }

    @Test
    void shouldThrowExceptionForInvalidUsername() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authenticateUserUseCase.execute(new AuthInput("unknown", "pass")));
        assertEquals("Invalid username or password", ex.getMessage());
    }

    @Test
    void shouldThrowExceptionForInvalidPassword() {
        User user = User.restore(1L, "john_doe", "John", "Doe",
                "john@example.com", "encodedPassword", java.time.LocalDateTime.now());
        when(userRepository.findByUsername("john_doe")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "encodedPassword")).thenReturn(false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authenticateUserUseCase.execute(new AuthInput("john_doe", "wrong")));
        assertEquals("Invalid username or password", ex.getMessage());
    }
}
