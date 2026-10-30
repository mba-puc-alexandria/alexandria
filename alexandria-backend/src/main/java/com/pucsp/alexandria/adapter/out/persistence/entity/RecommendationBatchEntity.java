package com.pucsp.alexandria.adapter.out.persistence.entity;
import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
@Entity
@Table(name="recommendation_batches",uniqueConstraints=@UniqueConstraint(columnNames={"user_id","reference_date"}))
public class RecommendationBatchEntity {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
  @Column(name="id",nullable=false)
  private Long id;

  @Column(name="user_id",nullable=false)
  private Long userId;

  @Column(name="reference_date",nullable=false)
  private LocalDate referenceDate;

  @Column(name="status",nullable=false,length=16)
  private String status;

  @Column(name="algorithm_version",nullable=false,length=32)
  private String algorithmVersion;
  @JdbcTypeCode(SqlTypes.CHAR)
  @Column(name="catalog_fingerprint",nullable=true,length=64)
  private String catalogFingerprint;
  @JdbcTypeCode(SqlTypes.CHAR)
  @Column(name="profile_fingerprint",nullable=true,length=64)
  private String profileFingerprint;

  @Column(name="selected_count",nullable=false)
  private short selectedCount;
  @JdbcTypeCode(SqlTypes.TIMESTAMP)
  @Column(name="generated_at",nullable=true)
  private Instant generatedAt;
  @JdbcTypeCode(SqlTypes.CHAR)
  @Column(name="lease_token",nullable=true,length=36)
  private String leaseToken;
  @JdbcTypeCode(SqlTypes.TIMESTAMP)
  @Column(name="lease_until",nullable=true)
  private Instant leaseUntil;

  @Column(name="attempt_count",nullable=false)
  private int attemptCount;

  @Column(name="automatic_attempt_count",nullable=false)
  private int automaticAttemptCount;
  @JdbcTypeCode(SqlTypes.TIMESTAMP)
  @Column(name="failed_at",nullable=true)
  private Instant failedAt;
  @JdbcTypeCode(SqlTypes.TIMESTAMP)
  @Column(name="next_attempt_at",nullable=true)
  private Instant nextAttemptAt;
  @JdbcTypeCode(SqlTypes.TIMESTAMP)
  @Column(name="created_at",nullable=false)
  private Instant createdAt;
  @JdbcTypeCode(SqlTypes.TIMESTAMP)
  @Column(name="updated_at",nullable=false)
  private Instant updatedAt;
  @JdbcTypeCode(SqlTypes.TIMESTAMP)
  @Column(name="operational_pending_since",nullable=true)
  private Instant operationalPendingSince;

  @Column(name="failure_code",nullable=true,length=64)
  private String failureCode;
  public RecommendationBatchEntity() {}
  public Long getId() { return id; }
  public Long getUserId() { return userId; }
  public LocalDate getReferenceDate() { return referenceDate; }
  public String getStatus() { return status; }
  public String getAlgorithmVersion() { return algorithmVersion; }
  public String getCatalogFingerprint() { return catalogFingerprint; }
  public String getProfileFingerprint() { return profileFingerprint; }
  public short getSelectedCount() { return selectedCount; }
  public Instant getGeneratedAt() { return generatedAt; }
  public String getLeaseToken() { return leaseToken; }
  public Instant getLeaseUntil() { return leaseUntil; }
  public int getAttemptCount() { return attemptCount; }
  public int getAutomaticAttemptCount() { return automaticAttemptCount; }
  public Instant getFailedAt() { return failedAt; }
  public Instant getNextAttemptAt() { return nextAttemptAt; }
  public Instant getCreatedAt() { return createdAt; }
  public Instant getUpdatedAt() { return updatedAt; }
  public Instant getOperationalPendingSince() { return operationalPendingSince; }
  public String getFailureCode() { return failureCode; }
}
