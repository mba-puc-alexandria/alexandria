package com.pucsp.alexandria.domain.recommendation;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
public interface RecommendationBatchRepository {
  RecommendationBatch ensurePending(long userId, LocalDate date, String algorithmVersion);
  Optional<RecommendationBatch> find(long userId, LocalDate date);
  Instant now();
  boolean normalizeExpired(RecommendationBatch observed);
  Optional<RecommendationBatch> claim(long userId, LocalDate date, LocalDate today, GenerationOrigin origin, Duration lease);
  boolean heartbeat(RecommendationBatch claim, Duration lease);
  boolean publish(RecommendationBatch claim, List<RecommendationCandidate> candidates, String catalogFingerprint, String profileFingerprint);
  boolean fail(RecommendationBatch claim, String failureCode);
  boolean releaseNoProfile(RecommendationBatch claim, GenerationOrigin origin);
  void markDormant(long userId, LocalDate date);
  void reactivate(long userId, LocalDate date);
  List<RecommendationBatch> pendingAfter(LocalDate date, long afterBatchId, int limit);
  int retain(LocalDate today, int chunkSize);
}
