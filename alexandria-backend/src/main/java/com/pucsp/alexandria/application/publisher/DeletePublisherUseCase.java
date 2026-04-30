package com.pucsp.alexandria.application.publisher;

import com.pucsp.alexandria.domain.publisher.Publisher;
import com.pucsp.alexandria.domain.publisher.PublisherRepository;
import com.pucsp.alexandria.domain.publisher.exception.PublisherNotFoundException;

public class DeletePublisherUseCase {

  private final PublisherRepository publisherRepository;

  public DeletePublisherUseCase(PublisherRepository publisherRepository) {
    this.publisherRepository = publisherRepository;
  }

  public void execute(Long id) {
    Publisher publisher = publisherRepository.findById(id)
        .orElseThrow(() -> new PublisherNotFoundException(id));

    publisherRepository.delete(publisher);
  }
}

