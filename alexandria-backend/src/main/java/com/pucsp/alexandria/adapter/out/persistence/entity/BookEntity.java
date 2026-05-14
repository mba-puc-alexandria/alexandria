package com.pucsp.alexandria.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "books")
public class BookEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 500)
  private String title;

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
      name = "book_authors",
      joinColumns = @JoinColumn(name = "book_id"),
      inverseJoinColumns = @JoinColumn(name = "author_id")
  )
  private Set<AuthorEntity> authors = new HashSet<>();

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

  public BookEntity() {
  }

  public BookEntity(Long id, String title, Set<AuthorEntity> authors, Long gutendexId, String downloadUrl,
      String coverUrl, String languages, String subjects,
      Integer downloadCount, Long publisherId, String source) {
    this.id = id;
    this.title = title;
    this.authors = authors != null ? authors : new HashSet<>();
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

  public void setId(Long id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public Set<AuthorEntity> getAuthors() {
    return authors;
  }

  public void setAuthors(Set<AuthorEntity> authors) {
    this.authors = authors;
  }

  public Long getGutendexId() {
    return gutendexId;
  }

  public void setGutendexId(Long gutendexId) {
    this.gutendexId = gutendexId;
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
