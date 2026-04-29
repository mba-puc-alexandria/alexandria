package com.pucsp.alexandria.application.publisher;

import com.pucsp.alexandria.application.publisher.dto.PublisherOutput;
import com.pucsp.alexandria.domain.publisher.PublisherRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public class ListPublishersUseCase {

  private final PublisherRepository publisherRepository;

  public ListPublishersUseCase(PublisherRepository publisherRepository) {
    this.publisherRepository = publisherRepository;
  }

  public Page<PublisherOutput> execute(Pageable pageable) {
    return publisherRepository.findAll(pageable)
        .map(PublisherOutput::from);
  }
}

