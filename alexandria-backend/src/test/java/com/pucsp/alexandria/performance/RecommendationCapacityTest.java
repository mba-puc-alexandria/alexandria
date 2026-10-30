package com.pucsp.alexandria.performance;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pucsp.alexandria.adapter.in.job.DailyRecommendationDispatcher;
import com.pucsp.alexandria.adapter.in.job.RecommendationExecutionResources;
import com.pucsp.alexandria.adapter.out.persistence.RecommendationReadAdapter;
import com.pucsp.alexandria.application.recommendation.RequestDailyRecommendationGenerationUseCase;
import com.pucsp.alexandria.config.RecommendationProperties;
import com.pucsp.alexandria.config.jwt.JwtTokenProvider;
import com.pucsp.alexandria.domain.recommendation.*;
import com.pucsp.alexandria.support.RecommendationCapacityFixture;
import io.micrometer.core.instrument.MeterRegistry;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MySQLContainer;

/** Opt-in engineering experiment. Browser paint/SC-006 is measured by the separate Playwright run. */
@EnabledIfSystemProperty(named="recommendations.capacity",matches="true")
@SpringBootTest(properties={"recommendations.jobs-enabled=false","spring.jpa.hibernate.ddl-auto=validate",
    "spring.flyway.enabled=true","spring.flyway.baseline-on-migrate=false",
    "spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect","spring.datasource.hikari.maximum-pool-size=60"})
@AutoConfigureMockMvc
@DirtiesContext(classMode=DirtiesContext.ClassMode.AFTER_CLASS)
class RecommendationCapacityTest {
  static final MySQLContainer<?> MYSQL=new MySQLContainer<>("mysql:8.0.32")
      .withDatabaseName("recommendation_capacity").withUsername("test").withPassword("local-test-only");
  @DynamicPropertySource static void mysql(DynamicPropertyRegistry registry) {
    MYSQL.start();registry.add("spring.datasource.url",MYSQL::getJdbcUrl);
    registry.add("spring.datasource.username",MYSQL::getUsername);registry.add("spring.datasource.password",MYSQL::getPassword);
    registry.add("spring.datasource.driver-class-name",()->"com.mysql.cj.jdbc.Driver");
  }
  @AfterAll static void stop(){MYSQL.stop();}
  @Autowired JdbcTemplate jdbc;
  @Autowired CatalogFeatureRepository catalog;
  @Autowired ReadingProfileRepository profiles;
  @Autowired ReadingProfileBuilder builder;
  @Autowired RecommendationReadPort reads;
  @Autowired RecommendationBatchRepository batches;
  @Autowired RecommendationCatalogSnapshots snapshots;
  @Autowired RequestDailyRecommendationGenerationUseCase request;
  @Autowired RecommendationExecutionResources resources;
  @Autowired RecommendationProperties properties;
  @Autowired MeterRegistry meters;
  @Autowired MockMvc mvc;
  @Autowired JwtTokenProvider jwt;
  @Autowired ObjectMapper json;
  @Autowired javax.sql.DataSource dataSource;

