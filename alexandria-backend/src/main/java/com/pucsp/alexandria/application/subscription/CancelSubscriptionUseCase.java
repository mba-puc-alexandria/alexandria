package com.pucsp.alexandria.application.subscription;

import com.pucsp.alexandria.domain.subscription.Subscription;
import com.pucsp.alexandria.domain.subscription.SubscriptionRepository;
import com.pucsp.alexandria.domain.subscription.exception.SubscriptionNotFoundException;

public class CancelSubscriptionUseCase {

  private final SubscriptionRepository subscriptionRepository;

  public CancelSubscriptionUseCase(SubscriptionRepository subscriptionRepository) {
    this.subscriptionRepository = subscriptionRepository;
  }

  public void execute(Long userId) {
    Subscription subscription = subscriptionRepository.findByUserId(userId)
        .orElseThrow(() -> new SubscriptionNotFoundException(userId));
    subscription.cancel();
    subscriptionRepository.save(subscription);
  }
}
