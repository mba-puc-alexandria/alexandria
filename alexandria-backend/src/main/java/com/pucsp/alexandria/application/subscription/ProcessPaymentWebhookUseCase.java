package com.pucsp.alexandria.application.subscription;

import com.pucsp.alexandria.application.subscription.dto.PaymentWebhookInput;
import com.pucsp.alexandria.config.SubscriptionProperties;
import com.pucsp.alexandria.domain.subscription.Subscription;
import com.pucsp.alexandria.domain.subscription.SubscriptionRepository;
import com.pucsp.alexandria.domain.subscription.exception.SubscriptionNotFoundException;
import java.time.LocalDateTime;
import java.util.Objects;

/** Applies a payment-api callback. Delivery may be repeated and therefore must be idempotent. */
public class ProcessPaymentWebhookUseCase {

  private final SubscriptionRepository subscriptionRepository;
  private final SubscriptionProperties properties;

  public ProcessPaymentWebhookUseCase(SubscriptionRepository subscriptionRepository,
      SubscriptionProperties properties) {
    this.subscriptionRepository = subscriptionRepository;
    this.properties = properties;
  }

  public void execute(PaymentWebhookInput input) {
    if (input.mpPaymentId() == null || input.status() == null) {
      return;
    }
    Subscription subscription = findSubscription(input);
    String status = input.status().toUpperCase();
    if ("COMPLETED".equals(status)) {
      if (subscription.getStatus().name().equals("ACTIVE")
          && Objects.equals(subscription.getMpPaymentId(), input.mpPaymentId())
          && subscription.getCurrentPeriodEndsAt() != null
          && subscription.getCurrentPeriodEndsAt().isAfter(LocalDateTime.now())) {
        return;
      }
      subscription.activate(input.mpPaymentId(),
          LocalDateTime.now().plusDays(properties.getPeriodDays()));
      subscriptionRepository.save(subscription);
      return;
    }
    if ("FAILED".equals(status) || "REFUNDED".equals(status) || "CANCELLED".equals(status)) {
      LocalDateTime retryAt = LocalDateTime.now().plusHours(properties.getRetryBaseHours());
      if (subscription.recordPaymentFailure(input.mpPaymentId(), status, retryAt)) {
        if (subscription.getFailedAttempts() >= properties.getMaxFailedAttempts()) {
          subscription.markExpired();
        }
        subscriptionRepository.save(subscription);
      }
    }
  }

  private Subscription findSubscription(PaymentWebhookInput input) {
    return subscriptionRepository.findByMpPaymentId(input.mpPaymentId())
        .or(() -> subscriptionIdFromReference(input.referenceId())
            .flatMap(subscriptionRepository::findById))
        .orElseThrow(() -> new SubscriptionNotFoundException(input.mpPaymentId()));
  }

  private java.util.Optional<Long> subscriptionIdFromReference(String referenceId) {
    if (referenceId == null || !referenceId.startsWith("subscription:")) {
      return java.util.Optional.empty();
    }
    String[] segments = referenceId.split(":", 3);
    try {
      return segments.length >= 2 ? java.util.Optional.of(Long.parseLong(segments[1]))
          : java.util.Optional.empty();
    } catch (NumberFormatException ex) {
      return java.util.Optional.empty();
    }
  }
}
