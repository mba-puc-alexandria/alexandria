package com.betrybe.alexandria.domain.publisher;

import com.betrybe.alexandria.domain.publisher.exception.InvalidPublisherException;

public class Publisher {

  private final PublisherId id;
  private final String name;
  private final String address;

  private Publisher(PublisherId id, String name, String address) {
    this.id = id;
    this.name = name;
    this.address = address;
  }

  public static Publisher create(String name, String address) {
    validateName(name);
    validateAddress(address);
    return new Publisher(null, name, address);
  }

  public static Publisher restore(Long id, String name, String address) {
    validateName(name);
    validateAddress(address);
    PublisherId publisherId = PublisherId.from(id);
    return new Publisher(publisherId, name, address);
  }

  public Publisher updateWith(String name, String address) {
    String finalName = name != null ? name : this.name;
    String finalAddress = address != null ? address : this.address;

    validateName(finalName);
    validateAddress(finalAddress);

    return new Publisher(this.id, finalName, finalAddress);
  }

  private static void validateName(String name) {
    if (name == null || name.isBlank()) {
      throw new InvalidPublisherException("Publisher name is required");
    }
    if (name.length() > 255) {
      throw new InvalidPublisherException("Publisher name must not exceed 255 characters");
    }
  }

  private static void validateAddress(String address) {
    if (address == null || address.isBlank()) {
      throw new InvalidPublisherException("Publisher address is required");
    }
    if (address.length() > 500) {
      throw new InvalidPublisherException("Publisher address must not exceed 500 characters");
    }
  }

  public PublisherId getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getAddress() {
    return address;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null || getClass() != obj.getClass()) return false;
    Publisher publisher = (Publisher) obj;
    return id != null ? id.equals(publisher.id) : publisher.id == null;
  }

  @Override
  public int hashCode() {
    return id != null ? id.hashCode() : 0;
  }
}

