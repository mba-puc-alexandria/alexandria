package com.pucsp.alexandria.adapter.in.job;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.pucsp.alexandria.config.RecommendationProperties;
import com.pucsp.alexandria.domain.recommendation.*;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class RecommendationMetricsTest {
  final RecommendationBatchRepository batches=mock(RecommendationBatchRepository.class);
  final ReadingProfileRepository profiles=mock(ReadingProfileRepository.class);
  final Instant start=Instant.parse("2026-10-30T15:00:00Z");
  final Clock clock=Clock.fixed(start,ZoneOffset.UTC);
  final LocalDate date=LocalDate.of(2026,10,30);
  @Test void warnsOnlyForCurrentPositiveOverdueWorkAndSuppressesForOneHour(){
    var now=new AtomicReference<>(start);when(batches.now()).thenAnswer(i->now.get());
    var page=new AtomicReference<>(List.of(batch(1,date,start.minusSeconds(3700)),batch(2,date,start.minusSeconds(30)),
        batch(3,date.minusDays(1),start.minusSeconds(90000)),batch(4,date,start.minusSeconds(90000))));
    when(batches.pendingAfter(eq(date),eq(0L),anyInt())).thenAnswer(i->page.get());
    when(profiles.load(anyLong())).thenReturn(List.of(new ReadingSignal(1,"private-title","reading",null,Map.of(),"private-subject","pt")));
    when(profiles.load(4)).thenReturn(List.of());
    var warnings=new ArrayList<RecommendationMetrics.Overdue>();var registry=new SimpleMeterRegistry();
    var metrics=new RecommendationMetrics(registry,batches,profiles,new ReadingProfileBuilder(),new RecommendationProperties(),clock,warnings::add);
    metrics.checkOverdue();assertEquals(List.of(new RecommendationMetrics.Overdue(date,1,3700)),warnings);
    now.set(start.plusSeconds(300));metrics.checkOverdue();assertEquals(1,warnings.size());
    now.set(start.plusSeconds(3600));metrics.checkOverdue();assertEquals(2,warnings.size());
    page.set(List.of());metrics.checkOverdue();assertEquals(2,warnings.size());
    assertFalse(warnings.toString().contains("private"));
    verify(profiles,never()).load(3);verify(batches,atLeastOnce()).markDormant(4,date);
  }
  @Test void dormantOrReactivatedPendingStartsFreshWithoutImmediateWarning(){
    when(batches.now()).thenReturn(start);
    var page=new AtomicReference<>(List.of(batch(1,date,null)));
    when(batches.pendingAfter(eq(date),eq(0L),anyInt())).thenAnswer(i->page.get());
    when(profiles.load(1)).thenReturn(List.of(new ReadingSignal(1,"Evidence","toread",null,Map.of(),"fiction",null)));
    var warnings=new ArrayList<RecommendationMetrics.Overdue>();
    var metrics=new RecommendationMetrics(new SimpleMeterRegistry(),batches,profiles,new ReadingProfileBuilder(),new RecommendationProperties(),clock,warnings::add);
    metrics.checkOverdue();page.set(List.of(batch(1,date,start)));metrics.checkOverdue();assertTrue(warnings.isEmpty());
    when(batches.now()).thenReturn(start.plusSeconds(3601));metrics.checkOverdue();assertEquals(1,warnings.size());
  }
  @Test void metersUseOnlyBoundedOutcomeOriginAndVersionTags(){
    var registry=new SimpleMeterRegistry();
    var metrics=new RecommendationMetrics(registry,batches,profiles,new ReadingProfileBuilder(),new RecommendationProperties(),clock,w->{});
    metrics.generation("READY",GenerationOrigin.AUTO,100000,30,1000);
    metrics.get("READY",500,start.minusSeconds(60));
    registry.getMeters().forEach(meter->meter.getId().getTags().forEach(tag->assertTrue(Set.of("outcome","origin","version","state").contains(tag.getKey()))));
    assertEquals(100000,registry.get("recommendations.catalog.analyzed").summary().totalAmount());
  }
  private RecommendationBatch batch(long id,LocalDate referenceDate,Instant pendingSince){
    return new RecommendationBatch(id,id,referenceDate,RecommendationBatchStatus.PENDING,"content-v1",0,null,null,null,0,0,null,null,start,pendingSince,null);
  }
}
