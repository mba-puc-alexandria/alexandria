package com.pucsp.alexandria.adapter.in.rest;

import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.pucsp.alexandria.application.recommendation.*;
import com.pucsp.alexandria.application.recommendation.dto.RecommendationsResponse;
import com.pucsp.alexandria.config.jwt.JwtTokenProvider;
import com.pucsp.alexandria.domain.recommendation.GenerationOrigin;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties={"recommendations.jobs-enabled=false","cors.allowed-origins=http://localhost:3100"})
@AutoConfigureMockMvc
class RecommendationControllerIntegrationTest {
  @Autowired MockMvc mvc;
  @MockitoBean GetRecommendationsUseCase get;
  @MockitoBean RequestDailyRecommendationGenerationUseCase request;
  @MockitoBean JwtTokenProvider jwt;
  final LocalDate date=LocalDate.of(2026,10,30);
  @BeforeEach void auth(){
    when(jwt.validateToken("a")).thenReturn(true);when(jwt.getUsernameFromToken("a")).thenReturn("reader-a");when(jwt.getUserIdFromToken("a")).thenReturn(7L);when(jwt.getRoleFromToken("a")).thenReturn("USER");
    when(jwt.validateToken("b")).thenReturn(true);when(jwt.getUsernameFromToken("b")).thenReturn("reader-b");when(jwt.getUserIdFromToken("b")).thenReturn(8L);when(jwt.getRoleFromToken("b")).thenReturn("USER");
  }
  @Test void jwtOwnsIdentityAndEmptyProfileNeverExposesPrivateMetadata() throws Exception {
    when(get.execute(7)).thenReturn(new GetRecommendationsUseCase.Result(RecommendationsResponse.noProfile(),null,null));
    mvc.perform(get("/recommendations?userId=8").header("Authorization","Bearer a"))
      .andExpect(status().isOk()).andExpect(header().string("Cache-Control","no-store"))
      .andExpect(jsonPath("$.generationStatus").value("NO_PROFILE")).andExpect(jsonPath("$.selection").isEmpty())
      .andExpect(jsonPath("$.items").isEmpty()).andExpect(jsonPath("$.emptyReason").value("NO_PROFILE"))
      .andExpect(jsonPath("$.userId").doesNotExist()).andExpect(jsonPath("$.profileFingerprint").doesNotExist());
    verify(get).execute(7);verify(get,never()).execute(8);
  }
  @Test void anotherAccountUsesItsPrincipal() throws Exception {
    when(get.execute(8)).thenReturn(new GetRecommendationsUseCase.Result(RecommendationsResponse.noProfile(),null,null));
    mvc.perform(get("/recommendations").header("Authorization","Bearer b")).andExpect(status().isOk());verify(get).execute(8);
  }
  @Test void unauthenticatedIsRejectedWithoutAssumingJsonBody() throws Exception {
    mvc.perform(get("/recommendations")).andExpect(status().isUnauthorized());
    mvc.perform(post("/recommendations/generation")).andExpect(status().isUnauthorized());verifyNoInteractions(get,request);
  }
  @Test void generatingUses202AndBrowserCanReadRetryAfter() throws Exception {
    when(get.execute(7)).thenReturn(new GetRecommendationsUseCase.Result(RecommendationsResponse.generating("PENDING"),null,null));
    mvc.perform(get("/recommendations").header("Authorization","Bearer a").header("Origin","http://localhost:3100"))
      .andExpect(status().isAccepted()).andExpect(header().string("Retry-After","3"))
      .andExpect(header().string("Access-Control-Expose-Headers","Retry-After"))
      .andExpect(jsonPath("$.generationStatus").value("PENDING"));
  }
  @Test void failureHasDedicated503WithNullableCooldownAndNoStore() throws Exception {
    when(get.execute(7)).thenReturn(new GetRecommendationsUseCase.Result(null,"FAILED",null));
    mvc.perform(get("/recommendations").header("Authorization","Bearer a"))
      .andExpect(status().isServiceUnavailable()).andExpect(header().string("Cache-Control","no-store"))
      .andExpect(header().doesNotExist("Retry-After")).andExpect(jsonPath("$.generationStatus").value("FAILED"))
      .andExpect(jsonPath("$.retryAfterSeconds").isEmpty()).andExpect(jsonPath("$.status").value(503));
  }
  @Test void queueUnavailableAndUnexpectedFailureDoNotLeakExceptionDetails() throws Exception {
    when(get.execute(7)).thenThrow(new IllegalStateException("private-user-and-token"));
    mvc.perform(get("/recommendations").header("Authorization","Bearer a"))
      .andExpect(status().isServiceUnavailable()).andExpect(header().string("Cache-Control","no-store"))
      .andExpect(jsonPath("$.generationStatus").value("IDLE"))
      .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("private-user"))));
  }
  @Test void manualRetryBeforeCooldownIs429AndNeverAuto() throws Exception {
    when(request.execute(7,GenerationOrigin.MANUAL)).thenReturn(new RequestDailyRecommendationGenerationUseCase.Result("COOLDOWN",date,"FAILED",40));
    mvc.perform(post("/recommendations/generation").header("Authorization","Bearer a"))
      .andExpect(status().isTooManyRequests()).andExpect(header().string("Retry-After","40")).andExpect(header().string("Cache-Control","no-store"));
    verify(request).execute(7,GenerationOrigin.MANUAL);
  }
  @Test void postReadyIsIdempotentAndAcceptedHasOnlyPublicFields() throws Exception {
    when(request.execute(7,GenerationOrigin.MANUAL)).thenReturn(new RequestDailyRecommendationGenerationUseCase.Result("READY",date,"READY",null));
    mvc.perform(post("/recommendations/generation").header("Authorization","Bearer a"))
      .andExpect(status().isOk()).andExpect(jsonPath("$.state").value("READY"))
      .andExpect(jsonPath("$.referenceDate").value("2026-10-30")).andExpect(jsonPath("$.generationStatus").doesNotExist());
    when(request.execute(7,GenerationOrigin.MANUAL)).thenReturn(new RequestDailyRecommendationGenerationUseCase.Result("ACCEPTED",date,"PENDING",3));
    mvc.perform(post("/recommendations/generation").header("Authorization","Bearer a"))
      .andExpect(status().isAccepted()).andExpect(header().string("Retry-After","3"));
  }
}
