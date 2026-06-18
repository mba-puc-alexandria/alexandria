package com.pucsp.alexandria.application.profile;

import com.pucsp.alexandria.application.profile.dto.ProfileOutput;
import com.pucsp.alexandria.domain.user.User;
import com.pucsp.alexandria.domain.user.UserRepository;
import com.pucsp.alexandria.domain.user.exception.UserNotFoundException;

public class GetProfileUseCase {

  private final UserRepository userRepository;

  public GetProfileUseCase(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public ProfileOutput execute(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFoundException(userId));

    return new ProfileOutput(
        user.getId().getValue(),
        user.getUsername(),
        user.getFirstName(),
        user.getLastName(),
        user.getEmail().getValue(),
        user.getCreatedAt()
    );
  }
}
