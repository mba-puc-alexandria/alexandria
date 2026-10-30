package com.pucsp.alexandria.domain.recommendation;

import java.util.*;

public final class RecommendationSelector {
  public static final String ALGORITHM_VERSION="content-v1";
  private final RecommendationScorer scorer;
  public RecommendationSelector(RecommendationScorer scorer){this.scorer=scorer;}
  public List<RecommendationCandidate> select(List<CatalogBookFeatures> catalog,ReadingProfile profile){
    var best=new PriorityQueue<RecommendationCandidate>(31,RecommendationScorer.BEST_FIRST.reversed());
    for(var book:catalog){
      if(Thread.currentThread().isInterrupted())throw new java.util.concurrent.CancellationException("Generation interrupted");
      scorer.score(book,profile).ifPresent(candidate->{
        if(best.size()<30)best.offer(candidate);
        else if(RecommendationScorer.BEST_FIRST.compare(candidate,best.peek())<0){best.poll();best.offer(candidate);}
      });
    }
    return best.stream().sorted(RecommendationScorer.BEST_FIRST).toList();
  }
}