  @Test @Timeout(value=70,unit=TimeUnit.MINUTES)
  void measuresFullCatalogGenerationAndFiftyConcurrentReaders() throws Exception {
    Map<String,Object> report=new LinkedHashMap<>();
    report.put("experiment","engineering-capacity-not-browser-SC006");
    Map<String,Object> environment=new LinkedHashMap<>(Map.of("java",System.getProperty("java.version"),"os",System.getProperty("os.name"),
        "arch",System.getProperty("os.arch"),"processors",Runtime.getRuntime().availableProcessors(),
        "maxHeapBytes",Runtime.getRuntime().maxMemory(),"mysqlImage","mysql:8.0.32"));
    environment.put("hikariMaxPoolSize",dataSource instanceof com.zaxxer.hikari.HikariDataSource hikari?hikari.getMaximumPoolSize():null);
    report.put("environment",environment);
    try {
      report.put("seed",RecommendationCapacityFixture.seed(jdbc));
      assertEquals(RecommendationCapacityFixture.BOOKS,jdbc.queryForObject("SELECT COUNT(*) FROM books",Integer.class));
      assertEquals(1_000_000L,jdbc.queryForObject("SELECT COUNT(*) FROM user_books",Long.class));
      LocalDate date=request.today();report.put("referenceDate",date.toString());
      var databaseBefore=databaseCounters();long cpuBefore=cpuTime();var gcBefore=gcCounters();
      try(var memory=new MemoryWindow()) {
        long started=System.nanoTime();
        new DailyRecommendationDispatcher(request,profiles,snapshots,resources,properties).trigger();
        long deadline=System.nanoTime()+Duration.ofMinutes(60).toNanos();
        while(true) {
          int ready=jdbc.queryForObject("SELECT COUNT(*) FROM recommendation_batches WHERE reference_date=? AND status='READY'",Integer.class,date);
          int failed=jdbc.queryForObject("SELECT COUNT(*) FROM recommendation_batches WHERE reference_date=? AND status='FAILED'",Integer.class,date);
          assertEquals(0,failed,"Generation failed; inspect sanitized failure_code instead of discarding the failure");
          if(ready==RecommendationCapacityFixture.READERS&&resources.active()==0&&resources.queued()==0)break;
          assertTrue(System.nanoTime()<deadline,"Daily generation exceeded the 60 minute engineering target");
          Thread.sleep(500);
        }
        long duration=(System.nanoTime()-started)/1_000_000;
        report.put("generation",Map.of("durationMillis",duration,"readyReaders",RecommendationCapacityFixture.READERS,
            "catalogSize",RecommendationCapacityFixture.BOOKS,"workers",properties.getWorkers(),"queueCapacity",properties.getQueueCapacity()));
        report.put("generationMemory",memory.measurement());
      }
      report.put("generationProcessCpuNanos",difference(cpuBefore,cpuTime()));
      report.put("generationGcDelta",delta(gcBefore,gcCounters()));
      report.put("generationDatabaseCounterDelta",delta(databaseBefore,databaseCounters()));
      var analyzed=meters.get("recommendations.catalog.analyzed").summary();
      assertEquals(RecommendationCapacityFixture.READERS,analyzed.count());
      assertEquals((double)RecommendationCapacityFixture.BOOKS*RecommendationCapacityFixture.READERS,analyzed.totalAmount());
      report.put("observedAnalyzedBooks",analyzed.totalAmount());
      report.put("crossDateProjectionOverlap",measureCrossDateProjectionOverlap(date));
      var fullCatalog=catalog.loadAll();assertEquals(RecommendationCapacityFixture.BOOKS,fullCatalog.size());
      for(long user:List.of(1L,500L,1000L)) {
        var profile=builder.build(profiles.load(user));
        var actual=reads.readyToday(user,date).orElseThrow();
        assertEquals(referenceTop30(fullCatalog,profile),actual.items().stream().map(item->item.book().id()).toList());
        assertTrue(actual.items().stream().allMatch(item->profile.excludedBookIds().contains(item.reason().sourceBookId())));
      }
      report.put("fullSortReferenceReaders",List.of(1,500,1000));
      report.put("explainFilteredRead",jdbc.queryForList("""
          EXPLAIN SELECT ri.book_id FROM recommendation_items ri JOIN books b ON b.id=ri.book_id
          WHERE ri.batch_id=(SELECT id FROM recommendation_batches WHERE user_id=1 AND reference_date=?)
            AND b.download_url IS NOT NULL AND TRIM(b.download_url)<>''
            AND NOT EXISTS(SELECT 1 FROM user_books ub WHERE ub.user_id=1 AND ub.book_id=ri.book_id)
          ORDER BY ri.position
          """,date));
      report.put("explainReadyLookup",jdbc.queryForList("EXPLAIN SELECT * FROM recommendation_batches WHERE user_id=1 AND status='READY' AND reference_date=?",date));
      report.put("concurrentHttpReads",concurrentReads());
      for(int age=3;age>=1;age--) {
        var old=date.minusDays(age);batches.ensurePending(1,old,RecommendationSelector.ALGORITHM_VERSION);
        var claim=batches.claim(1,old,old,GenerationOrigin.AUTO,Duration.ofMinutes(5)).orElseThrow();
        assertTrue(batches.publish(claim,List.of(),null,null));
      }
      assertEquals(2,batches.retain(date,100));
      assertEquals(2,jdbc.queryForObject("SELECT COUNT(*) FROM recommendation_batches WHERE user_id=1 AND status='READY'",Integer.class));
      // A fresh adapter/connection reads persisted state. This is deliberately not called a process restart.
      var fresh=new RecommendationReadAdapter(jdbc).readyToday(1,date).orElseThrow();
      assertEquals(reads.readyToday(1,date).orElseThrow().generatedAt(),fresh.generatedAt());
      assertEquals(date,reads.latestReady(1,date.plusDays(1)).orElseThrow().referenceDate());
      report.put("retentionAndFreshAdapterReadVerified",true);
      report.put("processCrashRestartMeasuredUnderCapacity",false);
      report.put("limits",List.of("Backend MockMvc latency does not measure browser paint or SC-006",
          "Full-sort reference checks three representative deterministic readers, not all one thousand",
          "Process crash/restart under capacity requires the separate server lifecycle experiment",
          "Cross-date projection overlap uses a separate memory window outside job duration; existing JVM state remains, with no forced GC",
          "RSS sampled at window boundaries; no claim of a complete instantaneous RSS peak"));
      report.put("passed",true);
    } catch(Exception|AssertionError failure) {
      report.put("passed",false);report.put("failure",failure.getClass().getSimpleName()+": "+failure.getMessage());throw failure;
    } finally {
      Path artifact=Path.of("target","recommendation-capacity","backend-results.json");
      Files.createDirectories(artifact.getParent());json.writerWithDefaultPrettyPrinter().writeValue(artifact.toFile(),report);
      System.out.println("CAPACITY_RESULT "+artifact.toAbsolutePath());
    }
  }

