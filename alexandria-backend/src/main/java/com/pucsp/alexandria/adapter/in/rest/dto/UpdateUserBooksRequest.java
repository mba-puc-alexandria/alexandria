package com.pucsp.alexandria.adapter.in.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Requisição para atualizar status/progresso/avaliação de um livro da biblioteca.")
public record UpdateUserBooksRequest(
    @Schema(description = "Status da leitura.", example = "READING",
        allowableValues = {"TOREAD", "READING", "DONE"})
    String status,

    @Schema(description = "Progresso da leitura em porcentagem (0-100).", example = "42", minimum = "0", maximum = "100")
    Integer progress,

    @Schema(description = "Avaliação do livro (1-5).", example = "4", minimum = "1", maximum = "5")
    Integer rating
) {}
