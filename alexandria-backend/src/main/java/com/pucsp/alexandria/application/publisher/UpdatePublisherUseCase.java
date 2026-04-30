package com.pucsp.alexandria.application.publisher;

import com.pucsp.alexandria.application.publisher.dto.UpdatePublisherInput;
import com.pucsp.alexandria.application.publisher.dto.PublisherOutput;
import com.pucsp.alexandria.domain.publisher.Publisher;
import com.pucsp.alexandria.domain.publisher.PublisherRepository;
import com.pucsp.alexandria.domain.publisher.exception.PublisherNotFoundException;

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

