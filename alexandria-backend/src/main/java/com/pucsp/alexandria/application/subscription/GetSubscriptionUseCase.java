package com.pucsp.alexandria.application.subscription;

import com.pucsp.alexandria.application.subscription.dto.SubscriptionOutput;
import com.pucsp.alexandria.config.SubscriptionProperties;
import com.pucsp.alexandria.domain.subscription.Subscription;
import com.pucsp.alexandria.domain.subscription.SubscriptionRepository;
import com.pucsp.alexandria.domain.subscription.exception.SubscriptionNotFoundException;

public class GetSubscriptionUseCase {

  private final SubscriptionRepository subscriptionRepository;
  private final SubscriptionProperties properties;

  public GetSubscriptionUseCase(
      SubscriptionRepository subscriptionRepository,
      SubscriptionProperties properties) {
    this.subscriptionRepository = subscriptionRepository;
    this.properties = properties;
  }

  public SubscriptionOutput execute(Long userId) {
    Subscription subscription = subscriptionRepository.findByUserId(userId)
        .orElseThrow(() -> new SubscriptionNotFoundException(userId));
    return new SubscriptionOutput(
        subscription.getStatus().name(),
        subscription.getTrialEndsAt(),
        subscription.getCurrentPeriodEndsAt(),
        properties.getPrice(),
        properties.getCurrency(),
        properties.getPeriodDays(),
        subscription.isPaymentScheduled());
  }
}
