package com.pucsp.alexandria.application.publisher;

import com.pucsp.alexandria.application.publisher.dto.CreatePublisherInput;
import com.pucsp.alexandria.application.publisher.dto.CreatePublisherOutput;
import com.pucsp.alexandria.domain.publisher.Publisher;
import com.pucsp.alexandria.domain.publisher.PublisherRepository;
import com.pucsp.alexandria.domain.publisher.exception.DuplicatePublisherException;

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

