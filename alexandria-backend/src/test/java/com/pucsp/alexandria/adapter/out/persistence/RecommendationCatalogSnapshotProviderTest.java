package com.pucsp.alexandria.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.*;
import com.pucsp.alexandria.domain.recommendation.*;
import com.pucsp.alexandria.support.RecommendationFixtures;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class RecommendationCatalogSnapshotProviderTest {
  private static final LocalDate DATE=LocalDate.of(2026,10,30);

  @Test void ownerAndRegisteredTasksKeepOneLazySnapshotAcrossTwoWorkersAndSixReaders() throws Exception {
    var builds=new AtomicInteger();
    var provider=new RecommendationCatalogSnapshotProvider(()->{builds.incrementAndGet();return RecommendationFixtures.catalog(40);});
    var owner=provider.open(DATE);
    List<RecommendationCatalogSnapshots.Task> tasks=new ArrayList<>();
    for(int i=0;i<6;i++)tasks.add(owner.register());
    assertEquals(0,builds.get());
    var workers=Executors.newFixedThreadPool(2);
    try {
      // Submit in three waves: there are no active consumers between waves, only registered work/owner.
      for(int wave=0;wave<3;wave++) {
        var barrier=new CyclicBarrier(2);
        List<Future<String>> results=new ArrayList<>();
        for(int reader=0;reader<2;reader++) {
          var registration=tasks.get(wave*2+reader);
          results.add(workers.submit(()->{
            try(registration;var handle=registration.acquire()) {
              barrier.await(10,TimeUnit.SECONDS);
              assertEquals(40,handle.books().size());
              assertThrows(UnsupportedOperationException.class,()->handle.books().clear());
              return handle.fingerprint();
            }
          }));
        }
        assertEquals(results.get(0).get(10,TimeUnit.SECONDS),results.get(1).get(10,TimeUnit.SECONDS));
        assertEquals(1,builds.get());assertEquals(1,provider.activeSessions());
      }
      owner.close();owner.close();
      assertEquals(0,provider.activeSessions());
      assertThrows(IllegalStateException.class,owner::register);
    } finally { tasks.forEach(RecommendationCatalogSnapshots.Task::close);owner.close();workers.shutdownNow(); }
  }

  @Test void overlappingOriginsShareDateButNewDateBuildsFreshWithoutInvalidatingOldHandle() {
    var data=new AtomicReference<>(RecommendationFixtures.catalog(1));var builds=new AtomicInteger();
    var provider=new RecommendationCatalogSnapshotProvider(()->{builds.incrementAndGet();return data.get();});
    try(var cron=provider.open(DATE);var cronTask=cron.register();var old=cronTask.acquire()) {
      try(var recovery=provider.open(DATE);var read=recovery.register();var same=read.acquire()) {
        assertSame(old.books(),same.books());assertEquals(1,builds.get());
      }
      data.set(RecommendationFixtures.catalog(2));
      try(var today=provider.open(DATE.plusDays(1));var current=today.register();var fresh=current.acquire()) {
        assertEquals(2,fresh.books().size());assertEquals(1,old.books().size());
        assertNotEquals(old.fingerprint(),fresh.fingerprint());assertEquals(2,provider.activeSessions());
      }
      assertEquals(1,provider.activeSessions());assertEquals(1,old.books().size());
    }
    assertEquals(0,provider.activeSessions());assertEquals(2,builds.get());
  }

  @Test void closeOwnerBeforeQueuedWorkAndRejectCancellationAreSafeAndIdempotent() {
    var builds=new AtomicInteger();var provider=new RecommendationCatalogSnapshotProvider(()->{builds.incrementAndGet();return List.of();});
    var owner=provider.open(DATE);var queued=owner.register();var rejected=owner.register();
    rejected.close();rejected.close();owner.close();
    assertEquals(1,provider.activeSessions());assertEquals(0,builds.get());
    var handle=queued.acquire();queued.close();queued.close();
    assertEquals(1,provider.activeSessions());assertTrue(handle.books().isEmpty());
    handle.close();handle.close();assertEquals(0,provider.activeSessions());
    assertThrows(IllegalStateException.class,queued::acquire);assertThrows(IllegalStateException.class,handle::books);
  }

  @Test void failedReadIsSingleFlightAndNeverExposesPartialProjection() throws Exception {
    var builds=new AtomicInteger();var reading=new CountDownLatch(1);var fail=new CountDownLatch(1);
    var provider=new RecommendationCatalogSnapshotProvider(()->{
      builds.incrementAndGet();reading.countDown();
      try{fail.await(10,TimeUnit.SECONDS);}catch(InterruptedException e){Thread.currentThread().interrupt();throw new IllegalStateException(e);}
      throw new IllegalStateException("CATALOG_FAILED");
    });
    var owner=provider.open(DATE);var first=owner.register();var second=owner.register();var workers=Executors.newFixedThreadPool(2);
    try {
      var a=workers.submit(first::acquire);assertTrue(reading.await(10,TimeUnit.SECONDS));
      var b=workers.submit(second::acquire);fail.countDown();
      assertThrows(ExecutionException.class,()->a.get(10,TimeUnit.SECONDS));
      assertThrows(ExecutionException.class,()->b.get(10,TimeUnit.SECONDS));
      assertEquals(1,builds.get());
    } finally {fail.countDown();first.close();second.close();owner.close();workers.shutdownNow();}
    assertEquals(0,provider.activeSessions());
  }

  @Test void fingerprintDoesNotDependOnCollectionOrDatabaseIterationOrder() {
    var data=new AtomicReference<>(RecommendationFixtures.catalog(3));
    var provider=new RecommendationCatalogSnapshotProvider(data::get);String first;
    try(var session=provider.open(DATE);var task=session.register();var handle=task.acquire()){first=handle.fingerprint();}
    var reversed=new ArrayList<>(data.get());java.util.Collections.reverse(reversed);data.set(reversed);
    try(var session=provider.open(DATE);var task=session.register();var handle=task.acquire()){assertEquals(first,handle.fingerprint());}
  }

  @Test void interruptedConsumerLeavesWhileBuilderRemainsBlockedWithoutCancellingSharedBuild() throws Exception {
    var entered=new CountDownLatch(1);var releaseBuilder=new CountDownLatch(1);var consumerExited=new CountDownLatch(1);
    var builds=new AtomicInteger();var interruptedFlag=new AtomicBoolean();var failure=new AtomicReference<Throwable>();
    var provider=new RecommendationCatalogSnapshotProvider(()->{
      builds.incrementAndGet();entered.countDown();
      try {
        if(!releaseBuilder.await(10,TimeUnit.SECONDS))throw new IllegalStateException("Builder barrier timed out");
      }catch(InterruptedException ex){Thread.currentThread().interrupt();throw new IllegalStateException(ex);}
      return RecommendationFixtures.catalog(2);
    });
    var owner=provider.open(DATE);var building=owner.register();var waiting=owner.register();
    var executor=Executors.newSingleThreadExecutor();
    var consumer=new Thread(()->{
      try(waiting;var unexpected=waiting.acquire()) {
        failure.set(new AssertionError("Interrupted consumer acquired a handle"));
      }catch(CancellationException expected) {
        interruptedFlag.set(Thread.currentThread().isInterrupted());
      }catch(Throwable unexpected){failure.set(unexpected);}
      finally{consumerExited.countDown();}
    },"snapshot-interrupted-consumer");
    try {
      var builder=executor.submit(()->{
        try(building;var handle=building.acquire()){return handle.books().size();}
      });
      assertTrue(entered.await(5,TimeUnit.SECONDS));consumer.start();
      long deadline=System.nanoTime()+TimeUnit.SECONDS.toNanos(5);
      while(consumer.getState()!=Thread.State.WAITING&&consumer.isAlive()&&System.nanoTime()<deadline)
        java.util.concurrent.locks.LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(1));
      assertEquals(Thread.State.WAITING,consumer.getState(),"Consumer must be waiting on the shared build");
      owner.close();consumer.interrupt();
      assertTrue(consumerExited.await(5,TimeUnit.SECONDS),"Consumer must leave before the builder is released");
      assertNull(failure.get());assertTrue(interruptedFlag.get(),"Interrupted flag must be restored");
      assertFalse(builder.isDone());assertEquals(1,releaseBuilder.getCount());assertEquals(1,provider.activeSessions());
      releaseBuilder.countDown();assertEquals(2,builder.get(5,TimeUnit.SECONDS));
      assertEquals(1,builds.get(),"Interrupting one consumer must not cancel or rebuild the shared future");
      assertEquals(0,provider.activeSessions(),"Cancelled consumer must not leak a task or handle");
    } finally {
      releaseBuilder.countDown();consumer.interrupt();consumer.join(5000);
      waiting.close();building.close();owner.close();executor.shutdownNow();
    }
  }
}
