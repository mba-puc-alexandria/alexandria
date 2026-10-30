package com.pucsp.alexandria.adapter.out.persistence;

import com.pucsp.alexandria.adapter.out.persistence.mapper.RecommendationBatchMapper;
import com.pucsp.alexandria.domain.recommendation.*;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/** Short database transactions arbitrate ownership; ranking never runs under their locks. */
@Repository
public class RecommendationBatchRepositoryImpl implements RecommendationBatchRepository {
  private final JdbcTemplate jdbc;
  public RecommendationBatchRepositoryImpl(JdbcTemplate jdbc) { this.jdbc = jdbc; }

  @Override
  @Transactional
  public RecommendationBatch ensurePending(long userId, LocalDate date, String version) {
    jdbc.update("""
        INSERT INTO recommendation_batches
          (user_id,reference_date,status,algorithm_version,created_at,updated_at,operational_pending_since)
        VALUES (?,?,'PENDING',?,UTC_TIMESTAMP(6),UTC_TIMESTAMP(6),UTC_TIMESTAMP(6))
        ON DUPLICATE KEY UPDATE id=id
        """, userId, date, version);
    return find(userId, date).orElseThrow();
  }

  @Override
  public Optional<RecommendationBatch> find(long userId, LocalDate date) {
    return jdbc.query("SELECT * FROM recommendation_batches WHERE user_id=? AND reference_date=?",
        RecommendationBatchMapper::fromRow, userId, date).stream().findFirst();
  }

  @Override
  public Instant now() {
    return jdbc.queryForObject("SELECT UTC_TIMESTAMP(6) AS db_now",
        (rs, row) -> RecommendationBatchMapper.instant(rs, "db_now"));
  }

  @Override
  @Transactional
  public boolean normalizeExpired(RecommendationBatch observed) {
    if (observed.status() != RecommendationBatchStatus.BUILDING || observed.leaseUntil() == null) return false;
    return jdbc.update("""
        UPDATE recommendation_batches SET status='FAILED',failed_at=lease_until,
          next_attempt_at=CASE WHEN automatic_attempt_count>=3 THEN NULL
            ELSE TIMESTAMPADD(SECOND,CASE WHEN automatic_attempt_count>=2 THEN 300 ELSE 60 END,lease_until) END,
          lease_token=NULL,lease_until=NULL,failure_code='LEASE_EXPIRED',updated_at=UTC_TIMESTAMP(6)
        WHERE id=? AND status='BUILDING' AND lease_token=? AND lease_until=?
          AND lease_until<=UTC_TIMESTAMP(6)
        """, observed.id(), observed.leaseToken(), RecommendationBatchMapper.utc(observed.leaseUntil())) == 1;
  }

  @Override
  @Transactional
  public Optional<RecommendationBatch> claim(long userId, LocalDate date, LocalDate today,
      GenerationOrigin origin, Duration lease) {
    if (!date.equals(today)) return Optional.empty();
    long micros = positiveMicros(lease);
    var observed = find(userId, date);
    observed.ifPresent(this::normalizeExpired);
    String token = UUID.randomUUID().toString();
    int automatic = origin == GenerationOrigin.AUTO ? 1 : 0;
    int updated = jdbc.update("""
        UPDATE recommendation_batches SET status='BUILDING',lease_token=?,
          lease_until=TIMESTAMPADD(MICROSECOND,?,UTC_TIMESTAMP(6)),attempt_count=attempt_count+1,
          automatic_attempt_count=automatic_attempt_count+?,failed_at=NULL,next_attempt_at=NULL,
          failure_code=NULL,operational_pending_since=COALESCE(operational_pending_since,UTC_TIMESTAMP(6)),
          updated_at=UTC_TIMESTAMP(6)
        WHERE user_id=? AND reference_date=? AND (
          (?=1 AND automatic_attempt_count<3 AND (status='PENDING' OR (status='FAILED' AND next_attempt_at<=UTC_TIMESTAMP(6))))
          OR (?=0 AND (status='PENDING' OR (status='FAILED' AND TIMESTAMPADD(SECOND,60,failed_at)<=UTC_TIMESTAMP(6))))
        )
        """, token, micros, automatic, userId, date, automatic, automatic);
    return updated == 1 ? find(userId, date) : Optional.empty();
  }

