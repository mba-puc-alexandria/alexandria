package com.pucsp.alexandria.adapter.in.rest.subscription;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.pucsp.alexandria.adapter.in.rest.subscription.dto.PaymentWebhookRequest;
import com.pucsp.alexandria.application.subscription.ProcessPaymentWebhookUseCase;
import com.pucsp.alexandria.config.SubscriptionProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class SubscriptionControllerWebhookTest {

  private ProcessPaymentWebhookUseCase processPaymentWebhookUseCase;
  private SubscriptionController controller;

  @BeforeEach
  void setUp() {
    processPaymentWebhookUseCase = mock(ProcessPaymentWebhookUseCase.class);
    SubscriptionProperties properties = new SubscriptionProperties();
    properties.setCallbackSecret("callback-secret");
    controller = new SubscriptionController(null, null, processPaymentWebhookUseCase, null, properties,
        null);
  }

  @Test
  void acceptsCallbackWithValidSecret() {
    var response = controller.paymentWebhook(request(), "callback-secret");

    assertEquals(HttpStatus.OK, response.getStatusCode());
    verify(processPaymentWebhookUseCase).execute(any());
  }

  @Test
  void rejectsCallbackWithInvalidSecret() {
    var response = controller.paymentWebhook(request(), "wrong-secret");

    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    verifyNoInteractions(processPaymentWebhookUseCase);
  }

  private PaymentWebhookRequest request() {
    return new PaymentWebhookRequest("subscription:123", "payment-123", "COMPLETED", "CARD",
        123L, "2026-09-19T16:00:00");
  }
}
