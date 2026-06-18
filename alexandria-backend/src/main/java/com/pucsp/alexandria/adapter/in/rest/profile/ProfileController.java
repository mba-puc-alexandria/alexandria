package com.pucsp.alexandria.adapter.in.rest.profile;

import com.pucsp.alexandria.adapter.in.rest.profile.dto.ProfileResponse;
import com.pucsp.alexandria.adapter.in.rest.profile.dto.UpdatePasswordRequest;
import com.pucsp.alexandria.adapter.in.rest.profile.dto.UpdateProfileRequest;
import com.pucsp.alexandria.application.profile.GetProfileUseCase;
import com.pucsp.alexandria.application.profile.UpdatePasswordUseCase;
import com.pucsp.alexandria.application.profile.UpdateProfileUseCase;
import com.pucsp.alexandria.application.profile.dto.UpdatePasswordInput;
import com.pucsp.alexandria.application.profile.dto.UpdateProfileInput;
import com.pucsp.alexandria.domain.shared.valueobject.AuthenticatedUser;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/profile")
@SecurityRequirement(name = "bearerAuth")
public class ProfileController {

  private final GetProfileUseCase getProfileUseCase;
  private final UpdateProfileUseCase updateProfileUseCase;
  private final UpdatePasswordUseCase updatePasswordUseCase;

  public ProfileController(
      GetProfileUseCase getProfileUseCase,
      UpdateProfileUseCase updateProfileUseCase,
      UpdatePasswordUseCase updatePasswordUseCase
  ) {
    this.getProfileUseCase = getProfileUseCase;
    this.updateProfileUseCase = updateProfileUseCase;
    this.updatePasswordUseCase = updatePasswordUseCase;
  }

  @GetMapping("/me")
  public ResponseEntity<ProfileResponse> getProfile(Authentication authentication) {
    AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
    var output = getProfileUseCase.execute(user.id());
    return ResponseEntity.ok(ProfileResponse.from(output));
  }

  @PutMapping("/me")
  public ResponseEntity<ProfileResponse> updateProfile(
      Authentication authentication,
      @RequestBody UpdateProfileRequest request
  ) {
    AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
    var input = new UpdateProfileInput(request.username(), request.firstName(), request.lastName());
    var output = updateProfileUseCase.execute(user.id(), input);
    return ResponseEntity.ok(ProfileResponse.from(output));
  }

  @PutMapping("/password")
  public ResponseEntity<Void> updatePassword(
      Authentication authentication,
      @RequestBody UpdatePasswordRequest request
  ) {
    AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
    var input = new UpdatePasswordInput(request.currentPassword(), request.newPassword());
    updatePasswordUseCase.execute(user.id(), input);
    return ResponseEntity.noContent().build();
  }
}
