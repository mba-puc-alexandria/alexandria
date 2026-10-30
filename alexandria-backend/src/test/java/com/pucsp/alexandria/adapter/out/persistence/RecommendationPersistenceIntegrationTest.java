package com.pucsp.alexandria.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.*;
import com.pucsp.alexandria.adapter.out.persistence.entity.RecommendationBatchEntity;
import com.pucsp.alexandria.adapter.out.persistence.entity.RecommendationItemEntity;
import com.pucsp.alexandria.domain.recommendation.*;
import com.pucsp.alexandria.support.RecommendationFixtures;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.*;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.*;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.testcontainers.containers.MySQLContainer;

/** Real committed MySQL transactions, not the H2 application test profile. */
class RecommendationPersistenceIntegrationTest {
  static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0.32")
      .withDatabaseName("recommendations_test").withUsername("test").withPassword("local-test-only");
  static AnnotationConfigApplicationContext context;
  static DataSource source;
  static ObservingJdbcTemplate jdbc;
  static RecommendationBatchRepository batches;
  static CatalogFeatureRepository catalog;
  static ReadingProfileRepository profiles;
  static RecommendationReadPort reads;
  static final LocalDate TODAY = LocalDate.of(2026,10,30);

  @BeforeAll static void start() {
    MYSQL.start();
    source = new DriverManagerDataSource(MYSQL.getJdbcUrl(), MYSQL.getUsername(), MYSQL.getPassword());
    var result = Flyway.configure().dataSource(source).locations("classpath:db/migration").load().migrate();
    assertEquals(5, result.migrationsExecuted);
    context = new AnnotationConfigApplicationContext();
    context.register(TestConfiguration.class);
    context.registerBean(DataSource.class, () -> source);
    context.registerBean(RecommendationBatchRepositoryImpl.class);
    context.registerBean(CatalogFeatureRepositoryImpl.class);
    context.registerBean(ReadingProfileRepositoryImpl.class);
    context.registerBean(RecommendationReadAdapter.class);
    context.refresh();
    jdbc = (ObservingJdbcTemplate) context.getBean(JdbcTemplate.class);
    batches = context.getBean(RecommendationBatchRepository.class);
    catalog = context.getBean(CatalogFeatureRepository.class);
    profiles = context.getBean(ReadingProfileRepository.class);
    reads = context.getBean(RecommendationReadPort.class);
  }
  @AfterAll static void stop() { if(context!=null)context.close(); MYSQL.stop(); }
  @BeforeEach void seed() {
    jdbc.afterFirstCatalogPage = null;
    for(String table:List.of("recommendation_items","recommendation_batches","user_books","book_authors","authors","books","subscriptions","users")) jdbc.update("DELETE FROM "+table);
    jdbc.update("INSERT INTO users(id,username,first_name,last_name,email,password,role) VALUES (1,'a','A','A','a@test','test','USER'),(2,'b','B','B','b@test','test','USER')");
    jdbc.update("INSERT INTO books(id,title,source,download_url,subjects,languages,download_count) VALUES (1,'Evidência','GUTENDEX','test:1','Ficção','pt',0),(2,'Candidato','GUTENDEX','test:2','Ficção','pt',9),(3,'Outro','GUTENDEX','test:3','Ficção','en',0)");
    jdbc.update("INSERT INTO authors(id,name) VALUES (1,'Autora')");
    jdbc.update("INSERT INTO book_authors(book_id,author_id) VALUES (1,1),(2,1),(3,1)");
  }

