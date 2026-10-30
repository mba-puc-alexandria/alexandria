package com.pucsp.alexandria.domain.recommendation;

import java.time.LocalDate;
import java.util.List;

/** Owned lazy snapshot sessions; queued work is registered before submission. */
public interface RecommendationCatalogSnapshots {
  Session open(LocalDate referenceDate);
  interface Session extends AutoCloseable {
    Task register();
    @Override void close();
  }
  interface Task extends AutoCloseable {
    Handle acquire();
    @Override void close();
  }
  interface Handle extends AutoCloseable {
    List<CatalogBookFeatures> books();
    String fingerprint();
    @Override void close();
  }
}