  @Override
  @Transactional
  public boolean heartbeat(RecommendationBatch claim, Duration lease) {
    return jdbc.update("""
        UPDATE recommendation_batches SET lease_until=TIMESTAMPADD(MICROSECOND,?,UTC_TIMESTAMP(6)),updated_at=UTC_TIMESTAMP(6)
        WHERE id=? AND status='BUILDING' AND lease_token=? AND lease_until>UTC_TIMESTAMP(6)
        """, positiveMicros(lease), claim.id(), claim.leaseToken()) == 1;
  }

  @Override
  @Transactional
  public boolean publish(RecommendationBatch claim, List<RecommendationCandidate> candidates,
      String catalogFingerprint, String profileFingerprint) {
    if (candidates.size() > 30 || candidates.stream().map(RecommendationCandidate::bookId).distinct().count() != candidates.size()) {
      throw new IllegalArgumentException("Invalid daily selection");
    }
    var locked = jdbc.query("SELECT * FROM recommendation_batches WHERE id=? FOR UPDATE",
        RecommendationBatchMapper::fromRow, claim.id()).stream().findFirst();
    if (locked.isEmpty() || !owns(locked.get(), claim, now())) return false;
    int position = 0;
    for (var candidate : candidates) {
      var reason = candidate.reason();
      jdbc.update("""
          INSERT INTO recommendation_items(batch_id,position,book_id,reason_type,reason_value,
            reason_text,source_book_id,source_book_title,evidence_weight) VALUES (?,?,?,?,?,?,?,?,?)
          """, claim.id(), ++position, candidate.bookId(), reason.type(), reason.matchedValue(), reason.text(),
          reason.sourceBookId(), reason.sourceBookTitle(), reason.weight());
    }
    // Check lease again after the inserts: a long transaction must not publish an expired lease.
    int published = jdbc.update("""
        UPDATE recommendation_batches SET status='READY',selected_count=?,catalog_fingerprint=?,
          profile_fingerprint=?,generated_at=UTC_TIMESTAMP(6),lease_token=NULL,lease_until=NULL,
          operational_pending_since=NULL,failed_at=NULL,next_attempt_at=NULL,failure_code=NULL,updated_at=UTC_TIMESTAMP(6)
        WHERE id=? AND status='BUILDING' AND lease_token=? AND lease_until>UTC_TIMESTAMP(6)
        """, candidates.size(), catalogFingerprint, profileFingerprint, claim.id(), claim.leaseToken());
    if (published != 1) throw new IllegalStateException("Lease expired during publication");
    return true;
  }

  private boolean owns(RecommendationBatch current, RecommendationBatch claim, Instant now) {
    return current.status() == RecommendationBatchStatus.BUILDING
        && claim.leaseToken() != null && claim.leaseToken().equals(current.leaseToken())
        && current.leaseUntil() != null && current.leaseUntil().isAfter(now);
  }

  @Override
  @Transactional
  public boolean fail(RecommendationBatch claim, String failureCode) {
    if (failureCode == null || !failureCode.matches("[A-Z0-9_]{1,64}")) throw new IllegalArgumentException("Unsanitized failure code");
    return jdbc.update("""
        UPDATE recommendation_batches SET status='FAILED',failed_at=UTC_TIMESTAMP(6),
          next_attempt_at=CASE WHEN automatic_attempt_count>=3 THEN NULL
            ELSE TIMESTAMPADD(SECOND,CASE WHEN automatic_attempt_count>=2 THEN 300 ELSE 60 END,UTC_TIMESTAMP(6)) END,
          lease_token=NULL,lease_until=NULL,failure_code=?,updated_at=UTC_TIMESTAMP(6)
        WHERE id=? AND status='BUILDING' AND lease_token=? AND lease_until>UTC_TIMESTAMP(6)
        """, failureCode, claim.id(), claim.leaseToken()) == 1;
  }

