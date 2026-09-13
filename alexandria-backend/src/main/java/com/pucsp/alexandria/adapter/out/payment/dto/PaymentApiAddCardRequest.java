package com.pucsp.alexandria.adapter.out.payment.dto;

public record PaymentApiAddCardRequest(String cardToken, String paymentMethodId) {
}
