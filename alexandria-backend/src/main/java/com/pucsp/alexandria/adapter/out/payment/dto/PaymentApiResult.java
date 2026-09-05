package com.pucsp.alexandria.adapter.out.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resposta do payment-api após criar/processar um pagamento.")
public record PaymentApiResult(
    @Schema(description = "Identificador do pagamento no payment-api.", example = "uuid-pagamento")
    String id,

    @Schema(description = "ID do pagamento no Mercado Pago.", example = "123456789")
    Long mpPaymentId,

    @Schema(
        description = "Status do pagamento.",
        example = "PENDING",
        allowableValues = {"PENDING", "APPROVED", "REJECTED", "IN_PROCESS"})
    String status,

    @Schema(description = "Código PIX copia-e-cola.", example = "00020126580014BR.GOV.BCB.PIX...")
    String qrCode,

    @Schema(description = "QR Code em base64.", example = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==")
    String qrCodeBase64,

    @Schema(description = "URL do ticket/QR Code no Mercado Pago.", example = "https://www.mercadopago.com.br/payments/123456789/ticket")
    String ticketUrl) {
}
