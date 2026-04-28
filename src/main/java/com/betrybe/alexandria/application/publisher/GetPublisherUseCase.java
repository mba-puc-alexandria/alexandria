package com.betrybe.alexandria.application.publisher;

import com.betrybe.alexandria.application.publisher.dto.PublisherOutput;
import com.betrybe.alexandria.domain.publisher.Publisher;
import com.betrybe.alexandria.domain.publisher.PublisherRepository;
import com.betrybe.alexandria.domain.publisher.exception.PublisherNotFoundException;

public class GetPublisherUseCase {

  private final PublisherRepository publisherRepository;

  public GetPublisherUseCase(PublisherRepository publisherRepository) {
    this.publisherRepository = publisherRepository;
  }

  public PublisherOutput execute(Long id) {
    Publisher publisher = publisherRepository.findById(id)
        .orElseThrow(() -> new PublisherNotFoundException(id));

    return PublisherOutput.from(publisher);
  }
}