  private Map<String,Object> measureCrossDateProjectionOverlap(LocalDate date) {
    Map<String,Object> result=new LinkedHashMap<>();
    try(var memory=new MemoryWindow();var previous=snapshots.open(date);var previousTask=previous.register();
        var previousHandle=previousTask.acquire()) {
      assertEquals(RecommendationCapacityFixture.BOOKS,previousHandle.books().size());
      result.put("oneProjectionMemory",memory.measurement());
      try(var current=snapshots.open(date.plusDays(1));var currentTask=current.register();var currentHandle=currentTask.acquire()) {
        assertEquals(RecommendationCapacityFixture.BOOKS,currentHandle.books().size());
        assertNotSame(previousHandle.books(),currentHandle.books());
        assertNotSame(previousHandle.books().get(0),currentHandle.books().get(0));
        // Both owners, tasks and handles remain live while heap/RSS are sampled.
        result.put("twoProjectionMemory",memory.measurement());
        assertEquals(RecommendationCapacityFixture.BOOKS,previousHandle.books().size());
        assertEquals(RecommendationCapacityFixture.BOOKS,currentHandle.books().size());
        result.put("simultaneousProjections",2);result.put("catalogBooksPerProjection",RecommendationCapacityFixture.BOOKS);
        result.put("simultaneousCatalogEntries",2L*RecommendationCapacityFixture.BOOKS);
        result.put("referenceDates",List.of(date.toString(),date.plusDays(1).toString()));
        result.put("includedInDailyJobDuration",false);
      }
    }
    return result;
  }

  private Map<String,Object> concurrentReads() throws Exception {
    var latencies=Collections.synchronizedList(new ArrayList<Double>());
    var workers=Executors.newFixedThreadPool(50);var start=new CountDownLatch(1);var futures=new ArrayList<Future<?>>();
    try {
      for(long user=1;user<=50;user++) {
        String token=jwt.generateToken(user,"capacity-"+user,"USER");
        futures.add(workers.submit(()->{
          start.await();
          for(int i=0;i<20;i++) {
            long begun=System.nanoTime();var result=mvc.perform(get("/recommendations").header("Authorization","Bearer "+token)).andReturn();
            latencies.add((System.nanoTime()-begun)/1_000_000.0);
            assertEquals(200,result.getResponse().getStatus());
            assertEquals("READY",json.readTree(result.getResponse().getContentAsString()).get("state").asText());
          }
          return null;
        }));
      }
      start.countDown();for(var future:futures)future.get(5,TimeUnit.MINUTES);
    } finally {start.countDown();workers.shutdownNow();}
    var ordered=latencies.stream().sorted().toList();assertEquals(1000,ordered.size());
    return Map.of("readers",50,"samples",ordered.size(),"latenciesMillis",List.copyOf(latencies),
        "nearestRankP95Millis",ordered.get((int)Math.ceil(.95*ordered.size())-1),"failures",0,
        "transport","Spring MockMvc real JWT/security/JPA/MySQL, not browser or socket transport");
  }

