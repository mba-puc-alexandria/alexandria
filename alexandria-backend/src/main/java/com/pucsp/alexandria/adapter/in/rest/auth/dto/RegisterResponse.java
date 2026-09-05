package com.pucsp.alexandria.adapter.in.rest.auth.dto;

import com.pucsp.alexandria.application.auth.dto.RegisterOutput;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resposta do registro de usuário.")
public record RegisterResponse(
    @Schema(description = "ID do usuário criado.", example = "1")
    Long id,

    @Schema(description = "Nome de usuário.", example = "john_doe")
    String username,

    @Schema(description = "E-mail.", example = "john@example.com", format = "email")
    String email
) {

  public static RegisterResponse from(RegisterOutput output) {
    return new RegisterResponse(output.id(), output.username(), output.email());
  }
}
