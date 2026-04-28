package com.betrybe.alexandria.domain.user;

import com.betrybe.alexandria.domain.shared.valueobject.Email;
import com.betrybe.alexandria.domain.user.exception.InvalidUserException;
import java.time.LocalDateTime;

public class User {

  private final UserId id;
  private final Email email;
  private final String name;
  private final LocalDateTime createdAt;

  private User(UserId id, Email email, String name, LocalDateTime createdAt) {
    this.id = id;
    this.email = email;
    this.name = name;
    this.createdAt = createdAt;
  }

  public static User create(String email, String name) {
    validateName(name);
    Email emailVO = new Email(email);
    return new User(null, emailVO, name, LocalDateTime.now());
  }

  public static User restore(Long id, String email, String name, LocalDateTime createdAt) {
    validateName(name);
    Email emailVO = new Email(email);
    UserId userId = UserId.from(id);
    return new User(userId, emailVO, name, createdAt);
  }

  public User updateWith(String name) {
    String finalName = name != null ? name : this.name;
    validateName(finalName);
    return new User(this.id, this.email, finalName, this.createdAt);
  }

  private static void validateName(String name) {
    if (name == null || name.isBlank()) {
      throw new InvalidUserException("User name is required");
    }
    if (name.length() > 255) {
      throw new InvalidUserException("User name must not exceed 255 characters");
    }
  }

  public UserId getId() {
    return id;
  }

  public Email getEmail() {
    return email;
  }

  public String getName() {
    return name;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null || getClass() != obj.getClass()) return false;
    User user = (User) obj;
    return id != null ? id.equals(user.id) : user.id == null;
  }

  @Override
  public int hashCode() {
    return id != null ? id.hashCode() : 0;
  }
}

