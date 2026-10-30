package com.pucsp.alexandria.domain.recommendation;

import java.util.*;
import com.pucsp.alexandria.domain.recommendation.ReadingProfile.Evidence;
import com.pucsp.alexandria.domain.recommendation.RecommendationCandidate.Reason;
import com.pucsp.alexandria.domain.recommendation.RecommendationCandidate.Score;

public final class RecommendationScorer {
  public static final Comparator<RecommendationCandidate> BEST_FIRST=
      Comparator.comparingInt((RecommendationCandidate c)->c.score().smax()).reversed()
        .thenComparing(Comparator.comparingInt((RecommendationCandidate c)->c.score().smean()).reversed())
        .thenComparing(Comparator.comparingInt((RecommendationCandidate c)->c.score().amax()).reversed())
        .thenComparing(Comparator.comparingInt((RecommendationCandidate c)->c.score().amean()).reversed())
        .thenComparing(Comparator.comparingInt((RecommendationCandidate c)->c.score().lmax()).reversed())
        .thenComparing(Comparator.comparingInt((RecommendationCandidate c)->c.score().popularity()).reversed())
        .thenComparingLong(RecommendationCandidate::bookId);

  public Optional<RecommendationCandidate> score(CatalogBookFeatures book,ReadingProfile profile) {
    if(profile.excludedBookIds().contains(book.bookId()))return Optional.empty();
    var subjects=book.canonicalSubjects().stream().filter(profile.subjects()::containsKey)
        .map(key->Map.entry(key,profile.subjects().get(key)))
        .sorted(Comparator.<Map.Entry<String,Evidence>>comparingInt(e->e.getValue().weight()).reversed()
            .thenComparing(Map.Entry::getKey)).toList();
    var authors=book.authors().keySet().stream().filter(profile.authors()::containsKey)
        .map(key->Map.entry(key,profile.authors().get(key)))
        .sorted(Comparator.<Map.Entry<Long,Evidence>>comparingInt(e->e.getValue().weight()).reversed()
            .thenComparing(Map.Entry::getKey)).toList();
    if(subjects.isEmpty()&&authors.isEmpty())return Optional.empty();
    var subjectWeights=subjects.stream().map(e->e.getValue().weight()).toList();
    var authorWeights=authors.stream().map(e->e.getValue().weight()).toList();
    int language=book.languageCodes().stream().map(profile.languages()::get).filter(Objects::nonNull)
        .mapToInt(Evidence::weight).max().orElse(0);
    Evidence evidence=!subjects.isEmpty()?subjects.get(0).getValue():authors.get(0).getValue();
    String type=!subjects.isEmpty()?"SUBJECT":"AUTHOR";
    String value=!subjects.isEmpty()?subjects.get(0).getKey():evidence.matchedValue();
    String text="Na data desta seleção, sua biblioteca incluía “"+evidence.sourceBookTitle()+
        "”, com "+(type.equals("SUBJECT")?"o assunto ":"autoria de ")+value+".";
    return Optional.of(new RecommendationCandidate(book.bookId(),
        new Score(max(subjectWeights),mean(subjectWeights),max(authorWeights),mean(authorWeights),language,book.downloadCount()),
        new Reason(type,value,text,evidence.sourceBookId(),evidence.sourceBookTitle(),evidence.weight())));
  }
  private static int max(List<Integer> values){return values.stream().mapToInt(Integer::intValue).max().orElse(0);}
  private static int mean(List<Integer> values){return values.isEmpty()?0:(int)(1000L*values.stream().mapToLong(Integer::longValue).sum()/values.size());}
}
