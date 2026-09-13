package com.pucsp.alexandria.adapter.in.rest.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados para criação de conta.")
public record RegisterRequest(
    @Schema(description = "Nome de usuário.", example = "john_doe")
    String username,

    @Schema(description = "Primeiro nome.", example = "John")
    String firstName,

    @Schema(description = "Sobrenome.", example = "Doe")
    String lastName,

    @Schema(description = "E-mail.", example = "john@example.com", format = "email")
    String email,

    @Schema(description = "Senha (mínimo 8 caracteres).", example = "senha123", format = "password")
    String password
) {}
