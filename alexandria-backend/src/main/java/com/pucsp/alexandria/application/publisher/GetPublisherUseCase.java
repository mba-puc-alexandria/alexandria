package com.pucsp.alexandria.application.publisher;

import com.pucsp.alexandria.application.publisher.dto.PublisherOutput;
import com.pucsp.alexandria.domain.publisher.Publisher;
import com.pucsp.alexandria.domain.publisher.PublisherRepository;
import com.pucsp.alexandria.domain.publisher.exception.PublisherNotFoundException;

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

