package com.pucsp.alexandria.adapter.in.job;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
@Component
@ConditionalOnProperty(name="recommendations.jobs-enabled",havingValue="true",matchIfMissing=true)
public class RecommendationRecoveryJob {
  private final DailyRecommendationDispatcher dispatcher;
  public RecommendationRecoveryJob(DailyRecommendationDispatcher dispatcher){this.dispatcher=dispatcher;}
  @Scheduled(fixedDelayString="${recommendations.recovery-delay-ms:300000}")
  public void recover(){dispatcher.trigger();}
}
