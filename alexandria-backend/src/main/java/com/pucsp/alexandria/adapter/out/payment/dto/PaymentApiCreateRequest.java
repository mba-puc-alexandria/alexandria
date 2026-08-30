package com.pucsp.alexandria.adapter.out.payment.dto;

import java.math.BigDecimal;

public record PaymentApiCreateRequest(
    String referenceId,
    BigDecimal amount,
    String paymentMethod,
    String payerEmail,
    String payerDocumentType,
    String payerDocumentNumber,
    String gatewayToken,
    Integer installments,
    String paymentMethodId,
    String description,
    boolean capture) {
}
