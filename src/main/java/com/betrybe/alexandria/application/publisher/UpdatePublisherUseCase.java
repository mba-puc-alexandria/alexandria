package com.betrybe.alexandria.application.publisher;

import com.betrybe.alexandria.application.publisher.dto.UpdatePublisherInput;
import com.betrybe.alexandria.application.publisher.dto.PublisherOutput;
import com.betrybe.alexandria.domain.publisher.Publisher;
import com.betrybe.alexandria.domain.publisher.PublisherRepository;
import com.betrybe.alexandria.domain.publisher.exception.PublisherNotFoundException;

public class UpdatePublisherUseCase {

  private final PublisherRepository publisherRepository;

  public UpdatePublisherUseCase(PublisherRepository publisherRepository) {
    this.publisherRepository = publisherRepository;
  }

  public PublisherOutput execute(Long id, UpdatePublisherInput input) {
    Publisher publisher = publisherRepository.findById(id)
        .orElseThrow(() -> new PublisherNotFoundException(id));

    Publisher updated = publisher.updateWith(input.name(), input.address());
    Publisher saved = publisherRepository.save(updated);

    return PublisherOutput.from(saved);
  }
}

