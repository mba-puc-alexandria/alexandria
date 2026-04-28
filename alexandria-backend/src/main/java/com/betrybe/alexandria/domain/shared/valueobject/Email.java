package com.betrybe.alexandria.domain.shared.valueobject;

import java.util.regex.Pattern;

public class Email {

  private static final Pattern EMAIL_PATTERN =
      Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

  private final String value;

  public Email(String value) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("Email cannot be null or blank");
    }
    if (!isValidEmail(value)) {
      throw new IllegalArgumentException("Invalid email format: " + value);
    }
    this.value = value.toLowerCase();
  }

  public String getValue() {
    return value;
  }

  private static boolean isValidEmail(String email) {
    return EMAIL_PATTERN.matcher(email).matches();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null || getClass() != obj.getClass()) return false;
    Email email = (Email) obj;
    return value.equals(email.value);
  }

  @Override
  public int hashCode() {
    return value.hashCode();
  }

  @Override
  public String toString() {
    return value;
  }
}


