package com.pucsp.alexandria.domain.recommendation;

import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

class RecommendationScorerTest {
  private final RecommendationScorer scorer=new RecommendationScorer();
  private final RecommendationSelector selector=new RecommendationSelector(scorer);
  private final ReadingProfileBuilder builder=new ReadingProfileBuilder();
  @Test void respectsSignalPriorityAndNeverUsesLanguageOrPopularityAsEligibility() {
    var profile=builder.build(List.of(ReadingProfileTest.signal(1,"done",5,"forte"),ReadingProfileTest.signal(2,"reading",null,"médio"),
        ReadingProfileTest.signal(3,"toread",null,"leve"),ReadingProfileTest.signal(4,"done",3,"fraco")));
    var books=List.of(book(10,"fraco",0),book(11,"leve",0),book(12,"médio",0),book(13,"forte",0),book(14,"outro",99999));
    assertEquals(List.of(13L,12L,11L,10L),selector.select(books,profile).stream().map(RecommendationCandidate::bookId).toList());
    assertTrue(scorer.score(book(14,"outro",99999),profile).isEmpty());
  }
  @Test void considersStrongCandidatesAtEndAndMatchesIndependentFullReferenceSort() {
    var profile=builder.build(List.of(ReadingProfileTest.signal(1,"done",5,"s4"),ReadingProfileTest.signal(2,"reading",null,"s3"),
        ReadingProfileTest.signal(3,"toread",null,"s2"),ReadingProfileTest.signal(4,"done",3,"s1")));
    var books=IntStream.range(10,1010).mapToObj(i->book(i,"s"+(1+(i%4)),i%17)).toList();
    var expected=books.stream().sorted((a,b)->{
      int weightA=Integer.parseInt(a.canonicalSubjects().iterator().next().substring(1));
      int weightB=Integer.parseInt(b.canonicalSubjects().iterator().next().substring(1));
      int c=Integer.compare(weightB,weightA);
      if(c==0)c=Integer.compare(b.downloadCount(),a.downloadCount());
      return c!=0?c:Long.compare(a.bookId(),b.bookId());
    }).limit(30).map(CatalogBookFeatures::bookId).toList();
    assertEquals(expected,selector.select(books,profile).stream().map(RecommendationCandidate::bookId).toList());
    assertTrue(expected.stream().anyMatch(id->id>900));
  }
  @Test void computesIntegerMeansAndSubjectPrecedesAuthorWithoutAuthorCap() {
    var profile=builder.build(List.of(ReadingProfileTest.signal(1,"done",5,"a"),ReadingProfileTest.signal(2,"done",3,"b")));
    var score=scorer.score(new CatalogBookFeatures(10,"Livro",Map.of(9L,"Autor"),Set.of("a","b"),Set.of("pt"),2),profile).orElseThrow().score();
    assertEquals(4,score.smax()); assertEquals(2500,score.smean()); assertEquals(4,score.amax());
    assertEquals(30,selector.select(IntStream.range(10,70).mapToObj(i->new CatalogBookFeatures(i,"Livro",Map.of(9L,"Autor"),Set.of(),Set.of(),0)).toList(),profile).size());
  }
  @Test void returnsOnlyCompatibleNewBooksWithDeterministicReasonsAndIds() {
    var profile=builder.build(List.of(ReadingProfileTest.signal(1,"reading",null,"z; a")));
    var first=new CatalogBookFeatures(10,"Livro",Map.of(),new LinkedHashSet<>(List.of("z","a")),Set.of(),0);
    var second=new CatalogBookFeatures(11,"Livro",Map.of(),new LinkedHashSet<>(List.of("a","z")),Set.of(),0);
    assertEquals("a",scorer.score(first,profile).orElseThrow().reason().matchedValue());
    assertEquals(scorer.score(first,profile).orElseThrow().reason(),scorer.score(second,profile).orElseThrow().reason());
    assertEquals(List.of(10L,11L),selector.select(List.of(second,first,book(1,"a",99),book(12,"none",99)),profile).stream().map(RecommendationCandidate::bookId).toList());
  }
  @Test void authorReasonTieUsesIdAndEmptyCatalogHasNoFallback() {
    var profile=builder.build(List.of(new ReadingSignal(1,"Evidência","reading",null,Map.of(8L,"Z",3L,"A"),null,null)));
    var match=scorer.score(new CatalogBookFeatures(10,"Novo",Map.of(8L,"Z",3L,"A"),Set.of(),Set.of(),0),profile).orElseThrow();
    assertEquals("AUTHOR",match.reason().type()); assertEquals("A",match.reason().matchedValue());
    assertTrue(selector.select(List.of(),profile).isEmpty());
  }
  private static CatalogBookFeatures book(long id,String subject,int downloads) {
    return new CatalogBookFeatures(id,"Livro "+id,Map.of(),Set.of(subject),Set.of("pt"),downloads);
  }
}
