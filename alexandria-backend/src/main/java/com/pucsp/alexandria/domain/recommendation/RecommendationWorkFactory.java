package com.pucsp.alexandria.domain.recommendation;
import java.time.LocalDate;
public interface RecommendationWorkFactory {
  RecommendationGenerationQueue.Work create(long userId,LocalDate date,GenerationOrigin origin,RecommendationCatalogSnapshots.Task task);
}
