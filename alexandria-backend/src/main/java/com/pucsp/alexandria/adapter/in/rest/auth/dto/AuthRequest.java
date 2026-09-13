package com.pucsp.alexandria.adapter.in.rest.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Credenciais para autenticação (login).")
public record AuthRequest(
    @Schema(description = "Nome de usuário.", example = "john_doe")
    String username,

    @Schema(description = "Senha.", example = "senha123", format = "password")
    String password
) {}
