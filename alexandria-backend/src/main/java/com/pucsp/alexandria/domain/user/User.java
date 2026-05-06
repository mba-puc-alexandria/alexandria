package com.pucsp.alexandria.domain.user;

import com.pucsp.alexandria.domain.shared.valueobject.Email;
import com.pucsp.alexandria.domain.user.exception.InvalidUserException;
import java.time.LocalDateTime;

public class User {

  private final UserId id;
  private final String username;
  private final String firstName;
  private final String lastName;
  private final Email email;
  private final String password;
  private final LocalDateTime createdAt;

  private User(UserId id, String username, String firstName, String lastName, Email email, String password, LocalDateTime createdAt) {
    this.id = id;
    this.username = username;
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = email;
    this.password = password;
    this.createdAt = createdAt;
  }

  public static User create(String username, String firstName, String lastName, String email, String password) {
    validateUsername(username);
    validateFirstName(firstName);
    validateLastName(lastName);
    validatePassword(password);
    Email emailVO = new Email(email);
    return new User(null, username, firstName, lastName, emailVO, password, LocalDateTime.now());
  }

  public static User restore(Long id, String username, String firstName, String lastName, String email, String password, LocalDateTime createdAt) {
    validateUsername(username);
    validateFirstName(firstName);
    validateLastName(lastName);
    validatePassword(password);
    Email emailVO = new Email(email);
    UserId userId = UserId.from(id);
    return new User(userId, username, firstName, lastName, emailVO, password, createdAt);
  }

  public User updateWith(String username, String firstName, String lastName, String password) {
    String finalUsername = username != null ? username : this.username;
    String finalFirstName = firstName != null ? firstName : this.firstName;
    String finalLastName = lastName != null ? lastName : this.lastName;
    String finalPassword = password != null ? password : this.password;
    validateUsername(finalUsername);
    validateFirstName(finalFirstName);
    validateLastName(finalLastName);
    validatePassword(finalPassword);
    return new User(this.id, finalUsername, finalFirstName, finalLastName, this.email, finalPassword, this.createdAt);
  }

  private static void validateUsername(String username) {
    if (username == null || username.isBlank()) {
      throw new InvalidUserException("Username is required");
    }
    if (username.length() > 255) {
      throw new InvalidUserException("Username must not exceed 255 characters");
    }
  }

  private static void validateFirstName(String firstName) {
    if (firstName == null || firstName.isBlank()) {
      throw new InvalidUserException("First name is required");
    }
    if (firstName.length() > 255) {
      throw new InvalidUserException("First name must not exceed 255 characters");
    }
  }

  private static void validateLastName(String lastName) {
    if (lastName == null || lastName.isBlank()) {
      throw new InvalidUserException("Last name is required");
    }
    if (lastName.length() > 255) {
      throw new InvalidUserException("Last name must not exceed 255 characters");
    }
  }

  private static void validatePassword(String password) {
    if (password == null || password.isBlank()) {
      throw new InvalidUserException("Password is required");
    }
    if (password.length() < 8) {
      throw new InvalidUserException("Password must be at least 8 characters long");
    }
    if (password.length() > 255) {
      throw new InvalidUserException("Password must not exceed 255 characters");
    }
  }

  public UserId getId() {
    return id;
  }

  public String getUsername() {
    return username;
  }

  public String getFirstName() {
    return firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public Email getEmail() {
    return email;
  }

  public String getPassword() {
    return password;
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

