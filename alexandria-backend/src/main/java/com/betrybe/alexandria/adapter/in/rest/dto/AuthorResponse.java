package com.betrybe.alexandria.adapter.in.rest.dto;

import com.betrybe.alexandria.application.author.dto.AuthorOutput;

public record AuthorResponse(
    Long id,
    String name,
    String biography
) {

  public static AuthorResponse from(AuthorOutput output) {
    return new AuthorResponse(
        output.id(),
        output.name(),
        output.biography()
    );
  }
}

