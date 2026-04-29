package com.pucsp.alexandria.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "books")
public class BookEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 255)
  private String title;

  @Column(name = "gutenberg_id", unique = true, nullable = true)
  private Long gutenbergId;

  @Column(name = "download_url", nullable = true, columnDefinition = "LONGTEXT")
  private String downloadUrl;

  @Column(name = "cover_url", nullable = true, columnDefinition = "LONGTEXT")
  private String coverUrl;

  @Column(nullable = true, columnDefinition = "LONGTEXT")
  private String languages;

  @Column(nullable = true, columnDefinition = "LONGTEXT")
  private String subjects;

  @Column(name = "download_count", nullable = true)
  private Integer downloadCount;

  @Column(name = "publisher_id", nullable = true)
  private Long publisherId;

  @Column(nullable = false)
  private String source;

  public BookEntity() {}

  public BookEntity(Long id, String title, Long gutenbergId, String downloadUrl,
                   String coverUrl, String languages, String subjects,
                   Integer downloadCount, Long publisherId, String source) {
    this.id = id;
    this.title = title;
    this.gutenbergId = gutenbergId;
    this.downloadUrl = downloadUrl;
    this.coverUrl = coverUrl;
    this.languages = languages;
    this.subjects = subjects;
    this.downloadCount = downloadCount;
    this.publisherId = publisherId;
    this.source = source;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public Long getGutenbergId() {
    return gutenbergId;
  }

  public void setGutenbergId(Long gutenbergId) {
    this.gutenbergId = gutenbergId;
  }

  public String getDownloadUrl() {
    return downloadUrl;
  }

  public void setDownloadUrl(String downloadUrl) {
    this.downloadUrl = downloadUrl;
  }

  public String getCoverUrl() {
    return coverUrl;
  }

  public void setCoverUrl(String coverUrl) {
    this.coverUrl = coverUrl;
  }

  public String getLanguages() {
    return languages;
  }

  public void setLanguages(String languages) {
    this.languages = languages;
  }

  public String getSubjects() {
    return subjects;
  }

  public void setSubjects(String subjects) {
    this.subjects = subjects;
  }

  public Integer getDownloadCount() {
    return downloadCount;
  }

  public void setDownloadCount(Integer downloadCount) {
    this.downloadCount = downloadCount;
  }

  public Long getPublisherId() {
    return publisherId;
  }

  public void setPublisherId(Long publisherId) {
    this.publisherId = publisherId;
  }

  public String getSource() {
    return source;
  }

  public void setSource(String source) {
    this.source = source;
  }
}

