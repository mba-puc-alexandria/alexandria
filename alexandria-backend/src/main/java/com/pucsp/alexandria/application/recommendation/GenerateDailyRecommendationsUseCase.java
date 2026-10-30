package com.pucsp.alexandria.application.recommendation;

import com.pucsp.alexandria.domain.recommendation.*;
import java.util.function.BooleanSupplier;

public final class GenerateDailyRecommendationsUseCase {
  private final RecommendationBatchRepository batches;
  private final ReadingProfileRepository profiles;
  private final ReadingProfileBuilder builder;
  private final RecommendationSelector selector;
  public GenerateDailyRecommendationsUseCase(RecommendationBatchRepository batches,ReadingProfileRepository profiles,
      ReadingProfileBuilder builder,RecommendationSelector selector){
    this.batches=batches;this.profiles=profiles;this.builder=builder;this.selector=selector;
  }
  public record Outcome(String state,int analyzed,int selected){}
  public Outcome execute(RecommendationBatch claim,GenerationOrigin origin,RecommendationCatalogSnapshots.Task task,BooleanSupplier ownsLease){
    var profile=builder.build(profiles.load(claim.userId()));
    if(!profile.hasPositiveSignals()){
      batches.releaseNoProfile(claim,origin);return new Outcome("NO_PROFILE",0,0);
    }
    if(!ownsLease.getAsBoolean())return new Outcome("LOST_OWNERSHIP",0,0);
    try(var handle=task.acquire()){
      if(!ownsLease.getAsBoolean())return new Outcome("LOST_OWNERSHIP",0,0);
      var selected=selector.select(handle.books(),profile);
      if(!ownsLease.getAsBoolean())return new Outcome("LOST_OWNERSHIP",handle.books().size(),0);
      boolean published=batches.publish(claim,selected,handle.fingerprint(),profile.fingerprint());
      return new Outcome(published?"READY":"LOST_OWNERSHIP",handle.books().size(),published?selected.size():0);
    }
  }
}
