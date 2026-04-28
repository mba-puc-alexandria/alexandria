package com.betrybe.alexandria.application.author.dto;

import com.betrybe.alexandria.domain.author.Author;

public record AuthorOutput(
    Long id,
    String name,
    String biography
) {

  public static AuthorOutput from(Author author) {
    return new AuthorOutput(
        author.getId().getValue(),
        author.getName(),
        author.getBiography()
    );
  }
}

