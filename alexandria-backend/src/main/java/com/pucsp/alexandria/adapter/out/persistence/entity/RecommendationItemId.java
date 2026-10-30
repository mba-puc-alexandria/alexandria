package com.pucsp.alexandria.adapter.out.persistence.entity;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
@Embeddable
public class RecommendationItemId implements Serializable {
  @Column(name="batch_id",nullable=false) private Long batchId;
  @Column(name="position",nullable=false) private short position;
  public RecommendationItemId() {}
  public RecommendationItemId(Long batchId, short position) { this.batchId=batchId; this.position=position; }
  public Long getBatchId() { return batchId; }
  public short getPosition() { return position; }
  @Override public boolean equals(Object other) { return other instanceof RecommendationItemId id && Objects.equals(batchId,id.batchId) && position==id.position; }
  @Override public int hashCode() { return Objects.hash(batchId,position); }
}
