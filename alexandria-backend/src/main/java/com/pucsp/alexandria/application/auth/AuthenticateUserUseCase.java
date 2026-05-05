package com.pucsp.alexandria.application.auth;

import com.pucsp.alexandria.application.auth.dto.AuthInput;
import com.pucsp.alexandria.application.auth.dto.AuthOutput;
import com.pucsp.alexandria.domain.user.UserRepository;

public class AuthenticateUserUseCase {

  private final UserRepository userRepository;

  public AuthenticateUserUseCase(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public AuthOutput execute(AuthInput input) {
    var user = userRepository.findByUsername(input.username())
        .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));

    return AuthOutput.of(null, user.getId().getValue(), user.getUsername());
  }
}
