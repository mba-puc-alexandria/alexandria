package com.pucsp.alexandria.application.auth;

import com.pucsp.alexandria.application.auth.dto.RegisterInput;
import com.pucsp.alexandria.application.auth.dto.RegisterOutput;
import com.pucsp.alexandria.application.subscription.StartTrialUseCase;
import com.pucsp.alexandria.domain.user.User;
import com.pucsp.alexandria.domain.user.UserRepository;
import com.pucsp.alexandria.domain.user.exception.DuplicateUserException;

public class RegisterUserUseCase {

  private final UserRepository userRepository;
  private final StartTrialUseCase startTrialUseCase;

  public RegisterUserUseCase(UserRepository userRepository, StartTrialUseCase startTrialUseCase) {
    this.userRepository = userRepository;
    this.startTrialUseCase = startTrialUseCase;
  }

  public RegisterOutput execute(RegisterInput input) {
    if (userRepository.existsByEmail(input.email())) {
      throw new DuplicateUserException("Já existe um usuário cadastrado com este email");
    }

    if (userRepository.existsByUsername(input.username())) {
      throw new DuplicateUserException("Já existe um usuário cadastrado com este nome de usuário");
    }

    User user = User.create(
        input.username(),
        input.firstName(),
        input.lastName(),
        input.email(),
        input.password()
    );
    User saved = userRepository.save(user);
    startTrialUseCase.execute(saved.getId().getValue());
    return new RegisterOutput(
        saved.getId().getValue(),
        saved.getUsername(),
        saved.getEmail().getValue()
    );
  }
}
