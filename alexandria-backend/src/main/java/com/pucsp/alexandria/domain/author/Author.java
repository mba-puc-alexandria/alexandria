package com.pucsp.alexandria.domain.author;

import com.pucsp.alexandria.domain.author.exception.InvalidAuthorException;

public class Author {

  private final AuthorId id;
  private final String name;
  private final Integer birthYear;
  private final Integer deathYear;

  private Author(AuthorId id, String name, Integer birthYear, Integer deathYear) {
    this.id = id;
    this.name = name;
    this.birthYear = birthYear;
    this.deathYear = deathYear;
  }

  public static Author create(String name, Integer birthYear, Integer deathYear) {
    validateName(name);
    return new Author(null, name, birthYear, deathYear);
  }

  public static Author restore(Long id, String name, Integer birthYear, Integer deathYear) {
    validateName(name);
    AuthorId authorId = AuthorId.from(id);
    return new Author(authorId, name, birthYear, deathYear);
  }

  public Author updateName(String newName) {
    validateName(newName);
    return new Author(this.id, newName, this.birthYear, this.deathYear);
  }

  private static void validateName(String name) {
    if (name == null || name.isBlank()) {
      throw new InvalidAuthorException("Author name is required");
    }
    if (name.length() > 255) {
      throw new InvalidAuthorException("Author name must not exceed 255 characters");
    }
  }

  public AuthorId getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public Integer getBirthYear() {
    return birthYear;
  }

  public Integer getDeathYear() {
    return deathYear;
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
