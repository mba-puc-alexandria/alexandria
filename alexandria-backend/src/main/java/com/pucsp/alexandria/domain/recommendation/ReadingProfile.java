package com.pucsp.alexandria.domain.recommendation;
import java.util.Map;
import java.util.Set;
public record ReadingProfile(Set<Long> excludedBookIds, Map<String,Evidence> subjects,
    Map<Long,Evidence> authors, Map<String,Evidence> languages, String fingerprint) {
  public ReadingProfile {
    excludedBookIds = Set.copyOf(excludedBookIds);
    subjects = Map.copyOf(subjects); authors = Map.copyOf(authors); languages = Map.copyOf(languages);
  }
  public boolean hasPositiveSignals() { return !subjects.isEmpty() || !authors.isEmpty(); }
  public record Evidence(int weight, long sourceBookId, String sourceBookTitle, String matchedValue) {
    public Evidence {
      if (weight < 1 || weight > 4 || sourceBookId <= 0) throw new IllegalArgumentException("Invalid positive evidence");
      if (sourceBookTitle == null || matchedValue == null) throw new IllegalArgumentException("Missing evidence");
    }
  }
}
