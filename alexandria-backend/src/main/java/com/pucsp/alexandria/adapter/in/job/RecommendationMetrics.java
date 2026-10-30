package com.pucsp.alexandria.adapter.in.job;

import com.pucsp.alexandria.config.RecommendationProperties;
import com.pucsp.alexandria.domain.recommendation.*;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.slf4j.LoggerFactory;

public final class RecommendationMetrics {
  private final MeterRegistry registry;
  private final RecommendationBatchRepository batches;
  private final ReadingProfileRepository profiles;
  private final ReadingProfileBuilder builder;
  private final RecommendationProperties properties;
  private final Clock clock;
  private final Consumer<Overdue> warning;
  private final Map<LocalDate,Instant> lastWarnings=new HashMap<>();
  public record Overdue(LocalDate referenceDate,long pendingReaders,long oldestPendingSeconds){}
  public RecommendationMetrics(MeterRegistry registry,RecommendationBatchRepository batches,ReadingProfileRepository profiles,
      ReadingProfileBuilder builder,RecommendationProperties properties,Clock clock,RecommendationExecutionResources resources){
    this(registry,batches,profiles,builder,properties,clock,value->LoggerFactory.getLogger(RecommendationMetrics.class)
        .warn("Recommendation delay referenceDate={} pendingReaders={} oldestPendingSeconds={}",value.referenceDate(),value.pendingReaders(),value.oldestPendingSeconds()));
    registry.gauge("recommendations.queue.pending",resources,RecommendationExecutionResources::queued);
    registry.gauge("recommendations.workers.active",resources,RecommendationExecutionResources::active);
  }
  public RecommendationMetrics(MeterRegistry registry,RecommendationBatchRepository batches,ReadingProfileRepository profiles,
      ReadingProfileBuilder builder,RecommendationProperties properties,Clock clock,Consumer<Overdue> warning){
    this.registry=registry;this.batches=batches;this.profiles=profiles;this.builder=builder;this.properties=properties;this.clock=clock;this.warning=warning;
  }
  public void generation(String outcome,GenerationOrigin origin,int analyzed,int selected,long nanos){
    registry.counter("recommendations.generations","outcome",outcome,"origin",origin.name(),"version",RecommendationSelector.ALGORITHM_VERSION).increment();
    registry.timer("recommendations.generation.duration","outcome",outcome).record(nanos,TimeUnit.NANOSECONDS);
    registry.summary("recommendations.catalog.analyzed").record(analyzed);
    registry.summary("recommendations.books.selected").record(selected);
    if("FAILED".equals(outcome))registry.counter("recommendations.failures").increment();
  }
  public void attempt(RecommendationBatch claim,GenerationOrigin origin){
    if(origin==GenerationOrigin.MANUAL)registry.counter("recommendations.manual.attempts").increment();
    else if(claim.attemptCount()>1)registry.counter("recommendations.automatic.retries").increment();
  }
  public void get(String state,long nanos,Instant generatedAt){
    registry.timer("recommendations.get.duration","state",state).record(nanos,TimeUnit.NANOSECONDS);
    if(generatedAt!=null)registry.summary("recommendations.ready.age.seconds").record(Math.max(0,Duration.between(generatedAt,clock.instant()).toSeconds()));
  }
  public synchronized void checkOverdue(){
    var date=LocalDate.now(clock.withZone(properties.getZone()));var now=batches.now();
    lastWarnings.keySet().removeIf(previous->!previous.equals(date));
    long after=0,count=0,oldest=0;
    while(true){
      var page=batches.pendingAfter(date,after,properties.getPageSize());
      if(page.isEmpty())break;
      for(var batch:page){
        after=Math.max(after,batch.id());
        if(!batch.referenceDate().equals(date)||batch.status()==RecommendationBatchStatus.READY)continue;
        if(!builder.build(profiles.load(batch.userId())).hasPositiveSignals()){
          if(batch.status()==RecommendationBatchStatus.PENDING)batches.markDormant(batch.userId(),date);
          continue;
        }
        if(batch.operationalPendingSince()==null)continue;
        long age=Duration.between(batch.operationalPendingSince(),now).toSeconds();
        if(age>properties.getOverdueThreshold().toSeconds()){count++;oldest=Math.max(oldest,age);}
      }
      if(page.size()<properties.getPageSize())break;
    }
    var last=lastWarnings.get(date);
    if(count>0&&(last==null||!now.isBefore(last.plus(Duration.ofHours(1))))){
      warning.accept(new Overdue(date,count,oldest));lastWarnings.put(date,now);
    }
  }
}
