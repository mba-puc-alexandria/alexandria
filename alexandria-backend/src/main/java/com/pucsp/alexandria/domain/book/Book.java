package com.pucsp.alexandria.domain.book;

import com.pucsp.alexandria.domain.book.exception.InvalidBookException;

public class Book {

  private final BookId id;
  private final String title;
  private final String author;
  private final Long gutendexId;
  private final String downloadUrl;
  private final String coverUrl;
  private final String languages;
  private final String subjects;
  private final Integer downloadCount;
  private final Long publisherId;
  private final BookSource source;

  private Book(BookId id, String title, String author, Long gutendexId, String downloadUrl, String coverUrl,
      String languages, String subjects, Integer downloadCount, Long publisherId, BookSource source) {
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

  public static Book createLocal(String title, Long publisherId) {
    validateTitle(title);
    validatePublisherIdForLocal(publisherId);
    return new Book(null, title, null, null, null, null, null, null, null, publisherId, BookSource.LOCAL);
  }

  public static Book createFromGutendex(Long gutendexId, String title, String author, String downloadUrl,
      String coverUrl, String languages, String subjects, Integer downloadCount) {
    validateTitle(title);
    validateAuthor(author);
    validateGutendexId(gutendexId);
    return new Book(null, title, author, gutendexId, downloadUrl, coverUrl, languages, subjects,
        downloadCount, null, BookSource.GUTENDEX);
  }

  public static Book restore(Long id, String title, String author, Long gutendexId, String downloadUrl,
      String coverUrl, String languages, String subjects, Integer downloadCount, Long publisherId,
      BookSource source) {
    validateTitle(title);
    if (source == BookSource.LOCAL) {
      validatePublisherIdForLocal(publisherId);
    }
    BookId bookId = BookId.from(id);
    return new Book(bookId, title, author, gutendexId, downloadUrl, coverUrl, languages, subjects,
        downloadCount, publisherId, source);
  }

  private static void validateTitle(String title) {
    if (title == null || title.isBlank()) {
      throw new InvalidBookException("Book title is required");
    }
    if (title.length() > 255) {
      throw new InvalidBookException("Book title must not exceed 255 characters");
    }
  }

  private static void validateAuthor(String author) {
    if (author == null || author.isBlank()) {
      throw new InvalidBookException("Book author is required");
    }
    if (author.length() > 255) {
      throw new InvalidBookException("Book author must not exceed 255 characters");
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

  public BookId getId() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  public String getAuthor() {
    return author;
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

