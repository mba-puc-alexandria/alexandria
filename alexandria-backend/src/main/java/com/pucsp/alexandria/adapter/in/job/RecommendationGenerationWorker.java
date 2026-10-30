package com.pucsp.alexandria.adapter.in.job;

import com.pucsp.alexandria.application.recommendation.GenerateDailyRecommendationsUseCase;
import com.pucsp.alexandria.config.RecommendationProperties;
import com.pucsp.alexandria.domain.recommendation.*;
import java.time.*;
import java.util.concurrent.atomic.AtomicBoolean;

public final class RecommendationGenerationWorker implements RecommendationWorkFactory {
  private final RecommendationBatchRepository batches;
  private final GenerateDailyRecommendationsUseCase generate;
  private final RecommendationExecutionResources resources;
  private final RecommendationProperties properties;
  private final Clock clock;
  private final RecommendationMetrics metrics;
  public RecommendationGenerationWorker(RecommendationBatchRepository batches,GenerateDailyRecommendationsUseCase generate,
      RecommendationExecutionResources resources,RecommendationProperties properties,Clock clock,RecommendationMetrics metrics){
    this.batches=batches;this.generate=generate;this.resources=resources;this.properties=properties;this.clock=clock;this.metrics=metrics;
  }
  @Override public RecommendationGenerationQueue.Work create(long userId,LocalDate date,GenerationOrigin origin,RecommendationCatalogSnapshots.Task task){
    return new RecommendationGenerationQueue.Work(){
      private final AtomicBoolean closed=new AtomicBoolean();
      @Override public void run(){
        if(closed.get())return;
        try{runClaimed();}finally{close();}
      }
      private void runClaimed(){
        var today=LocalDate.now(clock.withZone(properties.getZone()));
        if(!date.equals(today)){close();return;}
        var claimed=batches.claim(userId,date,today,origin,properties.getLease());
        if(claimed.isEmpty()){close();return;}
        var claim=claimed.get();metrics.attempt(claim,origin);var ownsLease=new AtomicBoolean(true);var running=new AtomicBoolean(true);
        var attemptGuard=new Object();
        var workerThread=Thread.currentThread();long start=System.nanoTime();
        Runnable revokeOwnership=()->{
          synchronized(attemptGuard){
            // Completion uses the same guard: an old callback cannot interrupt
            // this pool thread after it has moved on to another claim.
            if(running.get()){ownsLease.set(false);workerThread.interrupt();}
          }
        };
        java.util.concurrent.ScheduledFuture<?> heartbeat=null;
        try{
          heartbeat=resources.heartbeat(()->{
            if(!running.get())return;
            try{
              if(!batches.heartbeat(claim,properties.getLease()))revokeOwnership.run();
            }catch(RuntimeException ex){revokeOwnership.run();}
          },properties.getHeartbeat());
          var result=generate.execute(claim,origin,task,()->ownsLease.get()&&!Thread.currentThread().isInterrupted());
          metrics.generation(result.state(),origin,result.analyzed(),result.selected(),System.nanoTime()-start);
        }catch(RuntimeException ex){
          if(ownsLease.get()){
            try{batches.fail(claim,"GENERATION_FAILED");}
            catch(RuntimeException unavailable){org.slf4j.LoggerFactory.getLogger(RecommendationGenerationWorker.class)
                .warn("Recommendation attempt interrupted; durable lease recovery required");}
          }
          metrics.generation(ownsLease.get()?"FAILED":"LOST_OWNERSHIP",origin,0,0,System.nanoTime()-start);
        }finally{
          synchronized(attemptGuard){running.set(false);}
          if(heartbeat!=null)heartbeat.cancel(false);
        }
      }
      @Override public void close(){if(closed.compareAndSet(false,true))task.close();}
    };
  }
}
