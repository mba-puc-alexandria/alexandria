package com.pucsp.alexandria.application.profile;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.pucsp.alexandria.application.profile.dto.ProfileOutput;
import com.pucsp.alexandria.domain.user.User;
import com.pucsp.alexandria.domain.user.UserRepository;
import com.pucsp.alexandria.domain.user.exception.UserNotFoundException;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetProfileUseCaseTest {

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private GetProfileUseCase getProfileUseCase;

  @Test
  void shouldGetProfile() {
    User user = User.restore(1L, "john_doe", "John", "Doe",
        "john@example.com", "password123", LocalDateTime.now());
    when(userRepository.findById(1L)).thenReturn(java.util.Optional.of(user));

    ProfileOutput output = getProfileUseCase.execute(1L);

    assertEquals(1L, output.userId());
    assertEquals("john_doe", output.username());
    assertEquals("John", output.firstName());
    assertEquals("Doe", output.lastName());
    assertEquals("john@example.com", output.email());
  }

  @Test
  void shouldThrowUserNotFoundException() {
    when(userRepository.findById(99L)).thenReturn(java.util.Optional.empty());

    assertThrows(UserNotFoundException.class, () -> getProfileUseCase.execute(99L));
  }
}