  @Override
  @Transactional
  public boolean releaseNoProfile(RecommendationBatch claim, GenerationOrigin origin) {
    return jdbc.update("""
        UPDATE recommendation_batches SET status='PENDING',lease_token=NULL,lease_until=NULL,
          automatic_attempt_count=GREATEST(0,automatic_attempt_count-?),failed_at=NULL,next_attempt_at=NULL,
          failure_code=NULL,operational_pending_since=NULL,updated_at=UTC_TIMESTAMP(6)
        WHERE id=? AND status='BUILDING' AND lease_token=? AND lease_until>UTC_TIMESTAMP(6)
        """, origin == GenerationOrigin.AUTO ? 1 : 0, claim.id(), claim.leaseToken()) == 1;
  }

  @Override
  @Transactional
  public void markDormant(long userId, LocalDate date) {
    jdbc.update("""
        UPDATE recommendation_batches SET operational_pending_since=NULL,updated_at=UTC_TIMESTAMP(6)
        WHERE user_id=? AND reference_date=? AND status='PENDING' AND lease_token IS NULL
        """, userId, date);
  }

  @Override
  @Transactional
  public void reactivate(long userId, LocalDate date) {
    jdbc.update("""
        UPDATE recommendation_batches SET operational_pending_since=UTC_TIMESTAMP(6),updated_at=UTC_TIMESTAMP(6)
        WHERE user_id=? AND reference_date=? AND status IN ('PENDING','FAILED') AND operational_pending_since IS NULL
        """, userId, date);
  }

  @Override
  public List<RecommendationBatch> pendingAfter(LocalDate date, long afterBatchId, int limit) {
    return jdbc.query("""
        SELECT * FROM recommendation_batches WHERE reference_date=? AND id>? AND status<>'READY'
        ORDER BY id LIMIT ?
        """, RecommendationBatchMapper::fromRow, date, afterBatchId, positiveLimit(limit));
  }

  @Override
  @Transactional
  public int retain(LocalDate today, int chunkSize) {
    int limit = positiveLimit(chunkSize);
    List<Long> readyIds = jdbc.queryForList("""
        SELECT old.id FROM recommendation_batches old WHERE old.status='READY' AND
          (SELECT COUNT(*) FROM recommendation_batches newer WHERE newer.user_id=old.user_id
            AND newer.status='READY' AND newer.reference_date>old.reference_date)>=2 ORDER BY old.id LIMIT ?
        """, Long.class, limit);
    int removed = 0;
    for (Long id : readyIds) removed += jdbc.update("DELETE FROM recommendation_batches WHERE id=? AND status='READY'", id);
    if (removed < limit) {
      List<Long> expiredIds = jdbc.queryForList("""
          SELECT id FROM recommendation_batches WHERE reference_date<? AND status<>'READY'
            AND (lease_until IS NULL OR lease_until<=UTC_TIMESTAMP(6)) ORDER BY id LIMIT ?
          """, Long.class, today.minusDays(7), limit - removed);
      for (Long id : expiredIds) removed += jdbc.update("""
          DELETE FROM recommendation_batches WHERE id=? AND status<>'READY'
            AND (lease_until IS NULL OR lease_until<=UTC_TIMESTAMP(6))
          """, id);
    }
    return removed;
  }

  private static long positiveMicros(Duration duration) {
    long micros = duration.toNanos() / 1000;
    if (micros <= 0) throw new IllegalArgumentException("Lease must be positive");
    return micros;
  }
  private static int positiveLimit(int value) {
    if (value <= 0) throw new IllegalArgumentException("Limit must be positive");
    return value;
  }
}
