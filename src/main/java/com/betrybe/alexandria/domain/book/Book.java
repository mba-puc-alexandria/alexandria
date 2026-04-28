package com.betrybe.alexandria.domain.book;

import com.betrybe.alexandria.domain.book.exception.InvalidBookException;

public class Book {

  private final BookId id;
  private final String title;
  private final String genre;
  private final Long publisherId;
  private final BookSource source;

  private Book(BookId id, String title, String genre, Long publisherId, BookSource source) {
    this.id = id;
    this.title = title;
    this.genre = genre;
    this.publisherId = publisherId;
    this.source = source;
  }

  public static Book create(String title, String genre, Long publisherId) {
    validateTitle(title);
    validateGenre(genre);
    validatePublisherId(publisherId);
    return new Book(null, title, genre, publisherId, BookSource.LOCAL);
  }

  public static Book restore(Long id, String title, String genre, Long publisherId, BookSource source) {
    validateTitle(title);
    validateGenre(genre);
    validatePublisherId(publisherId);
    BookId bookId = BookId.from(id);
    return new Book(bookId, title, genre, publisherId, source);
  }

  public Book updateWith(String title, String genre, Long publisherId) {
    String finalTitle = title != null ? title : this.title;
    String finalGenre = genre != null ? genre : this.genre;
    Long finalPublisherId = publisherId != null ? publisherId : this.publisherId;

    validateTitle(finalTitle);
    validateGenre(finalGenre);
    validatePublisherId(finalPublisherId);

    return new Book(this.id, finalTitle, finalGenre, finalPublisherId, this.source);
  }

  private static void validateTitle(String title) {
    if (title == null || title.isBlank()) {
      throw new InvalidBookException("Book title is required");
    }
    if (title.length() > 255) {
      throw new InvalidBookException("Book title must not exceed 255 characters");
    }
  }

  private static void validateGenre(String genre) {
    if (genre == null || genre.isBlank()) {
      throw new InvalidBookException("Book genre is required");
    }
    if (genre.length() > 100) {
      throw new InvalidBookException("Book genre must not exceed 100 characters");
    }
  }

  private static void validatePublisherId(Long publisherId) {
    if (publisherId == null || publisherId <= 0) {
      throw new InvalidBookException("Valid publisher ID is required");
    }
  }

  public BookId getId() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  public String getGenre() {
    return genre;
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

