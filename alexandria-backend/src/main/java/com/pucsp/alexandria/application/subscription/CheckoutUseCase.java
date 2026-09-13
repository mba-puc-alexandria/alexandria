package com.pucsp.alexandria.application.subscription;

import com.pucsp.alexandria.adapter.out.payment.PaymentApiClient;
import com.pucsp.alexandria.adapter.out.payment.dto.PaymentApiCreateCustomerRequest;
import com.pucsp.alexandria.adapter.out.payment.dto.PaymentApiCreateRequest;
import com.pucsp.alexandria.adapter.out.payment.dto.PaymentApiCustomerResult;
import com.pucsp.alexandria.adapter.out.payment.dto.PaymentApiResult;
import com.pucsp.alexandria.application.subscription.dto.CheckoutInput;
import com.pucsp.alexandria.application.subscription.dto.CheckoutOutput;
import com.pucsp.alexandria.config.SubscriptionProperties;
import com.pucsp.alexandria.domain.subscription.Subscription;
import com.pucsp.alexandria.domain.subscription.SubscriptionRepository;
import com.pucsp.alexandria.domain.subscription.exception.PaymentMethodNotAllowedException;
import java.time.LocalDateTime;
import java.util.Locale;

/** Coordinates immediate PIX payments and saved-card enrolment during the trial. */
public class CheckoutUseCase {

  private final SubscriptionRepository subscriptionRepository;
  private final PaymentApiClient paymentApiClient;
  private final SubscriptionProperties properties;

  public CheckoutUseCase(SubscriptionRepository subscriptionRepository, PaymentApiClient paymentApiClient,
      SubscriptionProperties properties) {
    this.subscriptionRepository = subscriptionRepository;
    this.paymentApiClient = paymentApiClient;
    this.properties = properties;
  }

  public CheckoutOutput execute(CheckoutInput input, String bearerToken) {
    Subscription subscription = subscriptionRepository.findByUserId(input.userId())
        .orElseGet(() -> subscriptionRepository.save(
            Subscription.startTrial(input.userId(), properties.getTrialDays())));
    String method = normalizeMethod(input.paymentMethod());

    if (subscription.isTrialActive(LocalDateTime.now())) {
      if (!"CARD".equals(method)) {
        throw new PaymentMethodNotAllowedException(
            "Durante o período de teste, somente cartão de crédito é permitido.");
      }
      return saveCardForTrial(subscription, input, bearerToken);
    }

    return switch (method) {
      case "PIX" -> checkoutPix(subscription, input, bearerToken);
      case "CARD" -> checkoutCard(subscription, input, bearerToken);
      default -> throw new PaymentMethodNotAllowedException("Método de pagamento inválido.");
    };
  }

  private CheckoutOutput saveCardForTrial(Subscription subscription, CheckoutInput input,
      String bearerToken) {
    requireCardDetails(input);
    PaymentApiCustomerResult customer = paymentApiClient.createCustomer(
        new PaymentApiCreateCustomerRequest(input.payerEmail(), input.cardToken(), input.cardBrand()),
        bearerToken);
    subscription.savePaymentMethod(customer.customerId(), customer.cardId());
    subscriptionRepository.save(subscription);
    return new CheckoutOutput("CARD_SAVED", null, null, null, null, null,
        subscription.getStatus().name(),
        "Cartão salvo com segurança. A primeira cobrança ocorrerá ao fim do período de teste.");
  }

  private CheckoutOutput checkoutPix(Subscription subscription, CheckoutInput input, String bearerToken) {
    PaymentApiResult result = paymentApiClient.createPayment(new PaymentApiCreateRequest(
        buildReferenceId(subscription), properties.getPrice(), "PIX", input.payerEmail(),
        input.payerDocumentType(), input.payerDocumentNumber(), null, null, null, null, null,
        "Assinatura Alexandria Premium"), bearerToken);
    subscription.recordPendingPayment(result.mpPaymentId());
    subscriptionRepository.save(subscription);
    return new CheckoutOutput(result.status(), result.id(), result.mpPaymentId(), result.qrCode(),
        result.qrCodeBase64(), result.ticketUrl(), subscription.getStatus().name(),
        "Pagamento PIX criado. Aguardando confirmação.");
  }

  private CheckoutOutput checkoutCard(Subscription subscription, CheckoutInput input, String bearerToken) {
    requireCardDetails(input);
    PaymentApiResult result = paymentApiClient.createPayment(new PaymentApiCreateRequest(
        buildReferenceId(subscription), properties.getPrice(), "CREDIT_CARD", input.payerEmail(),
        input.payerDocumentType(), input.payerDocumentNumber(), input.cardToken(), null, null,
        input.installments() != null ? input.installments() : 1, input.cardBrand(),
        "Assinatura Alexandria Premium"), bearerToken);
    if ("COMPLETED".equalsIgnoreCase(result.status())) {
      subscription.activate(result.mpPaymentId(), LocalDateTime.now().plusDays(properties.getPeriodDays()));
      subscriptionRepository.save(subscription);
      return new CheckoutOutput(result.status(), result.id(), result.mpPaymentId(), null, null, null,
          subscription.getStatus().name(), "Pagamento aprovado e assinatura ativada.");
    }
    subscription.recordPendingPayment(result.mpPaymentId());
    subscriptionRepository.save(subscription);
    return new CheckoutOutput(result.status(), result.id(), result.mpPaymentId(), null, null, null,
        subscription.getStatus().name(), "Pagamento em processamento. A assinatura será ativada quando confirmado.");
  }

  private void requireCardDetails(CheckoutInput input) {
    if (input.cardToken() == null || input.cardToken().isBlank() || input.payerEmail() == null
        || input.payerEmail().isBlank()) {
      throw new PaymentMethodNotAllowedException("Cartão e e-mail do pagador são obrigatórios.");
    }
  }

  private String buildReferenceId(Subscription subscription) {
    return "subscription:" + subscription.getId().getValue();
  }

  private String normalizeMethod(String method) {
    return method == null ? "" : method.trim().toUpperCase(Locale.ROOT);
  }
}
