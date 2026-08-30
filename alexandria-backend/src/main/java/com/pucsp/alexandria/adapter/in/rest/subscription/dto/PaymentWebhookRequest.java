package com.pucsp.alexandria.adapter.in.rest.subscription.dto;

public record PaymentWebhookRequest(
    String referenceId,
    String paymentId,
    String status,
    String paymentMethod,
    Long mpPaymentId,
    String occurredAt) {
}
