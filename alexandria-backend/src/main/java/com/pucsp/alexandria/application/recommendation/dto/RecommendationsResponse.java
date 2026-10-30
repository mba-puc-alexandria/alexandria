package com.pucsp.alexandria.application.recommendation.dto;

import com.pucsp.alexandria.domain.recommendation.DailySelection;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/** Public representation intentionally excludes ownership, fingerprints and evidence IDs. */
public record RecommendationsResponse(String state,Selection selection,List<Item> items,String emptyReason,
    boolean updatePending,Integer retryAfterSeconds,String message,String generationStatus) {
  public RecommendationsResponse {items=List.copyOf(items);}
  public record Selection(long id,LocalDate referenceDate,Instant generatedAt,String algorithmVersion,int selectedCount){}
  public record Item(int position,DailySelection.Book book,Reason reason){}
  public record Reason(String type,String matchedValue,String text){}
  public static RecommendationsResponse from(DailySelection value,boolean stale,String generationStatus,Integer retryAfter){
    var metadata=new Selection(value.id(),value.referenceDate(),value.generatedAt(),value.algorithmVersion(),value.selectedCount());
    var items=value.items().stream().map(item->new Item(item.position(),item.book(),
        new Reason(item.reason().type(),item.reason().matchedValue(),item.reason().text()))).toList();
    String empty=stale||!items.isEmpty()?null:value.selectedCount()==0?"NO_MATCHES":"ALL_ADDED_OR_UNAVAILABLE";
    return new RecommendationsResponse(stale?"STALE":items.isEmpty()?"EMPTY":"READY",metadata,items,empty,stale,retryAfter,
        stale?"A atualização de hoje está pendente. Mostrando a última seleção disponível.":null,generationStatus);
  }
  public static RecommendationsResponse noProfile(){
    return new RecommendationsResponse("EMPTY",null,List.of(),"NO_PROFILE",false,null,
        "Ainda não encontramos interesses suficientes para recomendar. Explore o catálogo e adicione livros que quer ler.","NO_PROFILE");
  }
  public static RecommendationsResponse generating(String status){
    return new RecommendationsResponse("GENERATING",null,List.of(),null,true,3,"Preparando suas recomendações",status);
  }
}
