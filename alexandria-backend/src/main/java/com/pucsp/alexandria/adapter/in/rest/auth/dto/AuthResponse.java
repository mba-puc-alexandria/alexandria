package com.pucsp.alexandria.adapter.in.rest.auth.dto;

import com.pucsp.alexandria.application.auth.dto.AuthOutput;

public record AuthResponse(
    String token,
    String type,
    Long userId,
    String username,
    String role
) {

  public static AuthResponse from(AuthOutput output) {
    return new AuthResponse(output.token(), output.type(), output.userId(), output.username(), output.role());
  }
}