  @Test void schemaValidatesJpaMappingsAndDatabaseConstraints() {
    var configuration = new Configuration().addAnnotatedClass(RecommendationBatchEntity.class).addAnnotatedClass(RecommendationItemEntity.class);
    configuration.getProperties().put("hibernate.connection.datasource", source);
    configuration.setProperty("hibernate.hbm2ddl.auto","validate");
    try(var factory=configuration.buildSessionFactory()) { assertTrue(factory.isOpen()); }
    var pending=batches.ensurePending(1,TODAY,"content-v1");
    assertCheckViolation(()->jdbc.update("UPDATE recommendation_batches SET selected_count=31 WHERE id=?",pending.id()),"ck_recommendation_selected_count");
    assertCheckViolation(()->jdbc.update("UPDATE recommendation_batches SET automatic_attempt_count=4 WHERE id=?",pending.id()),"ck_recommendation_attempts");
    assertThrows(DataIntegrityViolationException.class,()->jdbc.update("INSERT INTO recommendation_batches(user_id,reference_date,status,algorithm_version,created_at,updated_at) VALUES(1,?,'PENDING','v',UTC_TIMESTAMP(),UTC_TIMESTAMP())",TODAY));
    assertCheckViolation(()->jdbc.update("INSERT INTO recommendation_items(batch_id,position,book_id,reason_type,reason_value,reason_text,source_book_id,source_book_title,evidence_weight) VALUES (?,31,2,'SUBJECT','x','x',1,'x',4)",pending.id()),"ck_recommendation_position");
  }

  @Test void twoWorkersHaveOneClaimAndOnePublication() throws Exception {
    batches.ensurePending(1,TODAY,"content-v1");
    try(var executor=new ClosingExecutor(Executors.newFixedThreadPool(2))) {
      var start=new CountDownLatch(1);
      Callable<java.util.Optional<RecommendationBatch>> action=()->{start.await();return batches.claim(1,TODAY,TODAY,GenerationOrigin.AUTO,Duration.ofMinutes(5));};
      var first=executor.executor.submit(action);var second=executor.executor.submit(action);start.countDown();
      var claims=List.of(first.get(10,TimeUnit.SECONDS),second.get(10,TimeUnit.SECONDS));
      assertEquals(1,claims.stream().filter(java.util.Optional::isPresent).count());
      var claim=claims.stream().flatMap(java.util.Optional::stream).findFirst().orElseThrow();
      assertTrue(batches.publish(claim,List.of(RecommendationFixtures.candidate(2,1)),"a".repeat(64),"b".repeat(64)));
      assertFalse(batches.publish(claim,List.of(),null,null));
      assertTrue(batches.claim(1,TODAY,TODAY,GenerationOrigin.MANUAL,Duration.ofMinutes(5)).isEmpty());
      assertEquals(1,batches.find(1,TODAY).orElseThrow().attemptCount());
      assertEquals(1,reads.readyToday(1,TODAY).orElseThrow().items().size());
      assertTrue(reads.readyToday(2,TODAY).isEmpty());
    }
  }

  @Test void publicationRollbackNeverExposesPartialItems() {
    var claim=claim(1,TODAY);
    assertThrows(DataIntegrityViolationException.class,()->batches.publish(claim,List.of(
        RecommendationFixtures.candidate(2,1),RecommendationFixtures.candidate(999,1)),null,null));
    assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM recommendation_items",Integer.class));
    assertEquals(RecommendationBatchStatus.BUILDING,batches.find(1,TODAY).orElseThrow().status());
    assertTrue(reads.readyToday(1,TODAY).isEmpty());
    assertTrue(batches.publish(claim,List.of(),null,null));
    assertEquals(0,reads.readyToday(1,TODAY).orElseThrow().selectedCount());
  }

  @Test void expiredLeaseCannotResurrectAndNormalizationUsesObservedCas() {
    var initial=claim(1,TODAY);
    assertTrue(batches.heartbeat(initial,Duration.ofMinutes(10)));
    assertFalse(batches.normalizeExpired(initial));
    jdbc.update("UPDATE recommendation_batches SET lease_until=TIMESTAMPADD(SECOND,-600,UTC_TIMESTAMP(6)) WHERE id=?",initial.id());
    var expired=batches.find(1,TODAY).orElseThrow();
    assertFalse(batches.heartbeat(initial,Duration.ofMinutes(5)));
    assertFalse(batches.fail(initial,"TEST_FAILURE"));
    assertFalse(batches.publish(initial,List.of(),null,null));
    assertTrue(batches.normalizeExpired(expired));
    assertFalse(batches.normalizeExpired(expired));
    var replacement=batches.claim(1,TODAY,TODAY,GenerationOrigin.AUTO,Duration.ofMinutes(5)).orElseThrow();
    assertNotEquals(initial.leaseToken(),replacement.leaseToken());
    assertFalse(batches.normalizeExpired(expired));
    assertFalse(batches.fail(initial,"OLD_FAILURE"));
    assertTrue(batches.publish(replacement,List.of(),null,null));
  }

