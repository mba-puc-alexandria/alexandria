package com.pucsp.alexandria.adapter.out.payment.dto;

public record PaymentApiCreateCustomerRequest(
    String email,
    String cardToken,
    String paymentMethodId) {
}
