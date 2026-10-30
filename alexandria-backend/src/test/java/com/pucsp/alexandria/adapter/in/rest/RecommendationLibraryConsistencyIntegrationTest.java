package com.pucsp.alexandria.adapter.in.rest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.pucsp.alexandria.adapter.out.persistence.UserBooksRepositoryImpl;
import com.pucsp.alexandria.application.recommendation.RequestDailyRecommendationGenerationUseCase;
import com.pucsp.alexandria.config.jwt.JwtTokenProvider;
import com.pucsp.alexandria.domain.book.BookId;
import com.pucsp.alexandria.domain.recommendation.*;
import com.pucsp.alexandria.domain.user.UserId;
import com.pucsp.alexandria.support.RecommendationFixtures;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.MySQLContainer;

/** HTTP requests with real JWT, JPA, Flyway and separately committed MySQL transactions. */
@SpringBootTest(properties={"recommendations.jobs-enabled=false", "spring.jpa.hibernate.ddl-auto=validate",
    "spring.flyway.enabled=true", "spring.flyway.baseline-on-migrate=false",
    "spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect"})
@AutoConfigureMockMvc
@DirtiesContext(classMode=DirtiesContext.ClassMode.AFTER_CLASS)
class RecommendationLibraryConsistencyIntegrationTest {
  static final MySQLContainer<?> MYSQL=new MySQLContainer<>("mysql:8.0.32")
      .withDatabaseName("recommendation_library").withUsername("test").withPassword("local-test-only");
  @DynamicPropertySource static void mysql(DynamicPropertyRegistry registry) {
    MYSQL.start();
    registry.add("spring.datasource.url",MYSQL::getJdbcUrl);
    registry.add("spring.datasource.username",MYSQL::getUsername);
    registry.add("spring.datasource.password",MYSQL::getPassword);
    registry.add("spring.datasource.driver-class-name",()->"com.mysql.cj.jdbc.Driver");
  }
  @AfterAll static void stop(){MYSQL.stop();}
  @Autowired MockMvc mvc;
  @Autowired JdbcTemplate jdbc;
  @Autowired JwtTokenProvider jwt;
  @Autowired RecommendationBatchRepository batches;
  @Autowired RequestDailyRecommendationGenerationUseCase generation;
  @MockitoSpyBean UserBooksRepositoryImpl library;
  String token;
  LocalDate today;

  @BeforeEach void seed() {
    for(String table:List.of("recommendation_items","recommendation_batches","user_books","book_authors","authors","books","subscriptions","users"))jdbc.update("DELETE FROM "+table);
    jdbc.update("INSERT INTO users(id,username,first_name,last_name,email,password,role) VALUES (1,'reader','A','B','reader@test','test','USER'),(2,'other','A','B','other@test','test','USER')");
    jdbc.update("INSERT INTO books(id,title,source,download_url,subjects,languages) VALUES (1,'Evidência','GUTENDEX','test:1','Ficção','pt'),(2,'Candidato','GUTENDEX','test:2','Ficção','pt'),(3,'Outro','GUTENDEX','test:3','Ficção','pt')");
    jdbc.update("INSERT INTO authors(id,name) VALUES (1,'Autora de ensaio')");
    jdbc.update("INSERT INTO book_authors(book_id,author_id) VALUES (1,1),(2,1),(3,1)");
    jdbc.update("INSERT INTO user_books(user_id,book_id,status,rating,progress) VALUES (1,1,'done',5,NULL)");
    token=jwt.generateToken(1L,"reader","USER");today=generation.today();
  }

  @Test void simultaneousAddsHitUniqueConstraintAndProduceOneLinkAndOne409() throws Exception {
    var precheckBarrier=new CyclicBarrier(2);
    doAnswer(invocation->{
      boolean exists=(boolean)invocation.callRealMethod();
      assertFalse(exists,"Both requests must pass the precheck before either insert");
      precheckBarrier.await(10,TimeUnit.SECONDS);
      return false;
    }).when(library).existsByUserIdAndBookId(any(UserId.class),any(BookId.class));
    var workers=Executors.newFixedThreadPool(2);
    try {
      Callable<MvcResult> action=()->add(2);
      var a=workers.submit(action);var b=workers.submit(action);
      var responses=List.of(a.get(20,TimeUnit.SECONDS),b.get(20,TimeUnit.SECONDS));
      assertEquals(List.of(201,409),responses.stream().map(r->r.getResponse().getStatus()).sorted().toList());
      assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM user_books WHERE user_id=1 AND book_id=2",Integer.class));
      assertEquals("toread",jdbc.queryForObject("SELECT status FROM user_books WHERE user_id=1 AND book_id=2",String.class));
    } finally {workers.shutdownNow();}
  }

