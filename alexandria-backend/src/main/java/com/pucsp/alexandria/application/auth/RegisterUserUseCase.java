package com.pucsp.alexandria.application.auth;

import com.pucsp.alexandria.application.auth.dto.RegisterInput;
import com.pucsp.alexandria.application.auth.dto.RegisterOutput;
import com.pucsp.alexandria.domain.user.User;
import com.pucsp.alexandria.domain.user.UserRepository;

public class RegisterUserUseCase {

  private final UserRepository userRepository;

  public RegisterUserUseCase(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public RegisterOutput execute(RegisterInput input) {
    User user = User.create(
        input.username(),
        input.firstName(),
        input.lastName(),
        input.email(),
        input.password()
    );
    User saved = userRepository.save(user);
    return new RegisterOutput(
        saved.getId().getValue(),
        saved.getUsername(),
        saved.getEmail().getValue()
    );
  }
}
