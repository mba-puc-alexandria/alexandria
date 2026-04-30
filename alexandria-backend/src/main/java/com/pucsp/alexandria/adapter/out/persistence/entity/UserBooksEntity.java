package com.pucsp.alexandria.adapter.out.persistence.entity;

import com.pucsp.alexandria.domain.book.BookStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_books")

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

  @Enumerated(EnumType.STRING)
  private BookStatus status = BookStatus.toread;

  private Integer progress = 0;

  private Integer rating;

  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt = LocalDateTime.now();
}