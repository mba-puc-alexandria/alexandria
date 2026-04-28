package com.betrybe.alexandria.adapter.out.persistence.mapper;

import com.betrybe.alexandria.adapter.out.persistence.entity.PublisherEntity;
import com.betrybe.alexandria.domain.publisher.Publisher;
import org.springframework.stereotype.Component;

@Component
public class PublisherMapper {

  public Publisher toDomain(PublisherEntity entity) {
    if (entity == null) {
      return null;
    }
    return Publisher.restore(
        entity.getId(),
        entity.getName(),
        entity.getAddress()
    );
  }

  public PublisherEntity toPersistence(Publisher publisher) {
    if (publisher == null) {
      return null;
    }
    return new PublisherEntity(
        publisher.getId() != null ? publisher.getId().getValue() : null,
        publisher.getName(),
        publisher.getAddress()
    );
  }
}

