package com.pucsp.alexandria.domain.subscription;

import com.pucsp.alexandria.domain.subscription.exception.InvalidSubscriptionException;
import java.time.LocalDateTime;
import java.util.Objects;

public class Subscription {

  private final SubscriptionId id;
  private final Long userId;
  private SubscriptionStatus status;
  private LocalDateTime trialEndsAt;
  private LocalDateTime currentPeriodEndsAt;
  private Long mpPaymentId;
  private String mpCustomerId;
  private String mpCardId;
  private String lastPaymentStatus;
  private int failedAttempts;
  private LocalDateTime nextRetryAt;
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
      String mpCustomerId,
      String mpCardId,
      String lastPaymentStatus,
      int failedAttempts,
      LocalDateTime nextRetryAt,
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
    this.mpCustomerId = mpCustomerId;
    this.mpCardId = mpCardId;
    this.lastPaymentStatus = lastPaymentStatus;
    this.failedAttempts = failedAttempts;
    this.nextRetryAt = nextRetryAt;
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
        null,
        null,
        0,
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
      String mpCustomerId,
      String mpCardId,
      String lastPaymentStatus,
      int failedAttempts,
      LocalDateTime nextRetryAt,
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
        mpCustomerId,
        mpCardId,
        lastPaymentStatus,
        failedAttempts,
        nextRetryAt,
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

  public void savePaymentMethod(String mpCustomerId, String mpCardId) {
    ensureTrialActive();
    if (mpCustomerId == null || mpCustomerId.isBlank() || mpCardId == null || mpCardId.isBlank()) {
      throw new InvalidSubscriptionException("Mercado Pago customer and card ids are required");
    }
    this.mpCustomerId = mpCustomerId;
    this.mpCardId = mpCardId;
    this.paymentScheduled = true;
    this.updatedAt = LocalDateTime.now();
  }

  public void replacePaymentMethod(String mpCardId) {
    if (mpCustomerId == null || mpCustomerId.isBlank() || mpCardId == null || mpCardId.isBlank()) {
      throw new InvalidSubscriptionException("Mercado Pago customer and card ids are required");
    }
    this.mpCardId = mpCardId;
    this.paymentScheduled = status == SubscriptionStatus.TRIALING;
    this.updatedAt = LocalDateTime.now();
  }

  public void recordPendingPayment(Long mpPaymentId) {
    if (mpPaymentId == null || mpPaymentId <= 0) {
      throw new InvalidSubscriptionException("Valid Mercado Pago payment id is required");
    }
    this.mpPaymentId = mpPaymentId;
    this.lastPaymentStatus = "PENDING";
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
    this.lastPaymentStatus = "COMPLETED";
    this.failedAttempts = 0;
    this.nextRetryAt = null;
    this.scheduledPaymentId = null;
    this.paymentScheduled = false;
    this.updatedAt = LocalDateTime.now();
  }

  public void markPastDue(String paymentStatus, LocalDateTime retryAt) {
    this.status = SubscriptionStatus.PAST_DUE;
    this.lastPaymentStatus = paymentStatus;
    this.failedAttempts++;
    this.nextRetryAt = retryAt;
    this.updatedAt = LocalDateTime.now();
  }

  /** Records a terminal failed/refunded payment once, even if its webhook is redelivered. */
  public boolean recordPaymentFailure(Long mpPaymentId, String paymentStatus, LocalDateTime retryAt) {
    if (Objects.equals(this.mpPaymentId, mpPaymentId)
        && Objects.equals(this.lastPaymentStatus, paymentStatus)) {
      return false;
    }
    this.mpPaymentId = mpPaymentId;
    markPastDue(paymentStatus, retryAt);
    return true;
  }

  public void markPastDue() {
    markPastDue("FAILED", null);
  }

  public void markExpired() {
    this.status = SubscriptionStatus.EXPIRED;
    this.paymentScheduled = false;
    this.nextRetryAt = null;
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

  public String getMpCustomerId() { return mpCustomerId; }

  public String getMpCardId() { return mpCardId; }

  public String getLastPaymentStatus() { return lastPaymentStatus; }

  public int getFailedAttempts() { return failedAttempts; }

  public LocalDateTime getNextRetryAt() { return nextRetryAt; }

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
