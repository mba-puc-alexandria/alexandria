package com.pucsp.alexandria.adapter.in.rest.profile.dto;

import com.pucsp.alexandria.application.profile.dto.ProfileOutput;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Dados do perfil do usuário.")
public record ProfileResponse(
    @Schema(description = "ID do usuário.", example = "1")
    Long userId,

    @Schema(description = "Nome de usuário.", example = "john_doe")
    String username,

    @Schema(description = "Primeiro nome.", example = "John")
    String firstName,

    @Schema(description = "Sobrenome.", example = "Doe")
    String lastName,

    @Schema(description = "E-mail.", example = "john@example.com", format = "email")
    String email,

    @Schema(description = "Data de criação do perfil.", example = "2026-09-05T10:00:00", format = "date-time")
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
