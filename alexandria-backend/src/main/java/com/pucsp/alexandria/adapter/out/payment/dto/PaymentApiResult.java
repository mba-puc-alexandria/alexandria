package com.pucsp.alexandria.adapter.out.payment.dto;

public record PaymentApiResult(
    String id,
    Long mpPaymentId,
    String status,
    String qrCode,
    String qrCodeBase64,
    String ticketUrl) {
}
