package com.pucsp.alexandria.config;

import com.pucsp.alexandria.adapter.in.job.RecommendationExecutionResources;
import com.pucsp.alexandria.domain.recommendation.*;
import java.time.Clock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(RecommendationProperties.class)
public class RecommendationConfiguration {
  @Bean(name="recommendationClock")
  @ConditionalOnMissingBean(name="recommendationClock")
  public Clock recommendationClock(){return Clock.systemUTC();}
  @Bean(destroyMethod="close")
  public RecommendationExecutionResources recommendationExecutionResources(RecommendationProperties properties){
    return new RecommendationExecutionResources(properties.getWorkers(),properties.getQueueCapacity());
  }
  @Bean public ReadingProfileBuilder recommendationProfileBuilder(){return new ReadingProfileBuilder();}
  @Bean public RecommendationScorer recommendationScorer(){return new RecommendationScorer();}
  @Bean public RecommendationSelector recommendationSelector(RecommendationScorer scorer){return new RecommendationSelector(scorer);}
}
