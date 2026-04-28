package com.betrybe.alexandria.adapter.in.rest.dto;

import com.betrybe.alexandria.application.publisher.dto.PublisherOutput;

public record PublisherResponse(
    Long id,
    String name,
    String address
) {

  public static PublisherResponse from(PublisherOutput output) {
    return new PublisherResponse(
        output.id(),
        output.name(),
        output.address()
    );
  }
}

