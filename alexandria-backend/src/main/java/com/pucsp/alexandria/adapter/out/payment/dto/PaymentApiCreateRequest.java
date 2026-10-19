package com.pucsp.alexandria.adapter.out.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "Requisição enviada ao payment-api para criar um pagamento.")
public record PaymentApiCreateRequest(
    @Schema(description = "Referência da assinatura.", example = "subscription:123")
    String referenceId,

    @Schema(description = "Valor do pagamento.", example = "10.00")
    BigDecimal amount,

    @Schema(
        description = "Método de pagamento.",
        example = "PIX",
        allowableValues = {"PIX", "CARD", "CREDIT_CARD", "DEBIT_CARD"})
    String paymentMethod,

    @Schema(description = "E-mail do pagador.", example = "cliente@email.com", format = "email")
    String payerEmail,

    @Schema(description = "Tipo de documento do pagador.", example = "CPF")
    String payerDocumentType,

    @Schema(description = "Número do documento do pagador.", example = "19119119100")
    String payerDocumentNumber,

    @Schema(description = "CardToken do MercadoPago.js.", example = "token-abc123")
    String gatewayToken,

    @Schema(description = "ID do cartão salvo no Mercado Pago para cobrança recorrente.")
    String cardId,

    @Schema(description = "ID do Customer no Mercado Pago para cobrança recorrente.")
    String customerId,

    @Schema(description = "Número de parcelas.", example = "1", minimum = "1")
    Integer installments,

    @Schema(description = "Bandeira do cartão.", example = "master")
    String paymentMethodId,

    @Schema(description = "Descrição do pagamento.", example = "Assinatura Alexandria Premium")
    String description) {
}
