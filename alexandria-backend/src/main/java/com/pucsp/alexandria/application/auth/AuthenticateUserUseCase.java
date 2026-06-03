package com.pucsp.alexandria.application.auth;

import com.pucsp.alexandria.application.auth.dto.AuthInput;
import com.pucsp.alexandria.application.auth.dto.AuthOutput;
import com.pucsp.alexandria.domain.user.User;
import com.pucsp.alexandria.domain.user.UserRepository;
import com.pucsp.alexandria.domain.user.exception.InvalidCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

public class AuthenticateUserUseCase {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public AuthenticateUserUseCase(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  public AuthOutput execute(AuthInput input) {
    User user = userRepository.findByUsername(input.username())
        .orElseThrow(() -> new InvalidCredentialsException("Invalid username or password"));

    if (!passwordEncoder.matches(input.password(), user.getPassword())) {
      throw new InvalidCredentialsException("Invalid username or password");
    }

    return AuthOutput.of(null, user.getId().getValue(), user.getUsername());
  }
}
