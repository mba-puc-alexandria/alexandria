package com.betrybe.alexandria.application.publisher;

import com.betrybe.alexandria.application.publisher.dto.CreatePublisherInput;
import com.betrybe.alexandria.application.publisher.dto.CreatePublisherOutput;
import com.betrybe.alexandria.domain.publisher.Publisher;
import com.betrybe.alexandria.domain.publisher.PublisherRepository;
import com.betrybe.alexandria.domain.publisher.exception.DuplicatePublisherException;

public class CreatePublisherUseCase {

  private final PublisherRepository publisherRepository;

  public CreatePublisherUseCase(PublisherRepository publisherRepository) {
    this.publisherRepository = publisherRepository;
  }

  public CreatePublisherOutput execute(CreatePublisherInput input) {
    if (publisherRepository.existsByName(input.name())) {
      throw new DuplicatePublisherException(input.name());
    }

    Publisher publisher = Publisher.create(input.name(), input.address());
    Publisher saved = publisherRepository.save(publisher);

    return new CreatePublisherOutput(saved.getId().getValue());
  }
}

