package com.pucsp.alexandria.adapter.in.rest.profile.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Requisição para alterar a senha.")
public record UpdatePasswordRequest(
    @Schema(description = "Senha atual.", example = "senha123", format = "password")
    String currentPassword,

    @Schema(description = "Nova senha (mínimo 8 caracteres).", example = "novaSenha123", format = "password")
    String newPassword
) {}