  @Test void autoBudgetCannotBeBypassedAndThirdCrashAllowsManualRecovery() {
    for(int attempt=1;attempt<=3;attempt++) {
      var claim=claim(1,TODAY);
      assertEquals(attempt,claim.automaticAttemptCount());
      if(attempt<3) {
        assertTrue(batches.fail(claim,"TEST_FAILURE"));
        jdbc.update("UPDATE recommendation_batches SET next_attempt_at=TIMESTAMPADD(SECOND,-1,UTC_TIMESTAMP(6)),failed_at=TIMESTAMPADD(SECOND,-600,UTC_TIMESTAMP(6)) WHERE id=?",claim.id());
      } else {
        jdbc.update("UPDATE recommendation_batches SET lease_until=TIMESTAMPADD(SECOND,-600,UTC_TIMESTAMP(6)) WHERE id=?",claim.id());
        assertTrue(batches.normalizeExpired(batches.find(1,TODAY).orElseThrow()));
      }
    }
    for(int i=0;i<100;i++) assertTrue(batches.claim(1,TODAY,TODAY,GenerationOrigin.AUTO,Duration.ofMinutes(5)).isEmpty());
    var failed=batches.find(1,TODAY).orElseThrow();
    assertEquals(RecommendationBatchStatus.FAILED,failed.status());
    assertEquals("LEASE_EXPIRED",failed.failureCode());
    var manual=batches.claim(1,TODAY,TODAY,GenerationOrigin.MANUAL,Duration.ofMinutes(5)).orElseThrow();
    assertEquals(3,manual.automaticAttemptCount());assertEquals(4,manual.attemptCount());
    assertTrue(batches.publish(manual,List.of(),null,null));
  }

  @Test void dormantReleaseIsTokenGuardedAndReactivationDoesNotResetAgeOnPolls() {
    var initial=claim(1,TODAY);
    assertTrue(batches.releaseNoProfile(initial,GenerationOrigin.AUTO));
    assertFalse(batches.releaseNoProfile(initial,GenerationOrigin.AUTO));
    var dormant=batches.find(1,TODAY).orElseThrow();
    assertEquals(0,dormant.automaticAttemptCount());assertEquals(1,dormant.attemptCount());assertNull(dormant.operationalPendingSince());
    jdbc.update("UPDATE recommendation_batches SET created_at=TIMESTAMPADD(DAY,-5,UTC_TIMESTAMP(6)) WHERE id=?",dormant.id());
    batches.reactivate(1,TODAY);
    var first=batches.find(1,TODAY).orElseThrow();
    batches.reactivate(1,TODAY);
    var second=batches.find(1,TODAY).orElseThrow();
    assertEquals(first.operationalPendingSince(),second.operationalPendingSince());
    assertTrue(first.operationalPendingSince().isAfter(first.createdAt()));
    assertEquals(0,second.automaticAttemptCount());
    batches.markDormant(1,TODAY);assertNull(batches.find(1,TODAY).orElseThrow().operationalPendingSince());
  }

