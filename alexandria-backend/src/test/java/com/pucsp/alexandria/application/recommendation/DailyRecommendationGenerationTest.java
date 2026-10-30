package com.pucsp.alexandria.application.recommendation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import com.pucsp.alexandria.domain.recommendation.*;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DailyRecommendationGenerationTest {
  final RecommendationBatchRepository batches=mock(RecommendationBatchRepository.class);
  final RecommendationReadPort reads=mock(RecommendationReadPort.class);
  final ReadingProfileRepository profiles=mock(ReadingProfileRepository.class);
  final RecommendationCatalogSnapshots snapshots=mock(RecommendationCatalogSnapshots.class);
  final RecommendationGenerationQueue queue=mock(RecommendationGenerationQueue.class);
  final RecommendationWorkFactory workFactory=mock(RecommendationWorkFactory.class);
  final Clock clock=Clock.fixed(Instant.parse("2026-10-30T02:59:59Z"),ZoneOffset.UTC);
  final LocalDate today=LocalDate.of(2026,10,29);
  RequestDailyRecommendationGenerationUseCase request;
  GetRecommendationsUseCase get;
  @BeforeEach void setUp(){
    when(reads.readyToday(anyLong(),any())).thenReturn(Optional.empty());
    when(reads.latestReady(anyLong(),any())).thenReturn(Optional.empty());
    when(batches.find(anyLong(),any())).thenReturn(Optional.empty());
    when(batches.now()).thenReturn(clock.instant());
    when(profiles.load(anyLong())).thenReturn(List.of(new ReadingSignal(1,"Origem","reading",null,Map.of(),"ficção","pt")));
    when(batches.ensurePending(anyLong(),any(),any())).thenReturn(batch(RecommendationBatchStatus.PENDING,0,null,null));
    var session=mock(RecommendationCatalogSnapshots.Session.class);var task=mock(RecommendationCatalogSnapshots.Task.class);
    when(snapshots.open(any())).thenReturn(session);when(session.register()).thenReturn(task);
    when(workFactory.create(anyLong(),any(),any(),any())).thenReturn(mock(RecommendationGenerationQueue.Work.class));
    when(queue.offer(anyLong(),any(),any(),any())).thenReturn(RecommendationGenerationQueue.OfferResult.ACCEPTED);
    request=new RequestDailyRecommendationGenerationUseCase(batches,reads,profiles,new ReadingProfileBuilder(),snapshots,queue,workFactory,clock,ZoneId.of("America/Sao_Paulo"));
    get=new GetRecommendationsUseCase(reads,request);
  }
  @Test void identityUsesSaoPauloAndPendingBeforeQueueWithoutAcquiringClaim(){
    var result=request.execute(7,GenerationOrigin.AUTO);
    assertEquals(today,result.referenceDate());assertEquals("ACCEPTED",result.state());
    var order=inOrder(batches,queue);order.verify(batches).ensurePending(7,today,"content-v1");
    order.verify(queue).offer(eq(7L),eq(today),eq(GenerationOrigin.AUTO),any());
    verify(batches,never()).claim(anyLong(),any(),any(),any(),any());
  }
  @Test void readyTodayIsReusedEvenWithNoCurrentPositiveProfile(){
    when(reads.readyToday(7,today)).thenReturn(Optional.of(selection(today,0)));
    assertEquals("EMPTY",get.execute(7).response().state());
    verifyNoInteractions(profiles,queue,snapshots);
  }
  @Test void noProfilePrecedesStaleAndDoesNotCreateReadyOrReadCatalog(){
    when(profiles.load(7)).thenReturn(List.of(new ReadingSignal(1,"Origem","done",0,Map.of(),"ficção","pt")));
    when(reads.latestReady(7,today)).thenReturn(Optional.of(selection(today.minusDays(1),1)));
    var response=get.execute(7).response();
    assertEquals("NO_PROFILE",response.emptyReason());assertNull(response.selection());
    verifyNoInteractions(queue,snapshots);verify(batches,never()).ensurePending(anyLong(),any(),any());
  }
  @Test void failedAutoBudgetCannotBeBypassedByOneHundredGets(){
    var failed=batch(RecommendationBatchStatus.FAILED,3,clock.instant().minusSeconds(120),null);
    when(batches.ensurePending(anyLong(),any(),any())).thenReturn(failed);
    when(batches.find(7,today)).thenReturn(Optional.of(failed));
    for(int i=0;i<100;i++)assertEquals("FAILED",get.execute(7).unavailableStatus());
    verifyNoInteractions(queue,snapshots);
  }
  @Test void staleCanBeEmptyWhileFailureOffersManualRetry(){
    var failed=batch(RecommendationBatchStatus.FAILED,3,clock.instant().minusSeconds(30),null);
    when(batches.ensurePending(anyLong(),any(),any())).thenReturn(failed);
    when(batches.find(7,today)).thenReturn(Optional.of(failed));
    when(reads.latestReady(7,today)).thenReturn(Optional.of(selection(today.minusDays(1),0)));
    var result=get.execute(7).response();assertEquals("STALE",result.state());
    assertEquals("FAILED",result.generationStatus());assertEquals(30,result.retryAfterSeconds());assertTrue(result.items().isEmpty());
    assertEquals("COOLDOWN",request.execute(7,GenerationOrigin.MANUAL).state());
  }
  @Test void expiredLeaseNormalizesBeforeBudgetCheckAndManualCanRecover(){
    var expired=batch(RecommendationBatchStatus.BUILDING,3,null,clock.instant().minusSeconds(120));
    var failed=batch(RecommendationBatchStatus.FAILED,3,clock.instant().minusSeconds(120),null);
    when(batches.ensurePending(anyLong(),any(),any())).thenReturn(expired);
    when(batches.find(7,today)).thenReturn(Optional.of(failed));when(batches.normalizeExpired(expired)).thenReturn(true);
    var response=request.execute(7,GenerationOrigin.MANUAL);
    assertEquals("ACCEPTED",response.state());verify(batches).normalizeExpired(expired);
    verify(queue).offer(eq(7L),eq(today),eq(GenerationOrigin.MANUAL),any());
  }
  @Test void queueRejectionAndCoalescingCloseUnusedWorkRegistration(){
    when(queue.offer(anyLong(),any(),any(),any())).thenReturn(RecommendationGenerationQueue.OfferResult.FULL);
    var result=request.execute(7,GenerationOrigin.AUTO);assertEquals("IDLE",result.generationStatus());
    verify(workFactory.create(7,today,GenerationOrigin.AUTO,snapshots.open(today).register())).close();
  }
  @Test void workerWithNoProfileReleasesDormantWithoutCatalogOrPublication(){
    when(profiles.load(7)).thenReturn(List.of());var task=mock(RecommendationCatalogSnapshots.Task.class);
    var generate=new GenerateDailyRecommendationsUseCase(batches,profiles,new ReadingProfileBuilder(),new RecommendationSelector(new RecommendationScorer()));
    var claim=batch(RecommendationBatchStatus.BUILDING,1,null,clock.instant().plusSeconds(300));
    assertEquals("NO_PROFILE",generate.execute(claim,GenerationOrigin.AUTO,task,()->true).state());
    verify(batches).releaseNoProfile(claim,GenerationOrigin.AUTO);verifyNoInteractions(task);
    verify(batches,never()).publish(any(),anyList(),anyString(),anyString());
  }
  @Test void unexpectedQueueFailureClosesWorkWithoutLeakingRegisteredSnapshot(){
    when(queue.offer(anyLong(),any(),any(),any())).thenThrow(new IllegalStateException("queue failed"));
    var session=snapshots.open(today);var task=session.register();var work=workFactory.create(7,today,GenerationOrigin.AUTO,task);
    assertThrows(IllegalStateException.class,()->request.execute(7,GenerationOrigin.AUTO));
    verify(work).close();verify(session).close();
  }
  private RecommendationBatch batch(RecommendationBatchStatus status,int attempts,Instant failedAt,Instant leaseUntil){
    return new RecommendationBatch(1,7,today,status,"content-v1",0,null,"token",leaseUntil,attempts,attempts,failedAt,null,clock.instant(),clock.instant(),null);
  }
  private static DailySelection selection(LocalDate date,int selected){return new DailySelection(1,7,date,Instant.parse("2026-10-29T03:00:00Z"),"content-v1",selected,List.of());}
}
