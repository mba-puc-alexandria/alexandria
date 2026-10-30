package com.pucsp.alexandria.adapter.in.job;
import com.pucsp.alexandria.application.recommendation.RequestDailyRecommendationGenerationUseCase;
import com.pucsp.alexandria.config.RecommendationProperties;
import com.pucsp.alexandria.domain.recommendation.RecommendationBatchRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
@Component
@ConditionalOnProperty(name="recommendations.jobs-enabled",havingValue="true",matchIfMissing=true)
public class RecommendationRetentionJob {
  private final RecommendationBatchRepository batches;
  private final RequestDailyRecommendationGenerationUseCase request;
  private final RecommendationExecutionResources resources;
  private final RecommendationProperties properties;
  public RecommendationRetentionJob(RecommendationBatchRepository batches,RequestDailyRecommendationGenerationUseCase request,
      RecommendationExecutionResources resources,RecommendationProperties properties){this.batches=batches;this.request=request;this.resources=resources;this.properties=properties;}
  @Scheduled(fixedDelayString="${recommendations.retention-delay-ms:3600000}") public void retain(){
    resources.observe("retention",()->{
      while(!resources.isStopping()&&!Thread.currentThread().isInterrupted()
          &&batches.retain(request.today(),properties.getPageSize())>=properties.getPageSize()){
        // Each repository call commits a bounded chunk; never keep locks while waiting.
      }
    });
  }
}
