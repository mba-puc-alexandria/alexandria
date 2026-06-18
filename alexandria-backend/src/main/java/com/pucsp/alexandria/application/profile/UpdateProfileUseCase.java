package com.pucsp.alexandria.application.profile;

import com.pucsp.alexandria.application.profile.dto.ProfileOutput;
import com.pucsp.alexandria.application.profile.dto.UpdateProfileInput;
import com.pucsp.alexandria.domain.user.User;
import com.pucsp.alexandria.domain.user.UserRepository;
import com.pucsp.alexandria.domain.user.exception.DuplicateUserException;
import com.pucsp.alexandria.domain.user.exception.UserNotFoundException;

public class UpdateProfileUseCase {

  private final UserRepository userRepository;

  public UpdateProfileUseCase(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public ProfileOutput execute(Long userId, UpdateProfileInput input) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFoundException(userId));

    if (input.username() != null
        && !input.username().equals(user.getUsername())
        && userRepository.existsByUsername(input.username())) {
      throw new DuplicateUserException("Este nome de usuário já está em uso");
    }

    User updated = user.updateProfile(input.username(), input.firstName(), input.lastName());
    User saved = userRepository.save(updated);

    return new ProfileOutput(
        saved.getId().getValue(),
        saved.getUsername(),
        saved.getFirstName(),
        saved.getLastName(),
        saved.getEmail().getValue(),
        saved.getCreatedAt()
    );
  }
}
