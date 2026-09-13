package com.pucsp.alexandria.adapter.in.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Requisição para adicionar um livro à biblioteca do usuário.")
public record AddUserBooksRequest(
    @Schema(description = "ID do livro.", example = "1")
    Long bookId,

    @Schema(description = "Status inicial da leitura.", example = "TOREAD",
        allowableValues = {"TOREAD", "READING", "DONE"})
    String status
) {}
