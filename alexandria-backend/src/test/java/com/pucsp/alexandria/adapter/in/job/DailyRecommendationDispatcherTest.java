package com.pucsp.alexandria.adapter.in.job;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.pucsp.alexandria.application.recommendation.RequestDailyRecommendationGenerationUseCase;
import com.pucsp.alexandria.config.RecommendationProperties;
import com.pucsp.alexandria.domain.recommendation.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import org.junit.jupiter.api.Test;

class DailyRecommendationDispatcherTest {
  @Test void startupAndRecoveryDiscoverMissingCurrentDayThroughRealRequestAndBoundedQueue() throws Exception {
    var batches=mock(RecommendationBatchRepository.class);var reads=mock(RecommendationReadPort.class);
    var profiles=mock(ReadingProfileRepository.class);var snapshots=mock(RecommendationCatalogSnapshots.class);
    var factory=mock(RecommendationWorkFactory.class);var properties=new RecommendationProperties();properties.setPageSize(1);
    var instant=new AtomicReference<>(Instant.parse("2026-10-30T15:00:00Z"));
    Clock clock=new Clock(){public ZoneId getZone(){return ZoneOffset.UTC;}public Clock withZone(ZoneId zone){return Clock.fixed(instant(),zone);}public Instant instant(){return instant.get();}};
    var readyDates=ConcurrentHashMap.<LocalDate>newKeySet();var completed=new LinkedBlockingQueue<LocalDate>();
    when(profiles.userIdsAfter(eq(0L),eq(1))).thenReturn(List.of(7L));when(profiles.userIdsAfter(eq(7L),eq(1))).thenReturn(List.of());
    when(profiles.load(7)).thenReturn(List.of(new ReadingSignal(1,"Evidence","toread",null,Map.of(),"fiction","en")));
    when(reads.readyToday(eq(7L),any())).thenAnswer(inv->{LocalDate date=inv.getArgument(1);return readyDates.contains(date)
        ?Optional.of(new DailySelection(1,7,date,instant.get(),"content-v1",0,List.of())):Optional.empty();});
    when(batches.ensurePending(eq(7L),any(),anyString())).thenAnswer(inv->new RecommendationBatch(1,7,inv.getArgument(1),RecommendationBatchStatus.PENDING,"content-v1",0,null,null,null,0,0,null,null,instant.get(),instant.get(),null));
    var owner=mock(RecommendationCatalogSnapshots.Session.class);when(snapshots.open(any())).thenReturn(owner);
    when(owner.register()).thenAnswer(inv->mock(RecommendationCatalogSnapshots.Task.class));
    when(factory.create(eq(7L),any(),eq(GenerationOrigin.AUTO),any())).thenAnswer(inv->{
      LocalDate date=inv.getArgument(1);RecommendationCatalogSnapshots.Task task=inv.getArgument(3);
      return new RecommendationGenerationQueue.Work(){
        final AtomicBoolean closed=new AtomicBoolean();
        public void run(){readyDates.add(date);completed.offer(date);}
        public void close(){if(closed.compareAndSet(false,true))task.close();}
      };
    });
    try(var resources=new RecommendationExecutionResources(1,1)){
      var queue=new BoundedRecommendationGenerationQueue(resources);
      var request=new RequestDailyRecommendationGenerationUseCase(batches,reads,profiles,new ReadingProfileBuilder(),snapshots,queue,factory,clock,properties.getZone());
      var dispatcher=new DailyRecommendationDispatcher(request,profiles,snapshots,resources,properties);
      dispatcher.startup();assertEquals(LocalDate.of(2026,10,30),completed.poll(2,TimeUnit.SECONDS));
      // Wait for control ownership to close before simulating a later recovery trigger.
      verify(owner,timeout(2000)).close();
      instant.set(Instant.parse("2026-10-31T15:00:00Z"));
      new RecommendationRecoveryJob(dispatcher).recover();
      assertEquals(LocalDate.of(2026,10,31),completed.poll(2,TimeUnit.SECONDS));
      assertEquals(Set.of(LocalDate.of(2026,10,30),LocalDate.of(2026,10,31)),readyDates);
      verify(batches).ensurePending(7,LocalDate.of(2026,10,30),"content-v1");
      verify(batches).ensurePending(7,LocalDate.of(2026,10,31),"content-v1");
      verify(batches,never()).claim(anyLong(),any(),any(),any(),any());
    }
  }
}
