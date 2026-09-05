package com.pucsp.alexandria.adapter.in.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Requisição para criar/sincronizar livros a partir do Gutendex.")
public record CreateBookRequest(
    @Schema(description = "Página do Gutendex a ser sincronizada.", example = "1", minimum = "1")
    int page
) {}
