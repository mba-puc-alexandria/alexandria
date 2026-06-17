package com.pucsp.alexandria.adapter.in.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pucsp.alexandria.adapter.in.rest.auth.dto.AuthRequest;
import com.pucsp.alexandria.adapter.in.rest.profile.dto.UpdatePasswordRequest;
import com.pucsp.alexandria.adapter.in.rest.profile.dto.UpdateProfileRequest;
import com.pucsp.alexandria.application.auth.AuthenticateUserUseCase;
import com.pucsp.alexandria.application.auth.GoogleAuthUseCase;
import com.pucsp.alexandria.application.auth.RegisterUserUseCase;
import com.pucsp.alexandria.application.auth.dto.AuthInput;
import com.pucsp.alexandria.application.auth.dto.AuthOutput;
import com.pucsp.alexandria.application.profile.GetProfileUseCase;
import com.pucsp.alexandria.application.profile.UpdatePasswordUseCase;
import com.pucsp.alexandria.application.profile.UpdateProfileUseCase;
import com.pucsp.alexandria.application.profile.dto.ProfileOutput;
import com.pucsp.alexandria.application.profile.dto.UpdatePasswordInput;
import com.pucsp.alexandria.application.profile.dto.UpdateProfileInput;
import com.pucsp.alexandria.config.jwt.JwtTokenProvider;
import com.pucsp.alexandria.domain.user.exception.DuplicateUserException;
import com.pucsp.alexandria.domain.user.exception.InvalidCredentialsException;
import com.pucsp.alexandria.domain.user.exception.InvalidUserException;
import com.pucsp.alexandria.domain.user.exception.UserNotFoundException;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {
    "jwt.secret=test-secret-key-for-tests-min-256-bits",
    "jwt.expiration-ms=86400000"
})
@AutoConfigureMockMvc
class ProfileControllerIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @MockitoBean
  private GoogleAuthUseCase googleAuthUseCase;

  @MockitoBean
  private RegisterUserUseCase registerUserUseCase;

  @MockitoBean
  private AuthenticateUserUseCase authenticateUserUseCase;

  @MockitoBean
  private PasswordEncoder passwordEncoder;

  @MockitoBean
  private JwtTokenProvider jwtTokenProvider;

  @MockitoBean
  private GetProfileUseCase getProfileUseCase;

  @MockitoBean
  private UpdateProfileUseCase updateProfileUseCase;

  @MockitoBean
  private UpdatePasswordUseCase updatePasswordUseCase;

  @Test
  void shouldGetProfile() throws Exception {
    when(jwtTokenProvider.validateToken("valid-token")).thenReturn(true);
    when(jwtTokenProvider.getUsernameFromToken("valid-token")).thenReturn("john_doe");
    when(jwtTokenProvider.getUserIdFromToken("valid-token")).thenReturn(1L);
    when(getProfileUseCase.execute(1L))
        .thenReturn(new ProfileOutput(1L, "john_doe", "John", "Doe",
            "john@example.com", LocalDateTime.now()));

    mockMvc.perform(get("/profile/me")
            .header("Authorization", "Bearer valid-token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.userId").value(1))
        .andExpect(jsonPath("$.username").value("john_doe"))
        .andExpect(jsonPath("$.firstName").value("John"))
        .andExpect(jsonPath("$.lastName").value("Doe"))
        .andExpect(jsonPath("$.email").value("john@example.com"));
  }

  @Test
  void shouldReturn401WhenNotAuthenticated() throws Exception {
    mockMvc.perform(get("/profile/me"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void shouldUpdateProfile() throws Exception {
    when(jwtTokenProvider.validateToken("valid-token")).thenReturn(true);
    when(jwtTokenProvider.getUsernameFromToken("valid-token")).thenReturn("john_doe");
    when(jwtTokenProvider.getUserIdFromToken("valid-token")).thenReturn(1L);
    when(updateProfileUseCase.execute(eq(1L), any(UpdateProfileInput.class)))
        .thenReturn(new ProfileOutput(1L, "john_novo", "John", "Silva",
            "john@example.com", LocalDateTime.now()));

    mockMvc.perform(put("/profile/me")
            .header("Authorization", "Bearer valid-token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(
                new UpdateProfileRequest("john_novo", "John", "Silva"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.username").value("john_novo"))
        .andExpect(jsonPath("$.lastName").value("Silva"));
  }

  @Test
  void shouldReturn409WhenUsernameDuplicated() throws Exception {
    when(jwtTokenProvider.validateToken("valid-token")).thenReturn(true);
    when(jwtTokenProvider.getUsernameFromToken("valid-token")).thenReturn("john_doe");
    when(jwtTokenProvider.getUserIdFromToken("valid-token")).thenReturn(1L);
    when(updateProfileUseCase.execute(eq(1L), any(UpdateProfileInput.class)))
        .thenThrow(new DuplicateUserException("Este nome de usuário já está em uso"));

    mockMvc.perform(put("/profile/me")
            .header("Authorization", "Bearer valid-token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(
                new UpdateProfileRequest("john_dup", "John", "Doe"))))
        .andExpect(status().isConflict());
  }

  @Test
  void shouldUpdatePassword() throws Exception {
    when(jwtTokenProvider.validateToken("valid-token")).thenReturn(true);
    when(jwtTokenProvider.getUsernameFromToken("valid-token")).thenReturn("john_doe");
    when(jwtTokenProvider.getUserIdFromToken("valid-token")).thenReturn(1L);
    doNothing().when(updatePasswordUseCase).execute(eq(1L), any(UpdatePasswordInput.class));

    mockMvc.perform(put("/profile/password")
            .header("Authorization", "Bearer valid-token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(
                new UpdatePasswordRequest("oldPass", "newPass123"))))
        .andExpect(status().isNoContent());
  }

  @Test
  void shouldReturn401WhenPasswordIncorrect() throws Exception {
    when(jwtTokenProvider.validateToken("valid-token")).thenReturn(true);
    when(jwtTokenProvider.getUsernameFromToken("valid-token")).thenReturn("john_doe");
    when(jwtTokenProvider.getUserIdFromToken("valid-token")).thenReturn(1L);
    doThrow(new InvalidCredentialsException("Senha atual incorreta"))
        .when(updatePasswordUseCase).execute(eq(1L), any(UpdatePasswordInput.class));

    mockMvc.perform(put("/profile/password")
            .header("Authorization", "Bearer valid-token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(
                new UpdatePasswordRequest("wrongPass", "newPass123"))))
        .andExpect(status().isUnauthorized());
  }
}
