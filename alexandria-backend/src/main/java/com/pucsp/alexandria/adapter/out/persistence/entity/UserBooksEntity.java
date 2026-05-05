package com.pucsp.alexandria.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_books", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "book_id"})
})
public class UserBooksEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private UserEntity user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "book_id", nullable = false)
  private BookEntity book;

  @Column(name = "status", nullable = false, length = 20)
  private String status = "toread";

  private Integer progress;

  private Integer rating;

  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt = LocalDateTime.now();

  public UserBooksEntity() {}

  public UserBooksEntity(Long id, UserEntity user, BookEntity book, String status,
                         Integer progress, Integer rating, LocalDateTime createdAt) {
    this.id = id;
    this.user = user;
    this.book = book;
    this.status = status;
    this.progress = progress;
    this.rating = rating;
    this.createdAt = createdAt;
  }

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }

  public UserEntity getUser() { return user; }
  public void setUser(UserEntity user) { this.user = user; }

  public BookEntity getBook() { return book; }
  public void setBook(BookEntity book) { this.book = book; }

  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }

  public Integer getProgress() { return progress; }
  public void setProgress(Integer progress) { this.progress = progress; }

  public Integer getRating() { return rating; }
  public void setRating(Integer rating) { this.rating = rating; }

  public LocalDateTime getCreatedAt() { return createdAt; }
  public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
