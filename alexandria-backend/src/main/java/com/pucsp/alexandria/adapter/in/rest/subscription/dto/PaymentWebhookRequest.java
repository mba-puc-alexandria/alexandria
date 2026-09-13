package com.pucsp.alexandria.adapter.in.rest.subscription.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Payload do callback enviado pelo payment-api com o status do pagamento.")
public record PaymentWebhookRequest(
    @Schema(
        description = "Referência da assinatura no formato subscription:{id}.",
        example = "subscription:123")
    String referenceId,

    @Schema(
        description = "Identificador do pagamento no payment-api.",
        example = "660e8400-e29b-41d4-a716-446655440002")
    String paymentId,

    @Schema(
        description = "Status do pagamento.",
        example = "COMPLETED",
        allowableValues = {"COMPLETED", "REFUNDED", "FAILED", "PENDING"})
    String status,

    @Schema(
        description = "Método de pagamento utilizado.",
        example = "PIX",
        allowableValues = {"PIX", "CARD"})
    String paymentMethod,

    @Schema(
        description = "ID do pagamento no Mercado Pago.",
        example = "123456789")
    Long mpPaymentId,

    @Schema(
        description = "Data/hora em que o evento ocorreu (ISO 8601).",
        example = "2026-09-05T12:00:00Z",
        format = "date-time")
    String occurredAt) {
}
