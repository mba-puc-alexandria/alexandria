package com.pucsp.alexandria.application.subscription;

import com.pucsp.alexandria.config.SubscriptionProperties;
import com.pucsp.alexandria.domain.subscription.Subscription;
import com.pucsp.alexandria.domain.subscription.SubscriptionRepository;

public class StartTrialUseCase {

  private final SubscriptionRepository subscriptionRepository;
  private final SubscriptionProperties properties;

  public StartTrialUseCase(
      SubscriptionRepository subscriptionRepository,
      SubscriptionProperties properties) {
    this.subscriptionRepository = subscriptionRepository;
    this.properties = properties;
  }

  public Subscription execute(Long userId) {
    Subscription trial = Subscription.startTrial(userId, properties.getTrialDays());
    return subscriptionRepository.save(trial);
  }
}
