package com.pucsp.alexandria.application.recommendation;

import com.pucsp.alexandria.domain.recommendation.*;
import java.time.*;

public final class RequestDailyRecommendationGenerationUseCase {
  private final RecommendationBatchRepository batches;
  private final RecommendationReadPort reads;
  private final ReadingProfileRepository profiles;
  private final ReadingProfileBuilder builder;
  private final RecommendationCatalogSnapshots snapshots;
  private final RecommendationGenerationQueue queue;
  private final RecommendationWorkFactory workFactory;
  private final Clock clock;
  private final ZoneId zone;
  public RequestDailyRecommendationGenerationUseCase(RecommendationBatchRepository batches,RecommendationReadPort reads,
      ReadingProfileRepository profiles,ReadingProfileBuilder builder,RecommendationCatalogSnapshots snapshots,
      RecommendationGenerationQueue queue,RecommendationWorkFactory workFactory,Clock clock,ZoneId zone){
    this.batches=batches;this.reads=reads;this.profiles=profiles;this.builder=builder;this.snapshots=snapshots;
    this.queue=queue;this.workFactory=workFactory;this.clock=clock;this.zone=zone;
  }
  public record Result(String state,LocalDate referenceDate,String generationStatus,Integer retryAfterSeconds){}
  public LocalDate today(){return LocalDate.now(clock.withZone(zone));}
  public Result execute(long userId,GenerationOrigin origin){
    return execute(userId,today(),origin,null);
  }
  public Result execute(long userId,LocalDate date,GenerationOrigin origin,RecommendationCatalogSnapshots.Session owner){
    if(!date.equals(today()))return result("UNAVAILABLE",date,"IDLE",3);
    if(reads.readyToday(userId,date).isPresent())return result("READY",date,"READY",null);
    if(!builder.build(profiles.load(userId)).hasPositiveSignals()){
      batches.markDormant(userId,date);return result("NO_PROFILE",date,"NO_PROFILE",null);
    }
    var batch=batches.ensurePending(userId,date,RecommendationSelector.ALGORITHM_VERSION);
    batches.reactivate(userId,date);
    for(int i=0;i<3 && batch.status()==RecommendationBatchStatus.BUILDING
        && batch.leaseUntil()!=null && !batch.leaseUntil().isAfter(batches.now());i++){
      batches.normalizeExpired(batch);batch=batches.find(userId,date).orElse(batch);
    }
    if(batch.status()==RecommendationBatchStatus.READY)return result("READY",date,"READY",null);
    if(batch.status()==RecommendationBatchStatus.BUILDING){
      if(batch.leaseUntil()!=null && batch.leaseUntil().isAfter(batches.now()))
        return result("ALREADY_RUNNING",date,"BUILDING",3);
      return result("UNAVAILABLE",date,"FAILED",3);
    }
    if(batch.status()==RecommendationBatchStatus.FAILED){
      Integer manualCooldown=remaining(batch.failedAt()==null?null:batch.failedAt().plusSeconds(60));
      if(origin==GenerationOrigin.MANUAL && manualCooldown!=null)return result("COOLDOWN",date,"FAILED",manualCooldown);
      if(origin==GenerationOrigin.AUTO && (batch.automaticAttemptCount()>=3
          ||batch.nextAttemptAt()!=null&&batch.nextAttemptAt().isAfter(batches.now())))
        return result("UNAVAILABLE",date,"FAILED",manualCooldown);
    }
    if(owner!=null)return offer(userId,date,origin,owner);
    try(var session=snapshots.open(date)){return offer(userId,date,origin,session);}
  }
  private Result offer(long userId,LocalDate date,GenerationOrigin origin,RecommendationCatalogSnapshots.Session session){
    var task=session.register();
    RecommendationGenerationQueue.Work work;
    try{work=java.util.Objects.requireNonNull(workFactory.create(userId,date,origin,task));}
    catch(RuntimeException ex){task.close();throw ex;}
    RecommendationGenerationQueue.OfferResult offered;
    try{offered=queue.offer(userId,date,origin,work);}
    catch(RuntimeException failure){work.close();throw failure;}
    if(offered!=RecommendationGenerationQueue.OfferResult.ACCEPTED)work.close();
    return switch(offered){
      case ACCEPTED -> result("ACCEPTED",date,"PENDING",3);
      case ALREADY_RUNNING -> result("ALREADY_RUNNING",date,"PENDING",3);
      case FULL -> result("UNAVAILABLE",date,"IDLE",3);
    };
  }
  private Integer remaining(Instant due){
    if(due==null)return null;
    long millis=Duration.between(batches.now(),due).toMillis();
    return millis>0?(int)Math.max(1,(millis+999)/1000):null;
  }
  private static Result result(String state,LocalDate date,String status,Integer retry){return new Result(state,date,status,retry);}
}
