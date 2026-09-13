package com.pucsp.alexandria.application.subscription.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados internos do callback de status do pagamento.")
public record PaymentWebhookInput(
    @Schema(description = "Referência da assinatura.", example = "subscription:123")
    String referenceId,

    @Schema(description = "Identificador do pagamento no payment-api.", example = "uuid-pagamento")
    String paymentId,

    @Schema(
        description = "Status do pagamento.",
        example = "COMPLETED",
        allowableValues = {"COMPLETED", "REFUNDED", "FAILED", "PENDING"})
    String status,

    @Schema(description = "Método de pagamento.", example = "PIX", allowableValues = {"PIX", "CARD"})
    String paymentMethod,

    @Schema(description = "ID do pagamento no Mercado Pago.", example = "123456789")
    Long mpPaymentId,

    @Schema(description = "Data/hora do evento.", example = "2026-09-05T12:00:00Z", format = "date-time")
    String occurredAt) {
}
