package com.pucsp.alexandria.adapter.out.persistence.entity;

import com.pucsp.alexandria.domain.subscription.SubscriptionStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "subscriptions")
public class SubscriptionEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id", unique = true, nullable = false)
  private Long userId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private SubscriptionStatus status;

  @Column(name = "trial_ends_at")
  private LocalDateTime trialEndsAt;

  @Column(name = "current_period_ends_at")
  private LocalDateTime currentPeriodEndsAt;

  @Column(name = "mp_payment_id")
  private Long mpPaymentId;

  @Column(name = "mp_customer_id")
  private String mpCustomerId;

  @Column(name = "mp_card_id")
  private String mpCardId;

  @Column(name = "last_payment_status")
  private String lastPaymentStatus;

  @Column(name = "failed_attempts", nullable = false)
  private int failedAttempts;

  @Column(name = "next_retry_at")
  private LocalDateTime nextRetryAt;

  @Column(name = "scheduled_payment_id")
  private String scheduledPaymentId;

  @Column(name = "payment_scheduled", nullable = false)
  private boolean paymentScheduled;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  public SubscriptionEntity() {
  }

  public SubscriptionEntity(
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

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public SubscriptionStatus getStatus() {
    return status;
  }

  public void setStatus(SubscriptionStatus status) {
    this.status = status;
  }

  public LocalDateTime getTrialEndsAt() {
    return trialEndsAt;
  }

  public void setTrialEndsAt(LocalDateTime trialEndsAt) {
    this.trialEndsAt = trialEndsAt;
  }

  public LocalDateTime getCurrentPeriodEndsAt() {
    return currentPeriodEndsAt;
  }

  public void setCurrentPeriodEndsAt(LocalDateTime currentPeriodEndsAt) {
    this.currentPeriodEndsAt = currentPeriodEndsAt;
  }

  public Long getMpPaymentId() {
    return mpPaymentId;
  }

  public void setMpPaymentId(Long mpPaymentId) {
    this.mpPaymentId = mpPaymentId;
  }

  public String getMpCustomerId() { return mpCustomerId; }
  public void setMpCustomerId(String mpCustomerId) { this.mpCustomerId = mpCustomerId; }
  public String getMpCardId() { return mpCardId; }
  public void setMpCardId(String mpCardId) { this.mpCardId = mpCardId; }
  public String getLastPaymentStatus() { return lastPaymentStatus; }
  public void setLastPaymentStatus(String lastPaymentStatus) { this.lastPaymentStatus = lastPaymentStatus; }
  public int getFailedAttempts() { return failedAttempts; }
  public void setFailedAttempts(int failedAttempts) { this.failedAttempts = failedAttempts; }
  public LocalDateTime getNextRetryAt() { return nextRetryAt; }
  public void setNextRetryAt(LocalDateTime nextRetryAt) { this.nextRetryAt = nextRetryAt; }

  public String getScheduledPaymentId() {
    return scheduledPaymentId;
  }

  public void setScheduledPaymentId(String scheduledPaymentId) {
    this.scheduledPaymentId = scheduledPaymentId;
  }

  public boolean isPaymentScheduled() {
    return paymentScheduled;
  }

  public void setPaymentScheduled(boolean paymentScheduled) {
    this.paymentScheduled = paymentScheduled;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }
}
