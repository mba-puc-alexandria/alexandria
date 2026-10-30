package com.pucsp.alexandria.adapter.in.job;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
@Component
@ConditionalOnProperty(name="recommendations.jobs-enabled",havingValue="true",matchIfMissing=true)
public class RecommendationMetricsJob {
  private final RecommendationMetrics metrics;
  private final RecommendationExecutionResources resources;
  public RecommendationMetricsJob(RecommendationMetrics metrics,RecommendationExecutionResources resources){this.metrics=metrics;this.resources=resources;}
  @Scheduled(fixedDelayString="${recommendations.metrics-delay-ms:300000}")
  public void check(){resources.observe("overdue",metrics::checkOverdue);}
}
