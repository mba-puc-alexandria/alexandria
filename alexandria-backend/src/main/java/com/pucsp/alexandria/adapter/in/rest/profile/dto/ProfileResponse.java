package com.pucsp.alexandria.adapter.in.rest.profile.dto;

import com.pucsp.alexandria.application.profile.dto.ProfileOutput;
import java.time.LocalDateTime;

public record ProfileResponse(
    Long userId,
    String username,
    String firstName,
    String lastName,
    String email,
    LocalDateTime createdAt
) {
  public static ProfileResponse from(ProfileOutput output) {
    return new ProfileResponse(
        output.userId(), output.username(),
        output.firstName(), output.lastName(),
        output.email(), output.createdAt()
    );
  }
}
