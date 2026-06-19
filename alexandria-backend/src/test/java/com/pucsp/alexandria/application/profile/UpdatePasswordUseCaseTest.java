package com.pucsp.alexandria.application.profile;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.pucsp.alexandria.application.profile.dto.UpdatePasswordInput;
import com.pucsp.alexandria.domain.user.User;
import com.pucsp.alexandria.domain.user.User.Role;
import com.pucsp.alexandria.domain.user.UserRepository;
import com.pucsp.alexandria.domain.user.exception.InvalidCredentialsException;
import com.pucsp.alexandria.domain.user.exception.UserNotFoundException;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UpdatePasswordUseCaseTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @InjectMocks
  private UpdatePasswordUseCase updatePasswordUseCase;

  @Captor
  private ArgumentCaptor<User> userCaptor;

    @Test
  void shouldUpdatePassword() {
    User user = User.restore(1L, "john_doe", "John", "Doe",
        "john@example.com", "encodedOldPassword", LocalDateTime.now(), Role.USER);
    when(userRepository.findById(1L)).thenReturn(java.util.Optional.of(user));
    when(passwordEncoder.matches("oldPass123", "encodedOldPassword")).thenReturn(true);
    when(passwordEncoder.encode("newPass456")).thenReturn("encodedNewPassword");

    User savedUser = User.restore(1L, "john_doe", "John", "Doe",
        "john@example.com", "encodedNewPassword", LocalDateTime.now(), Role.USER);
    when(userRepository.save(any(User.class))).thenReturn(savedUser);

    UpdatePasswordInput input = new UpdatePasswordInput("oldPass123", "newPass456");
    updatePasswordUseCase.execute(1L, input);

    verify(userRepository).save(userCaptor.capture());
    assertEquals("encodedNewPassword", userCaptor.getValue().getPassword());
  }

  @Test
  void shouldThrowUserNotFoundException() {
    when(userRepository.findById(99L)).thenReturn(java.util.Optional.empty());

    UpdatePasswordInput input = new UpdatePasswordInput("old", "new");
    assertThrows(UserNotFoundException.class, () -> updatePasswordUseCase.execute(99L, input));
  }

    @Test
  void shouldThrowInvalidCredentialsException() {
    User user = User.restore(1L, "john_doe", "John", "Doe",
        "john@example.com", "encodedOldPassword", LocalDateTime.now(), Role.USER);
    when(userRepository.findById(1L)).thenReturn(java.util.Optional.of(user));
    when(passwordEncoder.matches("wrongPass", "encodedOldPassword")).thenReturn(false);

    UpdatePasswordInput input = new UpdatePasswordInput("wrongPass", "newPass456");
    assertThrows(InvalidCredentialsException.class, () -> updatePasswordUseCase.execute(1L, input));
  }
}
