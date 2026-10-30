package com.pucsp.alexandria.adapter.in.job;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import com.pucsp.alexandria.application.recommendation.GenerateDailyRecommendationsUseCase;
import com.pucsp.alexandria.config.RecommendationProperties;
import com.pucsp.alexandria.domain.recommendation.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class RecommendationGenerationWorkerTest {
  final RecommendationBatchRepository batches=mock(RecommendationBatchRepository.class);
  final ReadingProfileRepository profiles=mock(ReadingProfileRepository.class);
  final RecommendationMetrics metrics=mock(RecommendationMetrics.class);
  final MutableClock clock=new MutableClock(Instant.parse("2026-10-30T02:59:59Z"));
  final LocalDate yesterday=LocalDate.of(2026,10,29);
  final RecommendationProperties properties=new RecommendationProperties();
  final GenerateDailyRecommendationsUseCase generate=new GenerateDailyRecommendationsUseCase(batches,profiles,new ReadingProfileBuilder(),new RecommendationSelector(new RecommendationScorer()));
  @Test void queuedWorkFromYesterdayIsClosedWithoutClaimAfterRollover(){
    var task=mock(RecommendationCatalogSnapshots.Task.class);
    try(var resources=new RecommendationExecutionResources(1,1)){
      var worker=new RecommendationGenerationWorker(batches,generate,resources,properties,clock,metrics);
      var work=worker.create(7,yesterday,GenerationOrigin.AUTO,task);
      clock.set(Instant.parse("2026-10-30T03:00:01Z"));work.run();
      verifyNoInteractions(batches,profiles);verify(task).close();
    }
  }
  @Test void workerStartedBeforeMidnightPublishesOnlyUnderOriginalDate(){
    var claim=claim();when(batches.claim(eq(7L),eq(yesterday),eq(yesterday),eq(GenerationOrigin.AUTO),any())).thenReturn(Optional.of(claim));
    when(profiles.load(7)).thenAnswer(inv->{clock.set(Instant.parse("2026-10-30T03:00:01Z"));return positive();});
    var task=mock(RecommendationCatalogSnapshots.Task.class);var handle=handle();when(task.acquire()).thenReturn(handle);
    when(batches.publish(eq(claim),anyList(),eq("catalog"),anyString())).thenReturn(true);
    try(var resources=new RecommendationExecutionResources(1,1)){
      new RecommendationGenerationWorker(batches,generate,resources,properties,clock,metrics).create(7,yesterday,GenerationOrigin.AUTO,task).run();
      verify(batches).publish(eq(claim),argThat(items->items.size()==1&&items.get(0).bookId()==2),eq("catalog"),anyString());
      assertEquals(yesterday,claim.referenceDate());verify(task).close();verify(handle).close();
    }
  }
  @Test void lostHeartbeatInterruptsComputationWithoutPublishingAndClosesTask() throws Exception {
    properties.setHeartbeat(Duration.ofMillis(5));properties.setLease(Duration.ofSeconds(1));
    var claim=claim();when(batches.claim(anyLong(),any(),any(),any(),any())).thenReturn(Optional.of(claim));
    when(profiles.load(7)).thenReturn(positive());
    when(batches.heartbeat(eq(claim),any())).thenReturn(false);
    var task=mock(RecommendationCatalogSnapshots.Task.class);var done=new CountDownLatch(1);var handle=handle();
    when(task.acquire()).thenAnswer(inv->{
      try{new CountDownLatch(1).await(2,TimeUnit.SECONDS);fail("Heartbeat must interrupt worker");}
      catch(InterruptedException expected){Thread.currentThread().interrupt();}
      return handle;
    });
    doAnswer(inv->{done.countDown();return null;}).when(task).close();
    try(var resources=new RecommendationExecutionResources(1,1)){
      var worker=new RecommendationGenerationWorker(batches,generate,resources,properties,clock,metrics);
      assertTrue(resources.submit(worker.create(7,yesterday,GenerationOrigin.AUTO,task)));
      assertTrue(done.await(2,TimeUnit.SECONDS));
      verify(batches,never()).publish(any(),anyList(),anyString(),anyString());
      verify(batches,never()).fail(any(),anyString());
      verify(metrics).generation(eq("LOST_OWNERSHIP"),eq(GenerationOrigin.AUTO),eq(0),eq(0),anyLong());
    }
  }
  @ParameterizedTest
  @ValueSource(booleans={false,true})
  void lateDatabaseHeartbeatCannotInterruptAnotherClaimOnTheSamePoolThread(boolean databaseFailure) throws Exception {
    properties.setHeartbeat(Duration.ofMillis(5));properties.setLease(Duration.ofSeconds(1));
    var firstClaim=claim();var secondClaim=claim(2,8);
    when(batches.claim(eq(7L),any(),any(),any(),any())).thenReturn(Optional.of(firstClaim));
    when(batches.claim(eq(8L),any(),any(),any(),any())).thenReturn(Optional.of(secondClaim));
    var heartbeatEntered=new CountDownLatch(1);var releaseHeartbeat=new CountDownLatch(1);
    var firstClosed=new CountDownLatch(1);var secondStarted=new CountDownLatch(1);var releaseSecond=new CountDownLatch(1);var secondClosed=new CountDownLatch(1);
    var secondInterrupted=new AtomicBoolean();var firstThread=new AtomicReference<Thread>();var secondThread=new AtomicReference<Thread>();
    when(batches.heartbeat(eq(firstClaim),any())).thenAnswer(inv->{
      heartbeatEntered.countDown();awaitLatch(releaseHeartbeat);
      if(databaseFailure)throw new IllegalStateException("Simulated heartbeat connection failure");
      return false;
    });
    when(batches.heartbeat(eq(secondClaim),any())).thenReturn(true);
    when(profiles.load(7)).thenReturn(positive());
    when(profiles.load(8)).thenAnswer(inv->{
      secondThread.set(Thread.currentThread());secondStarted.countDown();
      try{awaitLatch(releaseSecond);}catch(InterruptedException interrupted){secondInterrupted.set(true);Thread.currentThread().interrupt();}
      return positive();
    });
    when(batches.publish(any(),anyList(),anyString(),anyString())).thenReturn(true);
    var firstTask=mock(RecommendationCatalogSnapshots.Task.class);var secondTask=mock(RecommendationCatalogSnapshots.Task.class);
    var firstHandle=handle();var secondHandle=handle();
    when(firstTask.acquire()).thenAnswer(inv->{firstThread.set(Thread.currentThread());awaitLatch(heartbeatEntered);return firstHandle;});
    when(secondTask.acquire()).thenReturn(secondHandle);
    doAnswer(inv->{firstClosed.countDown();return null;}).when(firstTask).close();
    doAnswer(inv->{secondClosed.countDown();return null;}).when(secondTask).close();
    try(var resources=new RecommendationExecutionResources(1,1)){
      try{
        var worker=new RecommendationGenerationWorker(batches,generate,resources,properties,clock,metrics);
        assertTrue(resources.submit(worker.create(7,yesterday,GenerationOrigin.AUTO,firstTask)));
        assertTrue(heartbeatEntered.await(5,TimeUnit.SECONDS));
        assertTrue(resources.submit(worker.create(8,yesterday,GenerationOrigin.AUTO,secondTask)));
        assertTrue(firstClosed.await(5,TimeUnit.SECONDS));assertTrue(secondStarted.await(5,TimeUnit.SECONDS));
        assertSame(firstThread.get(),secondThread.get());
        releaseHeartbeat.countDown();
        var drained=new CountDownLatch(1);
        var marker=resources.heartbeat(drained::countDown,Duration.ofMillis(1));
        try{assertTrue(drained.await(5,TimeUnit.SECONDS));}finally{marker.cancel(false);}
        assertFalse(secondInterrupted.get());assertFalse(secondThread.get().isInterrupted());
        releaseSecond.countDown();assertTrue(secondClosed.await(5,TimeUnit.SECONDS));
        verify(batches).publish(eq(secondClaim),anyList(),anyString(),anyString());
      }finally{releaseHeartbeat.countDown();releaseSecond.countDown();}
    }
  }
  @Test void ownershipRevocationAndAttemptCompletionAreAtomicBeforePoolThreadReuse() throws Exception {
    properties.setHeartbeat(Duration.ofMillis(5));properties.setLease(Duration.ofSeconds(1));
    var firstClaim=claim();var secondClaim=claim(2,8);
    when(batches.claim(eq(7L),any(),any(),any(),any())).thenReturn(Optional.of(firstClaim));
    when(batches.claim(eq(8L),any(),any(),any(),any())).thenReturn(Optional.of(secondClaim));
    when(batches.heartbeat(eq(firstClaim),any())).thenReturn(false);when(batches.heartbeat(eq(secondClaim),any())).thenReturn(true);
    when(profiles.load(7)).thenReturn(positive());
    var interruptPaused=new CountDownLatch(1);var releaseInterrupt=new CountDownLatch(1);
    var secondStarted=new CountDownLatch(1);var releaseSecond=new CountDownLatch(1);var secondClosed=new CountDownLatch(1);
    var pausedOnce=new AtomicBoolean();var secondInterrupted=new AtomicBoolean();var workerThread=new AtomicReference<Thread>();
    ThreadFactory threads=runnable->{
      var thread=new Thread(runnable,"recommendation-regression-worker"){
        @Override public void interrupt(){
          if(Thread.currentThread().getName().startsWith("recommendation-heartbeat-")&&pausedOnce.compareAndSet(false,true)){
            interruptPaused.countDown();
            try{awaitLatch(releaseInterrupt);}catch(InterruptedException interrupted){Thread.currentThread().interrupt();}
          }
          super.interrupt();
        }
      };
      workerThread.set(thread);return thread;
    };
    when(profiles.load(8)).thenAnswer(inv->{
      secondStarted.countDown();secondInterrupted.set(Thread.currentThread().isInterrupted());
      try{awaitLatch(releaseSecond);}catch(InterruptedException interrupted){secondInterrupted.set(true);Thread.currentThread().interrupt();}
      return positive();
    });
    when(batches.publish(any(),anyList(),anyString(),anyString())).thenReturn(true);
    var firstTask=mock(RecommendationCatalogSnapshots.Task.class);var secondTask=mock(RecommendationCatalogSnapshots.Task.class);
    var firstHandle=handle();var secondHandle=handle();
    when(firstTask.acquire()).thenAnswer(inv->{awaitLatch(interruptPaused);return firstHandle;});
    when(secondTask.acquire()).thenReturn(secondHandle);
    doAnswer(inv->{secondClosed.countDown();return null;}).when(secondTask).close();
    try(var resources=new RecommendationExecutionResources(1,1,Duration.ofSeconds(1),threads)){
      try{
        var worker=new RecommendationGenerationWorker(batches,generate,resources,properties,clock,metrics);
        assertTrue(resources.submit(worker.create(7,yesterday,GenerationOrigin.AUTO,firstTask)));
        assertTrue(resources.submit(worker.create(8,yesterday,GenerationOrigin.AUTO,secondTask)));
        assertTrue(interruptPaused.await(5,TimeUnit.SECONDS));
        long until=System.nanoTime()+TimeUnit.SECONDS.toNanos(5);
        while(workerThread.get().getState()!=Thread.State.BLOCKED&&System.nanoTime()<until)Thread.sleep(1);
        assertEquals(Thread.State.BLOCKED,workerThread.get().getState(),"Old attempt cannot finish while its callback still owns the revocation guard");
        assertEquals(1,secondStarted.getCount(),"Pool must not reuse this thread before the old interrupt completes");
        releaseInterrupt.countDown();assertTrue(secondStarted.await(5,TimeUnit.SECONDS));
        assertFalse(secondInterrupted.get());assertFalse(workerThread.get().isInterrupted());
        releaseSecond.countDown();assertTrue(secondClosed.await(5,TimeUnit.SECONDS));
        verify(batches).publish(eq(secondClaim),anyList(),anyString(),anyString());
      }finally{releaseInterrupt.countDown();releaseSecond.countDown();}
    }
  }
  private static void awaitLatch(CountDownLatch latch) throws InterruptedException {
    if(!latch.await(5,TimeUnit.SECONDS))throw new IllegalStateException("Timed out waiting for controlled interleaving");
  }
  private RecommendationBatch claim(long id,long userId){return new RecommendationBatch(id,userId,yesterday,RecommendationBatchStatus.BUILDING,"content-v1",0,null,"token-"+id,clock.instant().plusSeconds(300),1,1,null,null,clock.instant(),clock.instant(),null);}
  private RecommendationBatch claim(){return new RecommendationBatch(1,7,yesterday,RecommendationBatchStatus.BUILDING,"content-v1",0,null,"token",clock.instant().plusSeconds(300),1,1,null,null,clock.instant(),clock.instant(),null);}
  private static List<ReadingSignal> positive(){return List.of(new ReadingSignal(1,"Evidence","reading",null,Map.of(),"fiction","en"));}
  private static RecommendationCatalogSnapshots.Handle handle(){
    var handle=mock(RecommendationCatalogSnapshots.Handle.class);
    when(handle.books()).thenReturn(List.of(new CatalogBookFeatures(2,"Candidate",Map.of(),Set.of("fiction"),Set.of("en"),0)));
    when(handle.fingerprint()).thenReturn("catalog");return handle;
  }
  private static class MutableClock extends Clock {
    private final AtomicReference<Instant> instant;
    MutableClock(Instant instant){this.instant=new AtomicReference<>(instant);}
    void set(Instant value){instant.set(value);}
    @Override public ZoneId getZone(){return ZoneOffset.UTC;}
    @Override public Clock withZone(ZoneId zone){return Clock.fixed(instant(),zone);}
    @Override public Instant instant(){return instant.get();}
  }
}
