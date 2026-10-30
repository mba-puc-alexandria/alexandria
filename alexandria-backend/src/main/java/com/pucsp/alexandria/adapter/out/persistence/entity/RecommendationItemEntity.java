package com.pucsp.alexandria.adapter.out.persistence.entity;
import jakarta.persistence.*;
@Entity
@Table(name="recommendation_items",uniqueConstraints=@UniqueConstraint(columnNames={"batch_id","book_id"}))
public class RecommendationItemEntity {
  @EmbeddedId private RecommendationItemId id;
  @Column(name="book_id",nullable=false) private Long bookId;
  @Column(name="reason_type",nullable=false,length=16) private String reasonType;
  @Column(name="reason_value",nullable=false,columnDefinition="TEXT") private String reasonValue;
  @Column(name="reason_text",nullable=false,columnDefinition="TEXT") private String reasonText;
  @Column(name="source_book_id") private Long sourceBookId;
  @Column(name="source_book_title",nullable=false,length=500) private String sourceBookTitle;
  @Column(name="evidence_weight",nullable=false) private short evidenceWeight;
  public RecommendationItemEntity() {}
  public RecommendationItemId getId(){ return id; }
  public Long getBookId(){ return bookId; }
  public String getReasonType(){ return reasonType; }
  public String getReasonValue(){ return reasonValue; }
  public String getReasonText(){ return reasonText; }
  public Long getSourceBookId(){ return sourceBookId; }
  public String getSourceBookTitle(){ return sourceBookTitle; }
  public short getEvidenceWeight(){ return evidenceWeight; }
}