  @Test void readyAndStaleAreFilteredAgainstCurrentLibraryAndAvailability() {
    var claim=claim(1,TODAY.minusDays(1));
    assertTrue(batches.publish(claim,List.of(RecommendationFixtures.candidate(2,1),RecommendationFixtures.candidate(3,1)),null,null));
    jdbc.update("INSERT INTO user_books(user_id,book_id,status) VALUES (1,2,'toread')");
    var stale=reads.latestReady(1,TODAY).orElseThrow();
    assertEquals(2,stale.selectedCount());assertEquals(List.of(3L),stale.items().stream().map(i->i.book().id()).toList());
    assertEquals(2,stale.items().get(0).position());
    jdbc.update("UPDATE books SET download_url=' ' WHERE id=3");
    assertTrue(reads.latestReady(1,TODAY).orElseThrow().items().isEmpty());
    jdbc.update("DELETE FROM user_books WHERE user_id=1 AND book_id=2");
    assertEquals(List.of(2L),reads.latestReady(1,TODAY).orElseThrow().items().stream().map(i->i.book().id()).toList());
    assertTrue(reads.latestReady(2,TODAY).isEmpty());
  }

  @Test void fullCatalogAndLibraryCrossAllPagesAndKeepOneConsistentSnapshot() throws Exception {
    for(int id=4;id<=605;id++) jdbc.update("INSERT INTO books(id,title,source,download_url,subjects,languages) VALUES (?,?,'GUTENDEX','test:x','Ficção','pt')",id,"Livro "+id);
    for(int id=1;id<=605;id++) jdbc.update("INSERT INTO user_books(user_id,book_id,status,rating) VALUES (1,?,'done',?)",id,id==605?0:5);
    try(var executor=new ClosingExecutor(Executors.newSingleThreadExecutor())) {
      jdbc.afterFirstCatalogPage=()->{
        try { executor.executor.submit(()->{
          jdbc.update("UPDATE books SET title='Alterado',subjects='Outro' WHERE id=605");
          jdbc.update("UPDATE authors SET name='Alterada' WHERE id=1");
        }).get(10,TimeUnit.SECONDS); }catch(Exception e){throw new RuntimeException(e);}
      };
      var all=catalog.loadAll();
      assertEquals(605,all.size());
      assertEquals("Livro 605",all.get(604).title());
      assertEquals("Autora",all.get(0).authors().get(1L));
      assertEquals("Alterado",jdbc.queryForObject("SELECT title FROM books WHERE id=605",String.class));
    }
    var library=profiles.load(1);
    assertEquals(605,library.size());assertEquals(0,library.get(604).rating());
    assertTrue(profiles.load(2).isEmpty());assertEquals(List.of(2L),profiles.userIdsAfter(1,500));
  }

  @Test void originalDateCannotBeClaimedAfterRolloverButStartedWorkerMayFinish() {
    batches.ensurePending(1,TODAY.minusDays(1),"content-v1");
    assertTrue(batches.claim(1,TODAY.minusDays(1),TODAY,GenerationOrigin.AUTO,Duration.ofMinutes(5)).isEmpty());
    var yesterday=batches.claim(1,TODAY.minusDays(1),TODAY.minusDays(1),GenerationOrigin.AUTO,Duration.ofMinutes(5)).orElseThrow();
    assertTrue(batches.publish(yesterday,List.of(),null,null));
    assertEquals(TODAY.minusDays(1),reads.latestReady(1,TODAY).orElseThrow().referenceDate());
  }

  @Test void retentionAndCascadesPreserveLastReadyAndActiveLeases() {
    for(int day=3;day>=0;day--)assertTrue(batches.publish(claim(1,TODAY.minusDays(day)),List.of(RecommendationFixtures.candidate(2,1)),null,null));
    var active=claim(2,TODAY.minusDays(10));
    batches.ensurePending(1,TODAY.minusDays(10),"content-v1");
    assertEquals(3,batches.retain(TODAY,100));
    assertEquals(2,jdbc.queryForObject("SELECT COUNT(*) FROM recommendation_batches WHERE user_id=1",Integer.class));
    assertTrue(batches.find(2,active.referenceDate()).isPresent());
    jdbc.update("UPDATE recommendation_batches SET lease_until=TIMESTAMPADD(SECOND,-1,UTC_TIMESTAMP(6)) WHERE id=?",active.id());
    assertEquals(1,batches.retain(TODAY,100));
    jdbc.update("DELETE FROM book_authors WHERE book_id=1");jdbc.update("DELETE FROM books WHERE id=1");
    assertNull(reads.readyToday(1,TODAY).orElseThrow().items().get(0).reason().sourceBookId());
    jdbc.update("DELETE FROM book_authors WHERE book_id=2");jdbc.update("DELETE FROM books WHERE id=2");
    assertTrue(reads.readyToday(1,TODAY).orElseThrow().items().isEmpty());
    assertEquals(1,reads.readyToday(1,TODAY).orElseThrow().selectedCount());
    jdbc.update("DELETE FROM users WHERE id=1");
    assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM recommendation_batches",Integer.class));
  }

