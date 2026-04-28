package com.betrybe.alexandria.domain.book;

public enum BookSource {
  LOCAL("Local"),
  OPEN_LIBRARY("Open Library"),
  GOOGLE_BOOKS("Google Books");

  private final String label;

  BookSource(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }
}

