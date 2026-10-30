package com.pucsp.alexandria.domain.recommendation;
import java.util.Map;
import java.util.Set;
/** Compact immutable catalog features, without persistence entities or digital payloads. */
public record CatalogBookFeatures(long bookId, String title, Map<Long,String> authors,
    Set<String> canonicalSubjects, Set<String> languageCodes, int downloadCount) {
  public CatalogBookFeatures {
    if (bookId <= 0) throw new IllegalArgumentException("Invalid book id");
    title = title == null ? "" : title;
    authors = Map.copyOf(authors);
    canonicalSubjects = Set.copyOf(canonicalSubjects);
    languageCodes = Set.copyOf(languageCodes);
    downloadCount = Math.max(0, downloadCount);
  }
}
