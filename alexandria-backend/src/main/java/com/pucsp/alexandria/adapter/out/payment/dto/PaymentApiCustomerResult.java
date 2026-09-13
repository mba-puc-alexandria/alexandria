package com.pucsp.alexandria.adapter.out.payment.dto;

public record PaymentApiCustomerResult(String customerId, String cardId, String paymentMethodId) {
}
