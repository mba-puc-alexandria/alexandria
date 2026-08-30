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