  @Test void committedAddsFilterStaleAndReadyWithoutReplacementOrChangingOriginalCount() throws Exception {
    var yesterday=claim(today.minusDays(1));
    assertTrue(batches.publish(yesterday,List.of(RecommendationFixtures.candidate(2,1),RecommendationFixtures.candidate(3,1)),null,null));
    var pending=batches.ensurePending(1,today,"content-v1");
    jdbc.update("UPDATE recommendation_batches SET status='FAILED',automatic_attempt_count=3,attempt_count=3,failed_at=TIMESTAMPADD(SECOND,-600,UTC_TIMESTAMP(6)),next_attempt_at=NULL WHERE id=?",pending.id());
    assertEquals(201,add(2).getResponse().getStatus());
    mvc.perform(get("/recommendations").header("Authorization","Bearer "+token))
        .andExpect(status().isOk()).andExpect(jsonPath("$.state").value("STALE"))
        .andExpect(jsonPath("$.selection.selectedCount").value(2)).andExpect(jsonPath("$.items.length()").value(1))
        .andExpect(jsonPath("$.items[0].book.id").value(3)).andExpect(jsonPath("$.items[0].position").value(2));
    var current=batches.claim(1,today,today,GenerationOrigin.MANUAL,Duration.ofMinutes(5)).orElseThrow();
    assertTrue(batches.publish(current,List.of(RecommendationFixtures.candidate(2,1),RecommendationFixtures.candidate(3,1)),null,null));
    mvc.perform(get("/recommendations").header("Authorization","Bearer "+token))
        .andExpect(status().isOk()).andExpect(jsonPath("$.state").value("READY"))
        .andExpect(jsonPath("$.selection.selectedCount").value(2)).andExpect(jsonPath("$.items.length()").value(1))
        .andExpect(jsonPath("$.items[0].book.id").value(3));
    assertEquals(201,add(3).getResponse().getStatus());
    mvc.perform(get("/recommendations").header("Authorization","Bearer "+token))
        .andExpect(status().isOk()).andExpect(jsonPath("$.state").value("EMPTY"))
        .andExpect(jsonPath("$.emptyReason").value("ALL_ADDED_OR_UNAVAILABLE"))
        .andExpect(jsonPath("$.selection.selectedCount").value(2)).andExpect(jsonPath("$.items").isEmpty());
    var other=jwt.generateToken(2L,"other","USER");
    mvc.perform(get("/recommendations").header("Authorization","Bearer "+other))
        .andExpect(status().isOk()).andExpect(jsonPath("$.emptyReason").value("NO_PROFILE"))
        .andExpect(jsonPath("$.selection").isEmpty()).andExpect(jsonPath("$.items").isEmpty());
  }

  @Test void missingBookHasSpecificCodeWhileGenericLibrary404KeepsExistingContract() throws Exception {
    mvc.perform(post("/user-books").header("Authorization","Bearer "+token).contentType(MediaType.APPLICATION_JSON)
        .content("{\"bookId\":999,\"status\":\"TOREAD\"}"))
        .andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("BOOK_NOT_FOUND"))
        .andExpect(jsonPath("$.message").exists()).andExpect(jsonPath("$.timestamp").exists());
    mvc.perform(delete("/user-books/999").header("Authorization","Bearer "+token))
        .andExpect(status().isNotFound()).andExpect(jsonPath("$.code").doesNotExist())
        .andExpect(jsonPath("$.message").exists()).andExpect(jsonPath("$.timestamp").exists());
  }

  private MvcResult add(long bookId) throws Exception {
    return mvc.perform(post("/user-books").header("Authorization","Bearer "+token)
        .contentType(MediaType.APPLICATION_JSON).content("{\"bookId\":"+bookId+",\"status\":\"TOREAD\"}")).andReturn();
  }
  private RecommendationBatch claim(LocalDate date) {
    batches.ensurePending(1,date,"content-v1");
    return batches.claim(1,date,date,GenerationOrigin.AUTO,Duration.ofMinutes(5)).orElseThrow();
  }
}
