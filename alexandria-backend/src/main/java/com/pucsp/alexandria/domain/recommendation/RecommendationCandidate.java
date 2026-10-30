package com.pucsp.alexandria.domain.recommendation;
import java.util.Objects;
public record RecommendationCandidate(long bookId, Score score, Reason reason) {
  public RecommendationCandidate {
    if (bookId <= 0) throw new IllegalArgumentException("Invalid book id");
    Objects.requireNonNull(score); Objects.requireNonNull(reason);
    if (score.smax() <= 0 && score.amax() <= 0) throw new IllegalArgumentException("No positive affinity");
    if (reason.sourceBookId() == null) throw new IllegalArgumentException("Missing source book");
  }
  public record Score(int smax, int smean, int amax, int amean, int lmax, int popularity) {}
  public record Reason(String type, String matchedValue, String text, Long sourceBookId,
      String sourceBookTitle, int weight) {
    public Reason {
      if (!"SUBJECT".equals(type) && !"AUTHOR".equals(type)) throw new IllegalArgumentException("Invalid reason type");
      if (weight < 1 || weight > 4 || matchedValue == null || matchedValue.isBlank()
          || text == null || text.isBlank() || sourceBookTitle == null) throw new IllegalArgumentException("Invalid reason evidence");
    }
  }
}
