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

  @Column(nullable = false, length = 255)
  private String author;

  @Column(name = "gutendex_id", unique = true, nullable = true)
  private Long gutendexId;

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

  public BookEntity(Long id, String title, String author, Long gutendexId, String downloadUrl,
                   String coverUrl, String languages, String subjects,
                   Integer downloadCount, Long publisherId, String source) {
    this.id = id;
    this.title = title;
    this.author = author;
    this.gutendexId = gutendexId;
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

