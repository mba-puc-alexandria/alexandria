package com.pucsp.alexandria.application.subscription.dto;

public record PaymentWebhookInput(
    String referenceId,
    String paymentId,
    String status,
    String paymentMethod,
    Long mpPaymentId,
    String occurredAt) {
}
