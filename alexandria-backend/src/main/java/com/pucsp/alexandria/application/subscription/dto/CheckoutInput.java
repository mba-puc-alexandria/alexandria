package com.pucsp.alexandria.application.subscription.dto;

public record CheckoutInput(
    Long userId,
    String paymentMethod,
    String cardToken,
    String cardBrand,
    Integer installments,
    String payerEmail,
    String payerDocumentType,
    String payerDocumentNumber) {
}
