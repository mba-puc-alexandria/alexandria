package com.pucsp.alexandria.adapter.in.rest.auth.dto;

import com.pucsp.alexandria.application.auth.dto.AuthOutput;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resposta de autenticação (login/login Google).")
public record AuthResponse(
    @Schema(description = "Token JWT de acesso.", example = "eyJhbGciOiJIUzI1NiJ9...")
    String token,

    @Schema(description = "Tipo do token.", example = "Bearer")
    String type,

    @Schema(description = "ID do usuário.", example = "1")
    Long userId,

    @Schema(description = "Nome de usuário.", example = "john_doe")
    String username,

    @Schema(description = "Papel do usuário.", example = "USER", allowableValues = {"USER", "ADMIN"})
    String role
) {

  public static AuthResponse from(AuthOutput output) {
    return new AuthResponse(output.token(), output.type(), output.userId(), output.username(), output.role());
  }
}
