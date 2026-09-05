package com.pucsp.alexandria.application.subscription.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados internos do checkout da assinatura.")
public record CheckoutInput(
    @Schema(description = "ID do usuário autenticado.", example = "1")
    Long userId,

    @Schema(
        description = "Método de pagamento.",
        example = "PIX",
        allowableValues = {"PIX", "CARD"})
    String paymentMethod,

    @Schema(description = "CardToken do MercadoPago.js.", example = "token-abc123")
    String cardToken,

    @Schema(description = "Bandeira do cartão.", example = "master")
    String cardBrand,

    @Schema(description = "Número de parcelas.", example = "1", minimum = "1")
    Integer installments,

    @Schema(description = "E-mail do pagador.", example = "cliente@email.com", format = "email")
    String payerEmail,

    @Schema(description = "Tipo de documento do pagador.", example = "CPF")
    String payerDocumentType,

    @Schema(description = "Número do documento do pagador.", example = "19119119100")
    String payerDocumentNumber) {
}
