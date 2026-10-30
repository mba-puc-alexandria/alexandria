package com.pucsp.alexandria.domain.recommendation;
import java.time.LocalDate;
public interface RecommendationGenerationQueue {
  OfferResult offer(long userId, LocalDate date, GenerationOrigin origin, Work task);
  enum OfferResult { ACCEPTED, ALREADY_RUNNING, FULL }
  interface Work extends Runnable, AutoCloseable {
    @Override void close();
  }
}
