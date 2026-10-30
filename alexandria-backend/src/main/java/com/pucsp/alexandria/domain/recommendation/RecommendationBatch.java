package com.pucsp.alexandria.domain.recommendation;
import java.time.Instant;
import java.time.LocalDate;
public record RecommendationBatch(long id, long userId, LocalDate referenceDate,
    RecommendationBatchStatus status, String algorithmVersion, int selectedCount,
    Instant generatedAt, String leaseToken, Instant leaseUntil, int attemptCount,
    int automaticAttemptCount, Instant failedAt, Instant nextAttemptAt, Instant createdAt,
    Instant operationalPendingSince, String failureCode) {}
