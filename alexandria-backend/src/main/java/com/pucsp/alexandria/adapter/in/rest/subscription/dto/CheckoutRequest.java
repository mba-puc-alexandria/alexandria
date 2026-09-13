package com.pucsp.alexandria.adapter.in.rest.subscription.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Requisição para iniciar o checkout da assinatura Alexandria Premium.")
public record CheckoutRequest(
    @Schema(
        description = "Método de pagamento. Durante o trial, somente CARD é permitido. Após o trial, PIX ou CARD.",
        example = "PIX",
        allowableValues = {"PIX", "CARD"})
    String paymentMethod,

    @Schema(
        description = "CardToken gerado pelo MercadoPago.js CardForm. Obrigatório quando o método é CARD.",
        example = "bf9edf6ffae3ab203bd1c7e4d5b9e9f1")
    String cardToken,

    @Schema(
        description = "Bandeira do cartão (ex.: visa, master, elo, amex). Usada quando o método é CARD.",
        example = "master")
    String cardBrand,

    @Schema(
        description = "Número de parcelas. Default 1.",
        example = "1",
        minimum = "1")
    Integer installments,

    @Schema(
        description = "E-mail do pagador (obrigatório para o Mercado Pago).",
        example = "cliente@email.com",
        format = "email")
    String payerEmail,

    @Schema(
        description = "Tipo de documento do pagador (ex.: CPF, CNPJ).",
        example = "CPF")
    String payerDocumentType,

    @Schema(
        description = "Número do documento do pagador.",
        example = "19119119100")
    String payerDocumentNumber) {
}
