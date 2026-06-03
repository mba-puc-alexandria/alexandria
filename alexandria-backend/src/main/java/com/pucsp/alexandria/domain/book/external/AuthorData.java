package com.pucsp.alexandria.domain.book.external;

public record AuthorData(
    String name,
    Integer birthYear,
    Integer deathYear
) {

  public String getFormattedName() {
    if (name == null || name.isBlank()) {
      return null;
    }

    String trimmed = name.trim();

    if (trimmed.contains(",")) {
      String[] parts = trimmed.split(",", 2);
      String lastName = parts[0].trim();
      String firstName = parts.length > 1 ? parts[1].trim() : "";

      if (firstName.isEmpty()) {
        return lastName;
      }

      return firstName + " " + lastName;
    }

    return trimmed;
  }
}
