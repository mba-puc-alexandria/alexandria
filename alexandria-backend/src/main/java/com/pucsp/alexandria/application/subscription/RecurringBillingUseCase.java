package com.pucsp.alexandria.application.subscription;

import com.pucsp.alexandria.adapter.out.payment.PaymentApiClient;
import com.pucsp.alexandria.adapter.out.payment.dto.PaymentApiCreateRequest;
import com.pucsp.alexandria.adapter.out.payment.dto.PaymentApiResult;
import com.pucsp.alexandria.config.SubscriptionProperties;
import com.pucsp.alexandria.config.jwt.JwtTokenProvider;
import com.pucsp.alexandria.domain.subscription.Subscription;
import com.pucsp.alexandria.domain.subscription.SubscriptionRepository;
import java.time.LocalDateTime;

/** Charges the Mercado Pago card token stored for a subscription at the end of each cycle. */
public class RecurringBillingUseCase {

  private final SubscriptionRepository subscriptionRepository;
  private final PaymentApiClient paymentApiClient;
  private final SubscriptionProperties properties;
  private final JwtTokenProvider jwtTokenProvider;

  public RecurringBillingUseCase(SubscriptionRepository subscriptionRepository,
      PaymentApiClient paymentApiClient, SubscriptionProperties properties,
      JwtTokenProvider jwtTokenProvider) {
    this.subscriptionRepository = subscriptionRepository;
    this.paymentApiClient = paymentApiClient;
    this.properties = properties;
    this.jwtTokenProvider = jwtTokenProvider;
  }

  public void execute() {
    LocalDateTime now = LocalDateTime.now();
    subscriptionRepository.findTrialsEndingBefore(now).forEach(subscription -> {
      if (hasStoredCard(subscription)) {
        charge(subscription, cycleReference(subscription), scheduledToken(subscription));
      } else {
        subscription.markExpired();
        subscriptionRepository.save(subscription);
      }
    });
    subscriptionRepository.findActiveEndingBefore(now).forEach(subscription ->
        charge(subscription, cycleReference(subscription), scheduledToken(subscription)));
    subscriptionRepository.findPastDueRetryingBefore(now).forEach(subscription -> {
      if (subscription.getFailedAttempts() >= properties.getMaxFailedAttempts()) {
        subscription.markExpired();
        subscriptionRepository.save(subscription);
      } else if (hasStoredCard(subscription)) {
        charge(subscription, cycleReference(subscription), scheduledToken(subscription));
      }
    });
  }

  public void retryNow(Subscription subscription, String bearerToken) {
    if (hasStoredCard(subscription)) {
      charge(subscription, cycleReference(subscription), bearerToken);
    }
  }

  private void charge(Subscription subscription, String referenceId, String bearerToken) {
    PaymentApiResult result = paymentApiClient.createPayment(new PaymentApiCreateRequest(
        referenceId, properties.getPrice(), "CREDIT_CARD", null, null, null, null,
        subscription.getMpCardId(), subscription.getMpCustomerId(), 1, null,
        "Cobrança recorrente Alexandria Premium"), bearerToken);
    if ("COMPLETED".equalsIgnoreCase(result.status())) {
      subscription.activate(result.mpPaymentId(), LocalDateTime.now().plusDays(properties.getPeriodDays()));
    } else {
      subscription.recordPaymentFailure(result.mpPaymentId(), result.status(),
          nextRetryAt(subscription.getFailedAttempts() + 1));
      if (subscription.getFailedAttempts() >= properties.getMaxFailedAttempts()) {
        subscription.markExpired();
      }
    }
    subscriptionRepository.save(subscription);
  }

  private LocalDateTime nextRetryAt(int attempt) {
    long multiplier = 1L << Math.min(Math.max(attempt - 1, 0), 2);
    return LocalDateTime.now().plusHours((long) properties.getRetryBaseHours() * multiplier);
  }

  private boolean hasStoredCard(Subscription subscription) {
    return subscription.getMpCustomerId() != null && !subscription.getMpCustomerId().isBlank()
        && subscription.getMpCardId() != null && !subscription.getMpCardId().isBlank();
  }

  private String scheduledToken(Subscription subscription) {
    return jwtTokenProvider.generateToken(subscription.getUserId(),
        "subscription-" + subscription.getUserId(), "USER");
  }

  /** Stable across retries: one reference represents exactly one subscription cycle. */
  private String cycleReference(Subscription subscription) {
    LocalDateTime cycleEnd = subscription.getCurrentPeriodEndsAt() != null
        ? subscription.getCurrentPeriodEndsAt() : subscription.getTrialEndsAt();
    return "subscription:" + subscription.getId().getValue() + ":period:" + cycleEnd;
  }
}
