package com.betrybe.alexandria.domain.author;

import com.betrybe.alexandria.domain.author.exception.InvalidAuthorException;

public class Author {

  private final AuthorId id;
  private final String name;
  private final String biography;

  private Author(AuthorId id, String name, String biography) {
    this.id = id;
    this.name = name;
    this.biography = biography;
  }

  public static Author create(String name, String biography) {
    validateName(name);
    validateBiography(biography);
    return new Author(null, name, biography);
  }

  public static Author restore(Long id, String name, String biography) {
    validateName(name);
    validateBiography(biography);
    AuthorId authorId = AuthorId.from(id);
    return new Author(authorId, name, biography);
  }

  public Author updateWith(String name, String biography) {
    String finalName = name != null ? name : this.name;
    String finalBiography = biography != null ? biography : this.biography;

    validateName(finalName);
    validateBiography(finalBiography);

    return new Author(this.id, finalName, finalBiography);
  }

  private static void validateName(String name) {
    if (name == null || name.isBlank()) {
      throw new InvalidAuthorException("Author name is required");
    }
    if (name.length() > 255) {
      throw new InvalidAuthorException("Author name must not exceed 255 characters");
    }
  }

  private static void validateBiography(String biography) {
    if (biography == null || biography.isBlank()) {
      throw new InvalidAuthorException("Author biography is required");
    }
    if (biography.length() > 2000) {
      throw new InvalidAuthorException("Author biography must not exceed 2000 characters");
    }
  }

  public AuthorId getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getBiography() {
    return biography;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null || getClass() != obj.getClass()) return false;
    Author author = (Author) obj;
    return id != null ? id.equals(author.id) : author.id == null;
  }

  @Override
  public int hashCode() {
    return id != null ? id.hashCode() : 0;
  }
}

