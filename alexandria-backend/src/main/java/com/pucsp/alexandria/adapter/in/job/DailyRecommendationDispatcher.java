package com.pucsp.alexandria.adapter.in.job;

import com.pucsp.alexandria.application.recommendation.RequestDailyRecommendationGenerationUseCase;
import com.pucsp.alexandria.config.RecommendationProperties;
import com.pucsp.alexandria.domain.recommendation.*;
import java.time.LocalDate;
import java.util.concurrent.locks.LockSupport;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name="recommendations.jobs-enabled",havingValue="true",matchIfMissing=true)
public class DailyRecommendationDispatcher {
  private final RequestDailyRecommendationGenerationUseCase request;
  private final ReadingProfileRepository profiles;
  private final RecommendationCatalogSnapshots snapshots;
  private final RecommendationExecutionResources resources;
  private final RecommendationProperties properties;
  public DailyRecommendationDispatcher(RequestDailyRecommendationGenerationUseCase request,ReadingProfileRepository profiles,
      RecommendationCatalogSnapshots snapshots,RecommendationExecutionResources resources,RecommendationProperties properties){
    this.request=request;this.profiles=profiles;this.snapshots=snapshots;this.resources=resources;this.properties=properties;
  }
  @EventListener(ApplicationReadyEvent.class) public void startup(){trigger();}
  @Scheduled(cron="${recommendations.cron:0 0 0 * * *}",zone="${recommendations.zone:America/Sao_Paulo}")
  public void daily(){trigger();}
  public void trigger(){
    var date=request.today();
    resources.discover(date.toString(),()->discover(date));
  }
  void discover(LocalDate date){
    try(var session=snapshots.open(date)){
      long after=0;
      while(!resources.isStopping()&&!Thread.currentThread().isInterrupted()&&date.equals(request.today())){
        var users=profiles.userIdsAfter(after,properties.getPageSize());if(users.isEmpty())break;
        for(long userId:users){
          if(resources.isStopping()||Thread.currentThread().isInterrupted()||!date.equals(request.today()))return;
          RequestDailyRecommendationGenerationUseCase.Result result;
          do{
            result=request.execute(userId,date,GenerationOrigin.AUTO,session);
            if("IDLE".equals(result.generationStatus()))LockSupport.parkNanos(25_000_000L);
          }while("IDLE".equals(result.generationStatus())&&!resources.isStopping()
              &&!Thread.currentThread().isInterrupted()&&date.equals(request.today()));
          after=userId;
        }
        if(users.size()<properties.getPageSize())break;
      }
    }catch(RuntimeException ex){LoggerFactory.getLogger(getClass()).warn("Recommendation discovery interrupted; durable pending work will be recovered");}
  }
}
