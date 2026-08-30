package com.pucsp.alexandria.adapter.out.payment;

import com.pucsp.alexandria.adapter.out.payment.dto.PaymentApiCreateRequest;
import com.pucsp.alexandria.adapter.out.payment.dto.PaymentApiResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class RestPaymentApiClient implements PaymentApiClient {

  private final RestTemplate restTemplate;
  private final String baseUrl;

  public RestPaymentApiClient(
      RestTemplate restTemplate,
      @Value("${payment-api.url}") String baseUrl) {
    this.restTemplate = restTemplate;
    this.baseUrl = baseUrl;
  }

  @Override
  public PaymentApiResult createPayment(PaymentApiCreateRequest request, String bearerToken) {
    var headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setBearerAuth(bearerToken);

    var entity = new HttpEntity<>(request, headers);
    return restTemplate.exchange(
            baseUrl + "/api/v1/payments",
            HttpMethod.POST,
            entity,
            PaymentApiResult.class)
        .getBody();
  }

  @Override
  public PaymentApiResult capturePayment(String paymentId, String bearerToken) {
    var headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setBearerAuth(bearerToken);

    var entity = new HttpEntity<>(headers);
    return restTemplate.exchange(
            baseUrl + "/api/v1/payments/" + paymentId + "/process",
            HttpMethod.POST,
            entity,
            PaymentApiResult.class)
        .getBody();
  }
}
