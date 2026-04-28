package com.betrybe.alexandria.application.publisher.dto;

import com.betrybe.alexandria.domain.publisher.Publisher;

public record PublisherOutput(
    Long id,
    String name,
    String address
) {

  public static PublisherOutput from(Publisher publisher) {
    return new PublisherOutput(
        publisher.getId().getValue(),
        publisher.getName(),
        publisher.getAddress()
    );
  }
}

