package com.pucsp.alexandria.adapter.in.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Requisição para atualizar um livro.")
public record UpdateBookRequest(
    @Schema(description = "Novo título do livro.", example = "Dom Casmurro")
    String title
) {}
