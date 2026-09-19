package com.pucsp.alexandria.advice;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Resposta de erro padronizada da API.")
public class ErrorResponse {

  @Schema(description = "Mensagem de erro.", example = "Book not found")
  private final String message;

  @Schema(description = "Código HTTP do erro.", example = "404")
  private final int status;

  @Schema(description = "Data/hora do erro.", example = "2026-09-05T10:00:00", format = "date-time")
  private final LocalDateTime timestamp;

  public ErrorResponse(String message, int status) {
    this.message = message;
    this.status = status;
    this.timestamp = LocalDateTime.now();
  }

  public String getMessage() {
    return message;
  }

  public int getStatus() {
    return status;
  }

  public LocalDateTime getTimestamp() {
    return timestamp;
  }
}
