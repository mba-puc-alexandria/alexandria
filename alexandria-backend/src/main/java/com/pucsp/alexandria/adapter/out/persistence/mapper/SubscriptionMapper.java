package com.pucsp.alexandria.adapter.out.persistence.mapper;

import com.pucsp.alexandria.adapter.out.persistence.entity.SubscriptionEntity;
import com.pucsp.alexandria.domain.subscription.Subscription;
import org.springframework.stereotype.Component;

@Component
public class SubscriptionMapper {

  public Subscription toDomain(SubscriptionEntity entity) {
    if (entity == null) {
      return null;
    }
    return Subscription.restore(
        entity.getId(),
        entity.getUserId(),
        entity.getStatus(),
        entity.getTrialEndsAt(),
        entity.getCurrentPeriodEndsAt(),
        entity.getMpPaymentId(),
        entity.getScheduledPaymentId(),
        entity.isPaymentScheduled(),
        entity.getCreatedAt(),
        entity.getUpdatedAt());
  }

  public SubscriptionEntity toPersistence(Subscription subscription) {
    if (subscription == null) {
      return null;
    }
    return new SubscriptionEntity(
        subscription.getId() != null ? subscription.getId().getValue() : null,
        subscription.getUserId(),
        subscription.getStatus(),
        subscription.getTrialEndsAt(),
        subscription.getCurrentPeriodEndsAt(),
        subscription.getMpPaymentId(),
        subscription.getScheduledPaymentId(),
        subscription.isPaymentScheduled(),
        subscription.getCreatedAt(),
        subscription.getUpdatedAt());
  }
}
