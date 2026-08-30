package com.pucsp.alexandria.adapter.in.rest.subscription.dto;

public record CheckoutRequest(
    String paymentMethod,
    String cardToken,
    String cardBrand,
    Integer installments,
    String payerEmail,
    String payerDocumentType,
    String payerDocumentNumber) {
}
