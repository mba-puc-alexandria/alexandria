package com.pucsp.alexandria.application.profile;

import com.pucsp.alexandria.application.profile.dto.UpdatePasswordInput;
import com.pucsp.alexandria.domain.user.User;
import com.pucsp.alexandria.domain.user.UserRepository;
import com.pucsp.alexandria.domain.user.exception.InvalidCredentialsException;
import com.pucsp.alexandria.domain.user.exception.InvalidUserException;
import com.pucsp.alexandria.domain.user.exception.UserNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

public class UpdatePasswordUseCase {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public UpdatePasswordUseCase(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  public void execute(Long userId, UpdatePasswordInput input) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFoundException(userId));

    if (!passwordEncoder.matches(input.currentPassword(), user.getPassword())) {
      throw new InvalidCredentialsException("Senha atual incorreta");
    }

    User.validatePassword(input.newPassword());
    String encodedNewPassword = passwordEncoder.encode(input.newPassword());
    User updated = user.updatePassword(encodedNewPassword);
    userRepository.save(updated);
  }
}
