package com.pucsp.alexandria.application.recommendation;

import com.pucsp.alexandria.application.recommendation.dto.RecommendationsResponse;
import com.pucsp.alexandria.domain.recommendation.*;

public final class GetRecommendationsUseCase {
  private final RecommendationReadPort reads;
  private final RequestDailyRecommendationGenerationUseCase request;
  public GetRecommendationsUseCase(RecommendationReadPort reads,RequestDailyRecommendationGenerationUseCase request){this.reads=reads;this.request=request;}
  public record Result(RecommendationsResponse response,String unavailableStatus,Integer retryAfterSeconds){}
  public Result execute(long userId){
    var date=request.today();var current=reads.readyToday(userId,date);
    if(current.isPresent())return new Result(RecommendationsResponse.from(current.get(),false,"READY",null),null,null);
    var requested=request.execute(userId,date,GenerationOrigin.AUTO,null);
    if("NO_PROFILE".equals(requested.state()))return new Result(RecommendationsResponse.noProfile(),null,null);
    current=reads.readyToday(userId,date);
    if(current.isPresent())return new Result(RecommendationsResponse.from(current.get(),false,"READY",null),null,null);
    var previous=reads.latestReady(userId,date);
    if(previous.isPresent())return new Result(RecommendationsResponse.from(previous.get(),true,requested.generationStatus(),requested.retryAfterSeconds()),null,null);
    if("UNAVAILABLE".equals(requested.state())||"COOLDOWN".equals(requested.state()))
      return new Result(null,requested.generationStatus(),requested.retryAfterSeconds());
    return new Result(RecommendationsResponse.generating(requested.generationStatus()),null,null);
  }
}
