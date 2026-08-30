package com.pucsp.alexandria.application.subscription;

import com.pucsp.alexandria.adapter.out.payment.PaymentApiClient;
import com.pucsp.alexandria.adapter.out.payment.dto.PaymentApiCreateRequest;
import com.pucsp.alexandria.adapter.out.payment.dto.PaymentApiResult;
import com.pucsp.alexandria.application.subscription.dto.CheckoutInput;
import com.pucsp.alexandria.application.subscription.dto.CheckoutOutput;
import com.pucsp.alexandria.config.SubscriptionProperties;
import com.pucsp.alexandria.domain.subscription.Subscription;
import com.pucsp.alexandria.domain.subscription.SubscriptionRepository;
import com.pucsp.alexandria.domain.subscription.exception.PaymentMethodNotAllowedException;
import com.pucsp.alexandria.domain.subscription.exception.SubscriptionNotFoundException;
import java.time.LocalDateTime;
import java.util.Locale;

public class CheckoutUseCase {

  private final SubscriptionRepository subscriptionRepository;
  private final PaymentApiClient paymentApiClient;
  private final SubscriptionProperties properties;

  public CheckoutUseCase(
      SubscriptionRepository subscriptionRepository,
      PaymentApiClient paymentApiClient,
      SubscriptionProperties properties) {
    this.subscriptionRepository = subscriptionRepository;
    this.paymentApiClient = paymentApiClient;
    this.properties = properties;
  }

  public CheckoutOutput execute(CheckoutInput input, String bearerToken) {
    Subscription subscription = subscriptionRepository.findByUserId(input.userId())
        .orElseThrow(() -> new SubscriptionNotFoundException(input.userId()));

    String method = normalizeMethod(input.paymentMethod());

    if (subscription.isTrialActive(LocalDateTime.now())) {
      // Durante o trial: somente cartão (agendado, sem cobrança agora)
      if (!"CARD".equals(method)) {
        throw new PaymentMethodNotAllowedException(
            "Durante o período de teste, somente cartão de crédito é permitido.");
      }
      return checkoutCardDuringTrial(subscription, input, bearerToken);
    }

    // Após o trial: PIX ou cartão, ambos imediatos
    return switch (method) {
      case "PIX" -> checkoutPix(subscription, input, bearerToken);
      case "CARD" -> checkoutCardAfterTrial(subscription, input, bearerToken);
      default -> throw new PaymentMethodNotAllowedException("Método de pagamento inválido.");
    };
  }

  private CheckoutOutput checkoutCardDuringTrial(
      Subscription subscription, CheckoutInput input, String bearerToken) {
    String referenceId = buildReferenceId(subscription);

    PaymentApiResult result = paymentApiClient.createPayment(
        new PaymentApiCreateRequest(
            referenceId,
            properties.getPrice(),
            "CARD",
            input.payerEmail(),
            input.payerDocumentType(),
            input.payerDocumentNumber(),
            input.cardToken(),
            input.installments() != null ? input.installments() : 1,
            input.cardBrand(),
            "Assinatura Alexandria Premium",
            false),
        bearerToken);

    subscription.scheduleCardAfterTrial(result.id(), result.mpPaymentId());
    subscriptionRepository.save(subscription);

    return new CheckoutOutput(
        result.status(),
        result.id(),
        result.mpPaymentId(),
        null,
        null,
        null,
        subscription.getStatus().name(),
        "Assinatura confirmada. A cobrança será processada ao fim do período de teste.");
  }

  private CheckoutOutput checkoutPix(
      Subscription subscription, CheckoutInput input, String bearerToken) {
    String referenceId = buildReferenceId(subscription);

    PaymentApiResult result = paymentApiClient.createPayment(
        new PaymentApiCreateRequest(
            referenceId,
            properties.getPrice(),
            "PIX",
            input.payerEmail(),
            input.payerDocumentType(),
            input.payerDocumentNumber(),
            null,
            null,
            null,
            "Assinatura Alexandria Premium",
            true),
        bearerToken);

    subscription.recordPendingPayment(result.mpPaymentId());
    subscriptionRepository.save(subscription);

    return new CheckoutOutput(
        result.status(),
        result.id(),
        result.mpPaymentId(),
        result.qrCode(),
        result.qrCodeBase64(),
        result.ticketUrl(),
        subscription.getStatus().name(),
        "Pagamento PIX criado. Aguardando confirmação.");
  }

  private CheckoutOutput checkoutCardAfterTrial(
      Subscription subscription, CheckoutInput input, String bearerToken) {
    String referenceId = buildReferenceId(subscription);

    PaymentApiResult result = paymentApiClient.createPayment(
        new PaymentApiCreateRequest(
            referenceId,
            properties.getPrice(),
            "CARD",
            input.payerEmail(),
            input.payerDocumentType(),
            input.payerDocumentNumber(),
            input.cardToken(),
            input.installments() != null ? input.installments() : 1,
            input.cardBrand(),
            "Assinatura Alexandria Premium",
            true),
        bearerToken);

    if ("APPROVED".equalsIgnoreCase(result.status())) {
      subscription.activate(result.mpPaymentId(), LocalDateTime.now().plusDays(properties.getPeriodDays()));
      subscriptionRepository.save(subscription);
      return new CheckoutOutput(
          result.status(),
          result.id(),
          result.mpPaymentId(),
          null,
          null,
          null,
          subscription.getStatus().name(),
          "Pagamento aprovado e assinatura ativada.");
    }

    subscription.recordPendingPayment(result.mpPaymentId());
    subscriptionRepository.save(subscription);
    return new CheckoutOutput(
        result.status(),
        result.id(),
        result.mpPaymentId(),
        null,
        null,
        null,
        subscription.getStatus().name(),
        "Pagamento em processamento. A assinatura será ativada quando confirmado.");
  }

  private String buildReferenceId(Subscription subscription) {
    return "subscription:" + subscription.getId().getValue();
  }

  private String normalizeMethod(String method) {
    if (method == null) {
      return "";
    }
    return method.trim().toUpperCase(Locale.ROOT);
  }
}
