package com.pucsp.alexandria.domain.recommendation;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ReadingProfileTest {
  private final ReadingProfileBuilder builder = new ReadingProfileBuilder();
  @Test void normalizesWholeSubjectsAndLanguagesWithoutSplittingCommasOrLosingAccents() {
    assertEquals(Set.of("ficção, história -- brasil", "romance"),
        RecommendationFeatureNormalizer.subjects(" ＦＩＣÇÃＯ, História -- Brasil ; ROMANCE; romance; "));
    assertEquals(Set.of("pt", "en"), RecommendationFeatureNormalizer.languages("PT, en, pt, "));
    assertEquals(Set.of(), RecommendationFeatureNormalizer.subjects(null));
  }
  @Test void usesAllSignalsWithMaximumWeightAndLowestEvidenceBookId() {
    var profile = builder.build(List.of(signal(9,"done",5,"romance"), signal(2,"done",4,"romance"),
        signal(3,"reading",null,"história"),signal(4,"toread",null,"poesia"),signal(5,"done",3,"drama")));
    assertEquals(Set.of(2L,3L,4L,5L,9L), profile.excludedBookIds());
    assertEquals(4, profile.subjects().get("romance").weight());
    assertEquals(2, profile.subjects().get("romance").sourceBookId());
    assertEquals(3, profile.subjects().get("história").weight());
    assertEquals(2, profile.subjects().get("poesia").weight());
    assertEquals(1, profile.subjects().get("drama").weight());
  }
  @Test void lowAndZeroRatingsAreValidExclusionsButNeverPositiveEvidence() {
    var profile = builder.build(List.of(signal(1,"done",0,"ficção"),signal(2,"done",1,"ficção"),
        signal(3,"done",2,"ficção")));
    assertFalse(profile.hasPositiveSignals());
    assertEquals(3,profile.excludedBookIds().size());
    assertTrue(profile.authors().isEmpty());
  }
  @Test void authorIdentityUsesIdAndMissingMetadataDoesNotInventSignals() {
    var profile = builder.build(List.of(new ReadingSignal(1,"Livro","reading",null,Map.of(7L,"Nome"),null,null),
        new ReadingSignal(2,"Outro","toread",null,Map.of(8L,"Nome"),null,null)));
    assertEquals(Set.of(7L,8L),profile.authors().keySet());
    assertTrue(profile.subjects().isEmpty());
    assertFalse(builder.build(List.of(new ReadingSignal(3,"Sem metadados","toread",null,Map.of(),null,"pt"))).hasPositiveSignals());
  }
  @Test void profileFingerprintIsStableAcrossInputOrderAndChangesWithRating() {
    var a=signal(1,"done",5,"ficção"); var b=signal(2,"toread",null,"história");
    assertEquals(builder.build(List.of(a,b)).fingerprint(),builder.build(List.of(b,a)).fingerprint());
    assertNotEquals(builder.build(List.of(a)).fingerprint(),builder.build(List.of(signal(1,"done",0,"ficção"))).fingerprint());
  }
  @Test void blankAuthorMetadataDoesNotCreateAnUnexplainablePositiveSignal(){
    var profile=builder.build(List.of(new ReadingSignal(1,"Livro","reading",null,Map.of(7L," "),null,null)));
    assertFalse(profile.hasPositiveSignals());assertEquals(Set.of(1L),profile.excludedBookIds());
  }
  static ReadingSignal signal(long id,String status,Integer rating,String subject) {
    return new ReadingSignal(id,"Livro "+id,status,rating,Map.of(9L,"Autor"),subject,"pt");
  }
}
