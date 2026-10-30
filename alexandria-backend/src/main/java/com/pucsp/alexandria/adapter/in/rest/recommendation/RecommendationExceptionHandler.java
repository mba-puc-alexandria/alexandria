package com.pucsp.alexandria.adapter.in.rest.recommendation;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
@RestControllerAdvice(assignableTypes=RecommendationController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RecommendationExceptionHandler {
  @ExceptionHandler(Exception.class) public ResponseEntity<?> unavailable(Exception failure){
    return RecommendationController.unavailable("IDLE",3);
  }
}
