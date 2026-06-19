package com.pucsp.alexandria.application.profile;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.pucsp.alexandria.application.profile.dto.ProfileOutput;
import com.pucsp.alexandria.application.profile.dto.UpdateProfileInput;
import com.pucsp.alexandria.domain.user.User;
import com.pucsp.alexandria.domain.user.User.Role;
import com.pucsp.alexandria.domain.user.UserRepository;
import com.pucsp.alexandria.domain.user.exception.DuplicateUserException;
import com.pucsp.alexandria.domain.user.exception.UserNotFoundException;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdateProfileUseCaseTest {

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private UpdateProfileUseCase updateProfileUseCase;

  @Captor
  private ArgumentCaptor<User> userCaptor;

    @Test
  void shouldUpdateProfile() {
    User user = User.restore(1L, "john_doe", "John", "Doe",
        "john@example.com", "password123", LocalDateTime.now(), Role.USER);
    when(userRepository.findById(1L)).thenReturn(java.util.Optional.of(user));

    User updatedUser = User.restore(1L, "john_novo", "John", "Silva",
        "john@example.com", "password123", LocalDateTime.now(), Role.USER);
    when(userRepository.save(any(User.class))).thenReturn(updatedUser);

    UpdateProfileInput input = new UpdateProfileInput("john_novo", "John", "Silva");
    ProfileOutput output = updateProfileUseCase.execute(1L, input);

    assertEquals("john_novo", output.username());
    assertEquals("John", output.firstName());
    assertEquals("Silva", output.lastName());

    verify(userRepository).save(userCaptor.capture());
    assertEquals("john_novo", userCaptor.getValue().getUsername());
    assertEquals("Silva", userCaptor.getValue().getLastName());
  }

    @Test
  void shouldUpdateProfilePartially() {
    User user = User.restore(1L, "john_doe", "John", "Doe",
        "john@example.com", "password123", LocalDateTime.now(), Role.USER);
    when(userRepository.findById(1L)).thenReturn(java.util.Optional.of(user));

    User updatedUser = User.restore(1L, "john_doe", "Joao", "Doe",
        "john@example.com", "password123", LocalDateTime.now(), Role.USER);
    when(userRepository.save(any(User.class))).thenReturn(updatedUser);

    UpdateProfileInput input = new UpdateProfileInput(null, "Joao", null);
    ProfileOutput output = updateProfileUseCase.execute(1L, input);

    assertEquals("john_doe", output.username());
    assertEquals("Joao", output.firstName());
    assertEquals("Doe", output.lastName());
  }

  @Test
  void shouldThrowUserNotFoundException() {
    when(userRepository.findById(99L)).thenReturn(java.util.Optional.empty());

    UpdateProfileInput input = new UpdateProfileInput("john", "John", "Doe");
    assertThrows(UserNotFoundException.class, () -> updateProfileUseCase.execute(99L, input));
  }

    @Test
  void shouldThrowDuplicateUserException() {
    User user = User.restore(1L, "john_doe", "John", "Doe",
        "john@example.com", "password123", LocalDateTime.now(), Role.USER);
    when(userRepository.findById(1L)).thenReturn(java.util.Optional.of(user));
    when(userRepository.existsByUsername("john_novo")).thenReturn(true);

    UpdateProfileInput input = new UpdateProfileInput("john_novo", "John", "Doe");
    assertThrows(DuplicateUserException.class, () -> updateProfileUseCase.execute(1L, input));
  }
}
