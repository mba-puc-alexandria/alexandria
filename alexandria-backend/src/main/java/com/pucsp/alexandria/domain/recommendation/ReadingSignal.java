package com.pucsp.alexandria.domain.recommendation;
import java.util.Map;
/** Current private library projection; createdAt is intentionally not a reading signal. */
public record ReadingSignal(long bookId, String title, String status, Integer rating,
    Map<Long,String> authors, String subjects, String languages) {
  public ReadingSignal { authors = Map.copyOf(authors); }
}
