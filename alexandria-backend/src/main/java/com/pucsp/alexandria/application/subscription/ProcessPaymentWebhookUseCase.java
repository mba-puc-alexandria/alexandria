package com.pucsp.alexandria.application.subscription;

import com.pucsp.alexandria.application.subscription.dto.PaymentWebhookInput;
import com.pucsp.alexandria.config.SubscriptionProperties;
import com.pucsp.alexandria.domain.subscription.Subscription;
import com.pucsp.alexandria.domain.subscription.SubscriptionRepository;
import com.pucsp.alexandria.domain.subscription.exception.SubscriptionNotFoundException;
import java.time.LocalDateTime;

public class ProcessPaymentWebhookUseCase {

  private final SubscriptionRepository subscriptionRepository;
  private final SubscriptionProperties properties;

  public ProcessPaymentWebhookUseCase(
      SubscriptionRepository subscriptionRepository,
      SubscriptionProperties properties) {
    this.subscriptionRepository = subscriptionRepository;
    this.properties = properties;
  }

  /**
   * Processa o callback do payment-api de forma idempotente.
   *
   * <p>Só ativa a assinatura para pagamentos confirmados (COMPLETED). Para REFUNDED
   * ou outros status, por enquanto apenas ignora (renovação/estorno são fases futuras).
   */
  public void execute(PaymentWebhookInput input) {
    if (input.mpPaymentId() == null) {
      return;
    }

    if (!"COMPLETED".equalsIgnoreCase(input.status())) {
      return;
    }

    Subscription subscription = subscriptionRepository.findByMpPaymentId(input.mpPaymentId())
        .orElseThrow(() -> new SubscriptionNotFoundException(input.mpPaymentId()));

    // Idempotência: se já está ativo com período futuro, não reativa.
    if (subscription.isAccessActive(LocalDateTime.now())
        && subscription.getStatus().name().equals("ACTIVE")) {
      return;
    }

    subscription.activate(
        input.mpPaymentId(),
        LocalDateTime.now().plusDays(properties.getPeriodDays()));
    subscriptionRepository.save(subscription);
  }
}
