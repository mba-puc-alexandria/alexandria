package com.pucsp.alexandria.domain.book;

public enum BookSource {
  LOCAL("Local"),
  GUTENDEX("Gutendex");

  private final String label;

  BookSource(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }
}

