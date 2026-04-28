package com.betrybe.alexandria.domain.shared.valueobject;

public abstract class Id<T> {

  private final T value;

  protected Id(T value) {
    this.value = value;
  }

  public T getValue() {
    return value;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null || getClass() != obj.getClass()) return false;
    Id<?> id = (Id<?>) obj;
    return value != null ? value.equals(id.value) : id.value == null;
  }

  @Override
  public int hashCode() {
    return value != null ? value.hashCode() : 0;
  }

  @Override
  public String toString() {
    return value != null ? value.toString() : "null";
  }
}


