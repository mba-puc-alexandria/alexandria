package com.pucsp.alexandria.application.subscription.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SubscriptionOutput(
    String status,
    LocalDateTime trialEndsAt,
    LocalDateTime currentPeriodEndsAt,
    BigDecimal price,
    String currency,
    int periodDays,
    boolean paymentScheduled) {
}
