package com.pucsp.alexandria.domain.recommendation;

import static org.junit.jupiter.api.Assertions.*;
import com.pucsp.alexandria.support.RecommendationFixtures;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class RecommendationModelTest {
  @Test void catalogOwnsItsFeaturesAndClampsMissingPopularity() {
    var authors = new HashMap<Long,String>(); authors.put(1L, "Autora");
    var book = new CatalogBookFeatures(1, "Livro", authors, Set.of("ficção"), Set.of("pt"), -1);
    authors.clear();
    assertEquals(1, book.authors().size());
    assertEquals(0, book.downloadCount());
    assertThrows(UnsupportedOperationException.class, () -> book.authors().put(2L,"Outro"));
  }
  @Test void publishedSelectionIsImmutableButAllowsFilteredPositionGaps() {
    var reason = RecommendationFixtures.candidate(2,1).reason();
    var item = new DailySelection.Item(3, new DailySelection.Book(2,"Livro",List.of(),null), reason);
    var items = new ArrayList<>(List.of(item));
    var selection = new DailySelection(1,1,LocalDate.of(2026,10,30),Instant.EPOCH,"content-v1",5,items);
    items.clear();
    assertEquals(1, selection.items().size());
    assertEquals(5, selection.selectedCount());
    assertEquals(3, selection.items().get(0).position());
    assertThrows(UnsupportedOperationException.class, () -> selection.items().clear());
  }
  @Test void invalidPositionsDuplicatesAndNonpositiveEvidenceAreRejected() {
    var candidate = RecommendationFixtures.candidate(2,1);
    var book = new DailySelection.Book(2,"Livro",List.of(),null);
    assertThrows(IllegalArgumentException.class, () -> new DailySelection.Item(31,book,candidate.reason()));
    var item = new DailySelection.Item(1,book,candidate.reason());
    assertThrows(IllegalArgumentException.class, () -> new DailySelection(1,1,LocalDate.now(),Instant.EPOCH,"v",2,List.of(item,item)));
    assertThrows(IllegalArgumentException.class, () -> new ReadingProfile.Evidence(0,1,"Livro","ficção"));
    assertThrows(IllegalArgumentException.class, () -> new RecommendationCandidate(2,
        new RecommendationCandidate.Score(0,0,0,0,4,999),candidate.reason()));
  }
}
