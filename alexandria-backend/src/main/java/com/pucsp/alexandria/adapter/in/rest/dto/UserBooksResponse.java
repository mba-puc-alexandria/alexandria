package com.pucsp.alexandria.adapter.in.rest.dto;

import com.pucsp.alexandria.application.userbooks.dto.UserBooksOutput;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Livro da biblioteca do usuário.")
public record UserBooksResponse(
    @Schema(description = "ID do registro usuário-livro.", example = "1")
    Long id,

    @Schema(description = "Resumo do livro.")
    BookSummaryResponse book,

    @Schema(description = "Status da leitura.", example = "READING",
        allowableValues = {"TOREAD", "READING", "DONE"})
    String status,

    @Schema(description = "Progresso da leitura (0-100).", example = "42", minimum = "0", maximum = "100")
    Integer progress,

    @Schema(description = "Avaliação do livro (1-5).", example = "4", minimum = "1", maximum = "5")
    Integer rating
) {

  public static UserBooksResponse from(UserBooksOutput output) {
    return new UserBooksResponse(
        output.id(),
        BookSummaryResponse.from(output.book()),
        output.status(),
        output.progress(),
        output.rating()
    );
  }
}