  /** Independent loop-based full ordering, deliberately not the heap or RecommendationScorer. */
  private static List<Long> referenceTop30(List<CatalogBookFeatures> catalog,ReadingProfile profile) {
    record Score(long id,int smax,int smean,int amax,int amean,int language,int downloads) {}
    List<Score> ranking=new ArrayList<>();
    for(var book:catalog) {
      if(profile.excludedBookIds().contains(book.bookId()))continue;
      int[] subject=weights(book.canonicalSubjects(),profile.subjects());int[] author=weights(book.authors().keySet(),profile.authors());
      if(subject[0]==0&&author[0]==0)continue;
      int language=0;
      for(var code:book.languageCodes()) {var evidence=profile.languages().get(code);if(evidence!=null)language=Math.max(language,evidence.weight());}
      ranking.add(new Score(book.bookId(),subject[0],subject[1],author[0],author[1],language,book.downloadCount()));
    }
    var comparator=Comparator.comparingInt(Score::smax).reversed()
        .thenComparing(Comparator.comparingInt(Score::smean).reversed())
        .thenComparing(Comparator.comparingInt(Score::amax).reversed())
        .thenComparing(Comparator.comparingInt(Score::amean).reversed())
        .thenComparing(Comparator.comparingInt(Score::language).reversed())
        .thenComparing(Comparator.comparingInt(Score::downloads).reversed()).thenComparingLong(Score::id);
    ranking.sort(comparator);return ranking.stream().limit(30).map(Score::id).toList();
  }
  private static <K> int[] weights(Iterable<K> keys,Map<K,ReadingProfile.Evidence> evidence) {
    int max=0,sum=0,count=0;
    for(K key:keys) {var match=evidence.get(key);if(match!=null){max=Math.max(max,match.weight());sum+=match.weight();count++;}}
    return new int[]{max,count==0?0:1000*sum/count};
  }
  private Map<String,Long> databaseCounters() {
    var result=new TreeMap<String,Long>();
    jdbc.query("""
        SHOW GLOBAL STATUS WHERE Variable_name IN ('Innodb_data_read','Innodb_data_written','Innodb_data_reads',
          'Innodb_data_writes','Innodb_os_log_written','Innodb_row_lock_time','Innodb_row_lock_waits','Innodb_deadlocks')
        """,rs->{result.put(rs.getString(1),Long.parseLong(rs.getString(2)));});return result;
  }
  private static Map<String,Long> gcCounters() {
    var result=new TreeMap<String,Long>();
    for(var collector:ManagementFactory.getGarbageCollectorMXBeans()) {
      if(collector.getCollectionCount()>=0)result.put(collector.getName()+".count",collector.getCollectionCount());
      if(collector.getCollectionTime()>=0)result.put(collector.getName()+".millis",collector.getCollectionTime());
    }return result;
  }
  private static Long cpuTime() {
    return ManagementFactory.getOperatingSystemMXBean() instanceof com.sun.management.OperatingSystemMXBean os?os.getProcessCpuTime():null;
  }
  private static Long difference(Long before,Long after){return before==null||after==null?null:after-before;}
  private static Map<String,Long> delta(Map<String,Long> before,Map<String,Long> after) {
    var result=new TreeMap<String,Long>();after.forEach((key,value)->{if(before.containsKey(key))result.put(key,value-before.get(key));});return result;
  }
  private static Long rss() {
    try {
      Path status=Path.of("/proc/self/status");
      if(Files.isReadable(status))for(String line:Files.readAllLines(status))if(line.startsWith("VmRSS:"))return Long.parseLong(line.replaceAll("[^0-9]",""))*1024;
      if(System.getProperty("os.name").startsWith("Windows")) {
        var process=new ProcessBuilder("powershell.exe","-NoProfile","-NonInteractive","-Command",
            "(Get-Process -Id "+ProcessHandle.current().pid()+").WorkingSet64").redirectErrorStream(true).start();
        if(process.waitFor(5,TimeUnit.SECONDS)&&process.exitValue()==0)return Long.parseLong(new String(process.getInputStream().readAllBytes()).strip());
        process.destroyForcibly();
      }
    }catch(Exception ignored){/* Unsupported measurement is reported as null, never zero. */}return null;
  }
  private static final class MemoryWindow implements AutoCloseable {
    final AtomicLong heapPeak=new AtomicLong(ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed());
    final long heapBefore=heapPeak.get();final Long rssBefore=rss();
    final ScheduledExecutorService sampler=Executors.newSingleThreadScheduledExecutor();
    MemoryWindow(){sampler.scheduleAtFixedRate(()->heapPeak.accumulateAndGet(ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed(),Math::max),0,100,TimeUnit.MILLISECONDS);}
    Map<String,Object> measurement(){var result=new LinkedHashMap<String,Object>();result.put("heapBeforeBytes",heapBefore);result.put("heapAfterBytes",ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed());result.put("heapObservedPeakBytes",heapPeak.get());result.put("heapSampleIntervalMillis",100);result.put("rssBeforeBytes",rssBefore);result.put("rssAfterBytes",rss());return result;}
    @Override public void close(){sampler.shutdownNow();}
  }
}
