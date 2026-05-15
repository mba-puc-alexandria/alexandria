package com.pucsp.alexandria.domain.book;

import com.pucsp.alexandria.domain.author.AuthorId;
import com.pucsp.alexandria.domain.book.exception.InvalidBookException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class Book {

  private final BookId id;
  private final String title;
  private final Set<AuthorId> authorIds;
  private final Long gutendexId;
  private final String downloadUrl;
  private final String coverUrl;
  private final String languages;
  private final String subjects;
  private final Integer downloadCount;
  private final Long publisherId;
  private final BookSource source;

  private Book(BookId id, String title, Set<AuthorId> authorIds, Long gutendexId, String downloadUrl, String coverUrl,
      String languages, String subjects, Integer downloadCount, Long publisherId, BookSource source) {
    this.id = id;
    this.title = title;
    this.authorIds = authorIds != null
        ? Collections.unmodifiableSet(new HashSet<>(authorIds))
        : Set.of();
    this.gutendexId = gutendexId;
    this.downloadUrl = downloadUrl;
    this.coverUrl = coverUrl;
    this.languages = languages;
    this.subjects = subjects;
    this.downloadCount = downloadCount;
    this.publisherId = publisherId;
    this.source = source;
  }

  public static Book createLocal(String title, Set<AuthorId> authorIds, Long publisherId) {
    validateTitle(title);
    validateAuthorIds(authorIds);
    validatePublisherIdForLocal(publisherId);
    return new Book(null, title, authorIds, null, null, null, null, null, null, publisherId, BookSource.LOCAL);
  }

  public static Book createFromGutendex(Long gutendexId, String title, Set<AuthorId> authorIds, String downloadUrl,
      String coverUrl, String languages, String subjects, Integer downloadCount) {
    validateTitle(title);
    validateAuthorIds(authorIds);
    validateGutendexId(gutendexId);
    return new Book(null, title, authorIds, gutendexId, downloadUrl, coverUrl, languages, subjects,
        downloadCount, null, BookSource.GUTENDEX);
  }

  public static Book restore(Long id, String title, Set<Long> authorIds, Long gutendexId, String downloadUrl,
      String coverUrl, String languages, String subjects, Integer downloadCount, Long publisherId,
      BookSource source) {
    validateTitle(title);
    Set<AuthorId> ids = authorIds.stream()
        .map(AuthorId::from)
        .collect(Collectors.toUnmodifiableSet());
    validateAuthorIds(ids);
    if (source == BookSource.LOCAL) {
      validatePublisherIdForLocal(publisherId);
    }
    BookId bookId = BookId.from(id);
    return new Book(bookId, title, ids, gutendexId, downloadUrl, coverUrl, languages, subjects,
        downloadCount, publisherId, source);
  }

  private static void validateTitle(String title) {
    if (title == null || title.isBlank()) {
      throw new InvalidBookException("Book title is required");
    }
    if (title.length() > 500) {
      throw new InvalidBookException("Book title must not exceed 500 characters");
    }
  }

  private static void validateAuthorIds(Set<AuthorId> authorIds) {
    if (authorIds == null || authorIds.isEmpty()) {
      throw new InvalidBookException("Book must have at least one author");
    }
  }

  private static void validateGutendexId(Long gutendexId) {
    if (gutendexId == null || gutendexId <= 0) {
      throw new InvalidBookException("Valid Gutendex ID is required");
    }
  }

  private static void validatePublisherIdForLocal(Long publisherId) {
    if (publisherId == null || publisherId <= 0) {
      throw new InvalidBookException("Valid publisher ID is required for LOCAL books");
    }
  }

  public Book updateWith(String title) {
    String finalTitle = title != null ? title : this.title;
    validateTitle(finalTitle);
    return new Book(this.id, finalTitle, this.authorIds, this.gutendexId,
        this.downloadUrl, this.coverUrl, this.languages, this.subjects,
        this.downloadCount, this.publisherId, this.source);
  }

  public BookId getId() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  public Set<AuthorId> getAuthorIds() {
    return authorIds;
  }

  public Long getGutendexId() {
    return gutendexId;
  }

  public String getDownloadUrl() {
    return downloadUrl;
  }

  public String getCoverUrl() {
    return coverUrl;
  }

  public String getLanguages() {
    return languages;
  }

  public String getSubjects() {
    return subjects;
  }

  public Integer getDownloadCount() {
    return downloadCount;
  }

  public Long getPublisherId() {
    return publisherId;
  }

  public BookSource getSource() {
    return source;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null || getClass() != obj.getClass()) return false;
    Book book = (Book) obj;
    return id != null ? id.equals(book.id) : book.id == null;
  }

  @Override
  public int hashCode() {
    return id != null ? id.hashCode() : 0;
  }
}
