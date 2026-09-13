package com.pucsp.alexandria.adapter.out.payment;

import com.pucsp.alexandria.adapter.out.payment.dto.PaymentApiCreateRequest;
import com.pucsp.alexandria.adapter.out.payment.dto.PaymentApiResult;
import com.pucsp.alexandria.adapter.out.payment.dto.PaymentApiAddCardRequest;
import com.pucsp.alexandria.adapter.out.payment.dto.PaymentApiAddCardResult;
import com.pucsp.alexandria.adapter.out.payment.dto.PaymentApiCreateCustomerRequest;
import com.pucsp.alexandria.adapter.out.payment.dto.PaymentApiCustomerResult;

public interface PaymentApiClient {

  PaymentApiResult createPayment(PaymentApiCreateRequest request, String bearerToken);

  PaymentApiResult capturePayment(String paymentId, String bearerToken);

  PaymentApiCustomerResult createCustomer(
      PaymentApiCreateCustomerRequest request, String bearerToken);

  PaymentApiAddCardResult addCard(
      String customerId, PaymentApiAddCardRequest request, String bearerToken);
}
