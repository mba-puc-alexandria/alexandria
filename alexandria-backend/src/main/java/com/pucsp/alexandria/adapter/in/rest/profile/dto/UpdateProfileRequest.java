package com.pucsp.alexandria.adapter.in.rest.profile.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Requisição para atualizar os dados do perfil.")
public record UpdateProfileRequest(
    @Schema(description = "Nome de usuário.", example = "john_doe")
    String username,

    @Schema(description = "Primeiro nome.", example = "John")
    String firstName,

    @Schema(description = "Sobrenome.", example = "Doe")
    String lastName
) {}
