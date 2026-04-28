package com.betrybe.alexandria.application.publisher;

import com.betrybe.alexandria.domain.publisher.Publisher;
import com.betrybe.alexandria.domain.publisher.PublisherRepository;
import com.betrybe.alexandria.domain.publisher.exception.PublisherNotFoundException;

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

