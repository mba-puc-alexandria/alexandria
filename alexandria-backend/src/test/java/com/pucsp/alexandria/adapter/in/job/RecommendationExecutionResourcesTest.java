package com.pucsp.alexandria.adapter.in.job;

import static org.junit.jupiter.api.Assertions.*;
import com.pucsp.alexandria.config.RecommendationProperties;
import java.time.Duration;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import com.pucsp.alexandria.domain.recommendation.*;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class RecommendationExecutionResourcesTest {
  @Test void saturatedWorkersAndDiscoveryDoNotBlockHeartbeatAndQueueIsBounded() throws Exception {
    var workerEntered=new CountDownLatch(1); var release=new CountDownLatch(1);
    var beat=new CountDownLatch(2);var discovery=new CountDownLatch(1);var observation=new CountDownLatch(1);
    try(var resources=new RecommendationExecutionResources(1,1)){
      assertTrue(resources.submit(()->{workerEntered.countDown();await(release);}));
      assertTrue(workerEntered.await(1,TimeUnit.SECONDS));
      assertTrue(resources.submit(()->await(release)));
      assertFalse(resources.submit(()->{}));
      assertTrue(resources.discover("day",()->{discovery.countDown();await(release);}));
      assertTrue(discovery.await(1,TimeUnit.SECONDS));
      assertTrue(resources.observe("overdue",observation::countDown));
      assertTrue(observation.await(1,TimeUnit.SECONDS));
      var future=resources.heartbeat(beat::countDown,Duration.ofMillis(10));
      assertTrue(beat.await(1,TimeUnit.SECONDS));
      future.cancel(false);release.countDown();
    }finally{release.countDown();}
  }
  @Test void heartbeatMustBePositiveAndLessThanLease(){
    var properties=new RecommendationProperties();properties.validate();
    properties.setHeartbeat(Duration.ofMinutes(5));assertThrows(IllegalArgumentException.class,properties::validate);
    properties.setHeartbeat(Duration.ZERO);assertThrows(IllegalArgumentException.class,properties::validate);
    properties.setHeartbeat(Duration.ofNanos(1));assertThrows(IllegalArgumentException.class,properties::validate);
  }
  @Test void shutdownCancelsQueuedRegistrationsAndClearsCoalescingKeys() throws Exception {
    var started=new CountDownLatch(1);var blocker=new CountDownLatch(1);var cleaned=new AtomicInteger();
    var workerThread=new AtomicReference<Thread>();
    var resources=new RecommendationExecutionResources(1,1,Duration.ofMillis(10));
    var queue=new BoundedRecommendationGenerationQueue(resources);
    var date=LocalDate.of(2026,10,30);
    assertEquals(RecommendationGenerationQueue.OfferResult.ACCEPTED,queue.offer(1,date,GenerationOrigin.AUTO,
        work(()->{workerThread.set(Thread.currentThread());started.countDown();await(blocker);},cleaned)));
    assertTrue(started.await(1,TimeUnit.SECONDS));
    assertEquals(RecommendationGenerationQueue.OfferResult.ACCEPTED,queue.offer(2,date,GenerationOrigin.AUTO,work(()->fail("Queued work must be cancelled"),cleaned)));
    resources.close();
    assertEquals(2,cleaned.get());assertEquals(0,queue.registered());
    assertFalse(workerThread.get().isAlive());
  }
  @Test void duplicateDoesNotPromoteOriginAndUnusedRegistrationIsClosedByCaller() throws Exception {
    var entered=new CountDownLatch(1);var release=new CountDownLatch(1);var cleaned=new AtomicInteger();
    try(var resources=new RecommendationExecutionResources(1,1)){
      var queue=new BoundedRecommendationGenerationQueue(resources);var date=LocalDate.of(2026,10,30);
      assertEquals(RecommendationGenerationQueue.OfferResult.ACCEPTED,queue.offer(1,date,GenerationOrigin.AUTO,work(()->{entered.countDown();await(release);},cleaned)));
      assertTrue(entered.await(1,TimeUnit.SECONDS));
      var duplicate=work(()->fail("Duplicate must not run"),cleaned);
      assertEquals(RecommendationGenerationQueue.OfferResult.ALREADY_RUNNING,queue.offer(1,date,GenerationOrigin.MANUAL,duplicate));
      duplicate.close();assertEquals(1,cleaned.get());release.countDown();
    }finally{release.countDown();}
    assertEquals(2,cleaned.get());
  }
  private static RecommendationGenerationQueue.Work work(Runnable runnable,AtomicInteger cleaned){
    return new RecommendationGenerationQueue.Work(){
      final AtomicBoolean closed=new AtomicBoolean();
      public void run(){runnable.run();}
      public void close(){if(closed.compareAndSet(false,true))cleaned.incrementAndGet();}
    };
  }
  private static void await(CountDownLatch latch){try{latch.await();}catch(InterruptedException ex){Thread.currentThread().interrupt();}}
}
