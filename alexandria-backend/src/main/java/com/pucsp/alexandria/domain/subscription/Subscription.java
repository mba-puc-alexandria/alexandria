package com.pucsp.alexandria.domain.subscription;

import com.pucsp.alexandria.domain.subscription.exception.InvalidSubscriptionException;
import java.time.LocalDateTime;

public class Subscription {

  private final SubscriptionId id;
  private final Long userId;
  private SubscriptionStatus status;
  private LocalDateTime trialEndsAt;
  private LocalDateTime currentPeriodEndsAt;
  private Long mpPaymentId;
  private String scheduledPaymentId;
  private boolean paymentScheduled;
  private final LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  private Subscription(
      SubscriptionId id,
      Long userId,
      SubscriptionStatus status,
      LocalDateTime trialEndsAt,
      LocalDateTime currentPeriodEndsAt,
      Long mpPaymentId,
      String scheduledPaymentId,
      boolean paymentScheduled,
      LocalDateTime createdAt,
      LocalDateTime updatedAt) {
    this.id = id;
    this.userId = userId;
    this.status = status;
    this.trialEndsAt = trialEndsAt;
    this.currentPeriodEndsAt = currentPeriodEndsAt;
    this.mpPaymentId = mpPaymentId;
    this.scheduledPaymentId = scheduledPaymentId;
    this.paymentScheduled = paymentScheduled;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public static Subscription startTrial(Long userId, int trialDays) {
    validateUserId(userId);
    LocalDateTime now = LocalDateTime.now();
    return new Subscription(
        null,
        userId,
        SubscriptionStatus.TRIALING,
        now.plusDays(trialDays),
        null,
        null,
        null,
        false,
        now,
        now);
  }

  public static Subscription restore(
      Long id,
      Long userId,
      SubscriptionStatus status,
      LocalDateTime trialEndsAt,
      LocalDateTime currentPeriodEndsAt,
      Long mpPaymentId,
      String scheduledPaymentId,
      boolean paymentScheduled,
      LocalDateTime createdAt,
      LocalDateTime updatedAt) {
    validateUserId(userId);
    SubscriptionId subscriptionId = SubscriptionId.from(id);
    return new Subscription(
        subscriptionId,
        userId,
        status,
        trialEndsAt,
        currentPeriodEndsAt,
        mpPaymentId,
        scheduledPaymentId,
        paymentScheduled,
        createdAt,
        updatedAt);
  }

  public boolean isTrialActive(LocalDateTime now) {
    return status == SubscriptionStatus.TRIALING
        && trialEndsAt != null
        && trialEndsAt.isAfter(now);
  }

  public boolean isAccessActive(LocalDateTime now) {
    if (isTrialActive(now)) {
      return true;
    }
    return currentPeriodEndsAt != null
        && currentPeriodEndsAt.isAfter(now)
        && (status == SubscriptionStatus.ACTIVE || status == SubscriptionStatus.CANCELED);
  }

  public void scheduleCardAfterTrial(String scheduledPaymentId, Long mpPaymentId) {
    ensureTrialActive();
    this.scheduledPaymentId = scheduledPaymentId;
    this.mpPaymentId = mpPaymentId;
    this.paymentScheduled = true;
    this.updatedAt = LocalDateTime.now();
  }

  public void activate(Long mpPaymentId, LocalDateTime periodEndsAt) {
    if (periodEndsAt == null || !periodEndsAt.isAfter(LocalDateTime.now())) {
      throw new InvalidSubscriptionException("Period end must be in the future");
    }
    this.status = SubscriptionStatus.ACTIVE;
    this.currentPeriodEndsAt = periodEndsAt;
    this.trialEndsAt = null;
    this.mpPaymentId = mpPaymentId;
    this.scheduledPaymentId = null;
    this.paymentScheduled = false;
    this.updatedAt = LocalDateTime.now();
  }

  public void markPastDue() {
    this.status = SubscriptionStatus.PAST_DUE;
    this.updatedAt = LocalDateTime.now();
  }

  public void markExpired() {
    this.status = SubscriptionStatus.EXPIRED;
    this.paymentScheduled = false;
    this.updatedAt = LocalDateTime.now();
  }

  public void cancel() {
    if (status == SubscriptionStatus.ACTIVE) {
      this.status = SubscriptionStatus.CANCELED;
    } else {
      this.status = SubscriptionStatus.CANCELED;
      this.trialEndsAt = null;
      this.currentPeriodEndsAt = null;
    }
    this.paymentScheduled = false;
    this.updatedAt = LocalDateTime.now();
  }

  private void ensureTrialActive() {
    if (!isTrialActive(LocalDateTime.now())) {
      throw new InvalidSubscriptionException("Subscription trial is not active");
    }
  }

  private static void validateUserId(Long userId) {
    if (userId == null || userId <= 0) {
      throw new InvalidSubscriptionException("Valid user id is required");
    }
  }

  public SubscriptionId getId() {
    return id;
  }

  public Long getUserId() {
    return userId;
  }

  public SubscriptionStatus getStatus() {
    return status;
  }

  public LocalDateTime getTrialEndsAt() {
    return trialEndsAt;
  }

  public LocalDateTime getCurrentPeriodEndsAt() {
    return currentPeriodEndsAt;
  }

  public Long getMpPaymentId() {
    return mpPaymentId;
  }

  public String getScheduledPaymentId() {
    return scheduledPaymentId;
  }

  public boolean isPaymentScheduled() {
    return paymentScheduled;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }
}
