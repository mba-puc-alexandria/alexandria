package com.pucsp.alexandria.adapter.out.payment;

import com.pucsp.alexandria.adapter.out.payment.dto.PaymentApiCreateRequest;
import com.pucsp.alexandria.adapter.out.payment.dto.PaymentApiResult;

public interface PaymentApiClient {

  PaymentApiResult createPayment(PaymentApiCreateRequest request, String bearerToken);

  PaymentApiResult capturePayment(String paymentId, String bearerToken);
}
