package com.pucsp.alexandria.application.subscription.dto;

public record CheckoutOutput(
    String status,
    String paymentId,
    Long mpPaymentId,
    String qrCode,
    String qrCodeBase64,
    String ticketUrl,
    String subscriptionStatus,
    String message) {
}