  @Test void upgradeFromV3PreservesSeededUsersAndLibrary() {
    var root=new DriverManagerDataSource(MYSQL.getJdbcUrl(),"root",MYSQL.getPassword());
    var rootJdbc=new JdbcTemplate(root);rootJdbc.execute("CREATE DATABASE recommendation_upgrade");
    var upgraded=new DriverManagerDataSource(MYSQL.getJdbcUrl().replace("/recommendations_test","/recommendation_upgrade"),"root",MYSQL.getPassword());
    Flyway.configure().dataSource(upgraded).locations("classpath:db/migration").target("3").load().migrate();
    var old=new JdbcTemplate(upgraded);
    old.update("INSERT INTO users(id,username,first_name,last_name,email,password,role) VALUES (9,'existing','A','B','existing@test','test','USER')");
    old.update("INSERT INTO books(id,title,source,download_url) VALUES (9,'Existente','GUTENDEX','test:existing'),(10,'Em leitura','GUTENDEX','test:reading')");
    old.update("INSERT INTO user_books(user_id,book_id,status,rating,progress) VALUES (9,9,'done',0,NULL),(9,10,'reading',NULL,42)");
    var flyway=Flyway.configure().dataSource(upgraded).locations("classpath:db/migration").load();
    assertEquals(1,flyway.migrate().migrationsExecuted);flyway.validate();
    assertEquals("existing",old.queryForObject("SELECT username FROM users WHERE id=9",String.class));
    assertEquals(0,old.queryForObject("SELECT rating FROM user_books WHERE user_id=9 AND book_id=9",Integer.class));
    assertNull(old.queryForObject("SELECT progress FROM user_books WHERE user_id=9 AND book_id=9",Integer.class));
    assertEquals(42,old.queryForObject("SELECT progress FROM user_books WHERE user_id=9 AND book_id=10",Integer.class));
  }

  private RecommendationBatch claim(long user,LocalDate date) {
    batches.ensurePending(user,date,"content-v1");
    return batches.claim(user,date,date,GenerationOrigin.AUTO,Duration.ofMinutes(5)).orElseThrow();
  }

  private static void assertCheckViolation(Runnable action,String constraint) {
    var failure=assertThrows(DataAccessException.class,action::run);
    assertTrue(failure.getMostSpecificCause().getMessage().contains(constraint),failure::getMessage);
  }

  @org.springframework.boot.test.context.TestConfiguration(proxyBeanMethods=false)
  @EnableTransactionManagement
  static class TestConfiguration {
    @Bean JdbcTemplate jdbcTemplate(DataSource source){return new ObservingJdbcTemplate(source);}
    @Bean PlatformTransactionManager transactionManager(DataSource source){return new DataSourceTransactionManager(source);}
  }
  static class ObservingJdbcTemplate extends JdbcTemplate {
    volatile Runnable afterFirstCatalogPage;
    ObservingJdbcTemplate(DataSource source){super(source);}
    @Override public <T> List<T> query(String sql,RowMapper<T> mapper,Object...args) {
      var result=super.query(sql,mapper,args);
      if(afterFirstCatalogPage!=null && sql.contains("ORDER BY id LIMIT 500")) {
        var action=afterFirstCatalogPage;afterFirstCatalogPage=null;action.run();
      }
      return result;
    }
  }
  static class ClosingExecutor implements AutoCloseable {
    final ExecutorService executor;
    ClosingExecutor(ExecutorService executor){this.executor=executor;}
    @Override public void close(){executor.shutdownNow();}
  }
}
