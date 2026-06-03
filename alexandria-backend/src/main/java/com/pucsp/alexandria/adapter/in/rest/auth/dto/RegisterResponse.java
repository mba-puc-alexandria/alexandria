package com.pucsp.alexandria.adapter.in.rest.auth.dto;

import com.pucsp.alexandria.application.auth.dto.RegisterOutput;

public record RegisterResponse(
    Long id,
    String username,
    String email
) {

  public static RegisterResponse from(RegisterOutput output) {
    return new RegisterResponse(output.id(), output.username(), output.email());
  }
}
