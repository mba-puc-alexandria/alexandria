package com.pucsp.alexandria.application.subscription.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resultado do checkout da assinatura (pagamento criado).")
public record CheckoutOutput(
    @Schema(
        description = "Status do pagamento no gateway.",
        example = "PENDING",
        allowableValues = {"PENDING", "APPROVED", "REJECTED", "IN_PROCESS"})
    String status,

    @Schema(
        description = "Identificador do pagamento no payment-api.",
        example = "660e8400-e29b-41d4-a716-446655440002")
    String paymentId,

    @Schema(
        description = "ID do pagamento no Mercado Pago.",
        example = "123456789")
    Long mpPaymentId,

    @Schema(
        description = "Código PIX copia-e-cola (quando método PIX).",
        example = "00020126580014BR.GOV.BCB.PIX...")
    String qrCode,

    @Schema(
        description = "QR Code em base64 para renderização da imagem (quando método PIX).",
        example = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==")
    String qrCodeBase64,

    @Schema(
        description = "URL do ticket/QR Code no Mercado Pago (quando método PIX).",
        example = "https://www.mercadopago.com.br/payments/123456789/ticket")
    String ticketUrl,

    @Schema(
        description = "Status da assinatura após o checkout.",
        example = "TRIALING",
        allowableValues = {"TRIALING", "ACTIVE", "PAST_DUE", "EXPIRED", "CANCELED"})
    String subscriptionStatus,

    @Schema(
        description = "Mensagem explicativa sobre o resultado.",
        example = "Pagamento PIX criado. Aguardando confirmação.")
    String message) {
}
