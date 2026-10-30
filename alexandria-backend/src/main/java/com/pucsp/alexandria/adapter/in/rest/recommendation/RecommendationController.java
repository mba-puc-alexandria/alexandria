package com.pucsp.alexandria.adapter.in.rest.recommendation;

import com.pucsp.alexandria.adapter.in.job.RecommendationMetrics;
import com.pucsp.alexandria.advice.ErrorResponse;
import com.pucsp.alexandria.application.recommendation.*;
import com.pucsp.alexandria.domain.recommendation.GenerationOrigin;
import com.pucsp.alexandria.domain.shared.valueobject.AuthenticatedUser;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/recommendations")
public class RecommendationController {
  private final GetRecommendationsUseCase get;
  private final RequestDailyRecommendationGenerationUseCase request;
  private final RecommendationMetrics metrics;
  public RecommendationController(GetRecommendationsUseCase get,RequestDailyRecommendationGenerationUseCase request,RecommendationMetrics metrics){this.get=get;this.request=request;this.metrics=metrics;}
  @GetMapping public ResponseEntity<?> get(Authentication authentication){
    var principal=(AuthenticatedUser)authentication.getPrincipal();long start=System.nanoTime();
    var result=get.execute(principal.id());
    if(result.response()==null){
      metrics.get("UNAVAILABLE",System.nanoTime()-start,null);
      return unavailable(result.unavailableStatus(),result.retryAfterSeconds());
    }
    var body=result.response();metrics.get(body.state(),System.nanoTime()-start,body.selection()==null?null:body.selection().generatedAt());
    return response("GENERATING".equals(body.state())?202:200,body.retryAfterSeconds()).body(body);
  }
  @PostMapping("/generation") public ResponseEntity<?> generate(Authentication authentication){
    var principal=(AuthenticatedUser)authentication.getPrincipal();
    var result=request.execute(principal.id(),GenerationOrigin.MANUAL);
    return switch(result.state()){
      case "READY","NO_PROFILE" -> response(200,null).body(new GenerationResponse(result.state(),result.referenceDate()));
      case "ACCEPTED","ALREADY_RUNNING" -> response(202,3).body(new GenerationResponse(result.state(),result.referenceDate()));
      case "COOLDOWN" -> response(429,result.retryAfterSeconds()).body(new ErrorResponse("Aguarde antes de tentar novamente",429));
      default -> unavailable(result.generationStatus(),result.retryAfterSeconds());
    };
  }
  public record GenerationResponse(String state,LocalDate referenceDate){}
  public record UnavailableResponse(String message,int status,LocalDateTime timestamp,String generationStatus,Integer retryAfterSeconds){}
  static ResponseEntity<?> unavailable(String generationStatus,Integer retryAfter){
    return response(503,retryAfter).body(new UnavailableResponse("Recomendações temporariamente indisponíveis",503,
        LocalDateTime.now(),"FAILED".equals(generationStatus)?"FAILED":"IDLE",retryAfter));
  }
  private static ResponseEntity.BodyBuilder response(int status,Integer retryAfter){
    var response=ResponseEntity.status(status).cacheControl(CacheControl.noStore());
    if(retryAfter!=null)response.header(HttpHeaders.RETRY_AFTER,retryAfter.toString());
    return response;
  }
}
