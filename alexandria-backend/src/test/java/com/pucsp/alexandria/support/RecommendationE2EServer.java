package com.pucsp.alexandria.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pucsp.alexandria.AlexandriaApplication;
import com.pucsp.alexandria.adapter.in.job.RecommendationExecutionResources;
import com.pucsp.alexandria.application.recommendation.RequestDailyRecommendationGenerationUseCase;
import com.pucsp.alexandria.config.jwt.JwtTokenProvider;
import com.pucsp.alexandria.domain.recommendation.GenerationOrigin;
import com.pucsp.alexandria.domain.recommendation.RecommendationCatalogSnapshots;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.testcontainers.containers.MySQLContainer;

/**
 * Isolated browser-test server. Control is through files in a private temporary
 * directory, never an HTTP route. No application database or volume is used.
 */
public final class RecommendationE2EServer {
  private static final Instant INITIAL_TIME = Instant.parse("2026-10-30T12:00:00Z");
  private static final ZoneId ZONE = ZoneId.of("America/Sao_Paulo");
  private final MutableClock clock = new MutableClock(INITIAL_TIME, ZONE);
  private final ObjectMapper json = new ObjectMapper();
  private ConfigurableApplicationContext context;
  private JdbcTemplate jdbc;
  private Path directory;
  private int port;

  public static void main(String[] args) throws Exception {
    if (args.length < 1) throw new IllegalArgumentException("Pass a private temporary control directory");
    new RecommendationE2EServer().run(Path.of(args[0]).toAbsolutePath(),
        args.length > 1 ? Integer.parseInt(args[1]) : 18080);
  }

  private void run(Path controlDirectory, int requestedPort) throws Exception {
    directory = controlDirectory;
    Files.createDirectories(directory);
    try (var mysql = new MySQLContainer<>("mysql:8.0.32")
        .withDatabaseName("recommendations_e2e")
        .withUsername("e2e").withPassword("local-browser-tests-only")) {
      mysql.start();
      var application = new SpringApplication(AlexandriaApplication.class);
      application.addInitializers(ctx -> ((GenericApplicationContext) ctx)
          .registerBean("recommendationClock", Clock.class, () -> clock));
      String[] applicationArguments = {
          "--server.address=127.0.0.1", "--server.port=" + requestedPort,
          "--spring.datasource.url=" + mysql.getJdbcUrl(),
          "--spring.datasource.username=" + mysql.getUsername(),
          "--spring.datasource.password=" + mysql.getPassword(),
          "--spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver",
          "--spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect",
          "--spring.jpa.hibernate.ddl-auto=validate", "--spring.flyway.enabled=true",
          "--spring.flyway.baseline-on-migrate=false",
          "--spring.jpa.properties.hibernate.jdbc.time_zone=UTC",
          "--recommendations.jobs-enabled=false",
          "--cors.allowed-origins=http://localhost:3100,http://127.0.0.1:3100",
          "--jwt.secret=browser-test-secret-only-not-for-production-256-bits",
          "--jwt.expiration-ms=86400000", "--google.client-id=browser-test-client",
          "--spring.ai.model.chat=ollama", "--spring.ai.model.embedding=none",
          "--spring.ai.ollama.init.pull-model-strategy=never" };
      context = application.run(applicationArguments);
      try {
        jdbc = context.getBean(JdbcTemplate.class);
        port = Integer.parseInt(context.getEnvironment().getProperty("local.server.port"));
        write("ready.json", reset("default"));
        System.out.println("RECOMMENDATION_E2E_READY " + directory);
        String lastId = "";
        boolean running = true;
        while (running && !Thread.currentThread().isInterrupted()) {
          Path request = directory.resolve("request.json");
          if (Files.exists(request)) {
            JsonNode command = json.readTree(request.toFile());
            String id = command.path("id").asText();
            if (!id.isEmpty() && !id.equals(lastId)) {
              lastId = id;
              try {
                Object result = null;
                switch (command.path("action").asText()) {
                  case "reset" -> result = reset(command.path("scenario").asText("default"));
                  case "capacity" -> result = capacity();
                  case "restart" -> result = restart(application, applicationArguments,
                      command.path("jobsEnabled").asBoolean(false), command.path("recover").asBoolean(false));
                  case "advance" -> clock.set(Instant.parse(command.path("instant").asText()));
                  case "delete" -> {
                    long bookId = command.path("bookId").asLong(100L);
                    jdbc.update("DELETE FROM book_authors WHERE book_id=?", bookId);
                    jdbc.update("DELETE FROM books WHERE id=?", bookId);
                  }
                  case "complete" -> {
                    awaitWorkers();
                    LocalDate today = LocalDate.now(clock);
                    jdbc.update("DELETE FROM recommendation_batches WHERE user_id=1 AND reference_date=?",
                        java.sql.Date.valueOf(today));
                    seedReady(today, 7);
                  }
                  case "stop" -> running = false;
                  default -> throw new IllegalArgumentException("Unknown test command");
                }
                var response = new LinkedHashMap<String, Object>();
                response.put("id", id); response.put("ok", true); response.put("result", result);
                write("response.json", response);
              } catch (Exception ex) {
                write("response.json", Map.of("id", id, "ok", false,
                    "error", ex.getClass().getSimpleName()));
              }
            }
          }
          Thread.sleep(50);
        }
      } finally { context.close(); }
    }
  }

  private Map<String, Object> reset(String scenario) throws Exception {
    awaitWorkers();
    clock.set(INITIAL_TIME);
    clearDatabase();
    String password = context.getBean(PasswordEncoder.class).encode("Browser-test-123!");
    for (long id = 1; id <= 2; id++) {
      jdbc.update("INSERT INTO users(id,username,first_name,last_name,email,password,created_at,role) "
          + "VALUES(?,?,?,?,?,?,UTC_TIMESTAMP(6),'USER')", id, "e2e_reader_" + id,
          "Leitor", String.valueOf(id), "reader" + id + "@example.invalid", password);
    }
    jdbc.update("INSERT INTO authors(id,name) VALUES(1,'Autora do perfil'),(2,'Outro autor')");
    jdbc.update("INSERT INTO books(id,title,source,download_url,subjects,languages,download_count) "
        + "VALUES(10,'Origem do perfil','GUTENDEX','https://example.invalid/source.epub','Ficção','pt',0)");
    jdbc.update("INSERT INTO book_authors(book_id,author_id) VALUES(10,1)");
    if (!scenario.equals("no_profile")) {
      jdbc.update("INSERT INTO user_books(user_id,book_id,status,progress,rating,created_at) "
          + "VALUES(1,10,'done',NULL,5,UTC_TIMESTAMP(6))");
    }
    int count = scenario.equals("many") ? 40 : 7;
    for (int index = 0; index < count; index++) {
      jdbc.update("INSERT INTO books(id,title,source,download_url,subjects,languages,download_count) "
          + "VALUES(?,?,'GUTENDEX','https://example.invalid/book.epub',?,'pt',?)",
          100L + index, "Sugestão " + (index + 1),
          scenario.equals("no_matches") ? "História" : "Ficção", 1000 - index);
      jdbc.update("INSERT INTO book_authors(book_id,author_id) VALUES(?,?)", 100L + index,
          scenario.equals("no_matches") ? 2 : 1);
    }
    LocalDate today = LocalDate.now(clock);
    switch (scenario) {
      case "ready_empty" -> seedReady(today, 0);
      case "stale_failed" -> { seedReady(today.minusDays(1), 7); seedActive(today, false); }
      case "stale_empty" -> { seedReady(today.minusDays(1), 0); seedActive(today, true); }
      case "failed" -> seedActive(today, false);
      case "generating" -> seedActive(today, true);
      default -> { }
    }
    var tokens = context.getBean(JwtTokenProvider.class);
    var result = new LinkedHashMap<String, Object>();
    result.put("baseUrl", "http://localhost:" + port);
    result.put("users", Map.of(
        "a", Map.of("userId", 1L, "username", "e2e_reader_1",
            "token", tokens.generateToken(1L, "e2e_reader_1", "USER")),
        "b", Map.of("userId", 2L, "username", "e2e_reader_2",
            "token", tokens.generateToken(2L, "e2e_reader_2", "USER"))));
    result.put("books", Map.of("source", 10L, "first", 100L, "last", 99L + count));
    result.put("referenceDate", today.toString());
    write("ready.json", result);
    return result;
  }

  private void clearDatabase() {
    for (String table : new String[] { "recommendation_items", "recommendation_batches",
        "user_books", "book_authors", "subscriptions", "users", "books", "authors" }) {
      jdbc.update("DELETE FROM " + table);
    }
  }

  /** Restarts the service context, executors and snapshots while MySQL survives. */
  private Map<String, Object> restart(SpringApplication application, String[] arguments,
      boolean jobsEnabled, boolean recover) throws Exception {
    awaitWorkers();
    LocalDate today = LocalDate.now(clock);
    if (recover) {
      jdbc.update("INSERT INTO recommendation_batches(user_id,reference_date,status,algorithm_version,"
          + "selected_count,attempt_count,automatic_attempt_count,lease_token,lease_until,created_at,"
          + "updated_at,operational_pending_since) VALUES(1,?,'BUILDING','content-v1',0,2,2,"
          + "'interrupted-test-claim',TIMESTAMPADD(SECOND,-600,UTC_TIMESTAMP(6)),UTC_TIMESTAMP(6),"
          + "UTC_TIMESTAMP(6),UTC_TIMESTAMP(6))", java.sql.Date.valueOf(today));
    }
    context.close();
    String[] restartArguments = java.util.Arrays.stream(arguments)
        .map(argument -> argument.startsWith("--recommendations.jobs-enabled=")
            ? "--recommendations.jobs-enabled=" + jobsEnabled : argument).toArray(String[]::new);
    context = application.run(restartArguments);
    jdbc = context.getBean(JdbcTemplate.class);
    port = Integer.parseInt(context.getEnvironment().getProperty("local.server.port"));
    if (recover) {
      long deadline = System.nanoTime() + java.time.Duration.ofSeconds(20).toNanos();
      boolean ready = false;
      while (!ready && System.nanoTime() < deadline) {
        ready = jdbc.queryForObject("SELECT COUNT(*) FROM recommendation_batches WHERE user_id=1 "
            + "AND reference_date=? AND status='READY' AND automatic_attempt_count=3",
            Integer.class, java.sql.Date.valueOf(today)) == 1;
        if (!ready) Thread.sleep(50);
      }
      if (!ready) throw new IllegalStateException("Startup did not recover the interrupted claim");
    }
    return Map.of("resumedExpired", recover, "contextRestarted", true,
        "baseUrl", "http://localhost:" + port);
  }

  /** Full-size data and real motor generation; only reachable from local test IPC. */
  private Map<String, Object> capacity() throws Exception {
    awaitWorkers();
    clock.set(INITIAL_TIME);
    clearDatabase();
    var seeded = RecommendationCapacityFixture.seed(jdbc);
    var generation = context.getBean(RequestDailyRecommendationGenerationUseCase.class);
    var resources = context.getBean(RecommendationExecutionResources.class);
    var snapshots = context.getBean(RecommendationCatalogSnapshots.class);
    long start = System.nanoTime();
    long deadline = start + java.time.Duration.ofMinutes(60).toNanos();
    try (var owner = snapshots.open(generation.today())) {
      for (long reader : RecommendationCapacityFixture.readerIds()) {
        while (true) {
          if (System.nanoTime() >= deadline) throw new IllegalStateException("Generation exceeded the capacity window");
          var accepted = generation.execute(reader, generation.today(), GenerationOrigin.AUTO, owner);
          if (!"IDLE".equals(accepted.generationStatus())) break;
          Thread.sleep(25);
        }
      }
      while (resources.active() > 0 || resources.queued() > 0) {
        if (System.nanoTime() >= deadline) throw new IllegalStateException("Generation exceeded the capacity window");
        Thread.sleep(50);
      }
    }
    int ready = jdbc.queryForObject("SELECT COUNT(*) FROM recommendation_batches WHERE status='READY' AND reference_date=?",
        Integer.class, java.sql.Date.valueOf(generation.today()));
    if (ready != RecommendationCapacityFixture.READERS) throw new IllegalStateException("Not all capacity readers have READY");
    var tokens = context.getBean(JwtTokenProvider.class);
    var loadUsers = new ArrayList<Map<String, Object>>();
    for (long reader = 1; reader <= 50; reader++) {
      String username = jdbc.queryForObject("SELECT username FROM users WHERE id=?", String.class, reader);
      loadUsers.add(Map.of("userId", reader, "username", username,
          "token", tokens.generateToken(reader, username, "USER")));
    }
    var result = new LinkedHashMap<String, Object>();
    result.put("baseUrl", "http://localhost:" + port);
    result.put("users", Map.of("a", loadUsers.get(0), "b", loadUsers.get(1)));
    result.put("books", Map.of("source", 1L, "first", 1001L, "last", (long) seeded.books()));
    result.put("referenceDate", generation.today().toString());
    result.put("loadUsers", loadUsers);
    var dataset = new LinkedHashMap<String, Object>();
    dataset.put("books", seeded.books()); dataset.put("readers", seeded.readers());
    dataset.put("libraryLinksPerReader", RecommendationCapacityFixture.LIBRARY_SIZE);
    dataset.put("javaVersion", System.getProperty("java.version"));
    dataset.put("maxHeapBytes", Runtime.getRuntime().maxMemory());
    dataset.put("processors", Runtime.getRuntime().availableProcessors());
    var dataSource = context.getBean(javax.sql.DataSource.class);
    dataset.put("hikariMaxPoolSize", dataSource instanceof com.zaxxer.hikari.HikariDataSource hikari
        ? hikari.getMaximumPoolSize() : null);
    dataset.put("mysqlImage", "mysql:8.0.32");
    result.put("dataset", dataset);
    result.put("seedMillis", seeded.durationMillis());
    result.put("generationMillis", (System.nanoTime() - start) / 1_000_000);
    write("ready.json", result);
    return result;
  }

  private void seedReady(LocalDate date, int count) {
    jdbc.update("INSERT INTO recommendation_batches(user_id,reference_date,status,algorithm_version,"
        + "selected_count,generated_at,attempt_count,automatic_attempt_count,created_at,updated_at) "
        + "VALUES(1,?,'READY','content-v1',?,UTC_TIMESTAMP(6),1,1,UTC_TIMESTAMP(6),UTC_TIMESTAMP(6))",
        java.sql.Date.valueOf(date), count);
    Long batch = jdbc.queryForObject("SELECT id FROM recommendation_batches WHERE user_id=1 AND reference_date=?",
        Long.class, java.sql.Date.valueOf(date));
    for (int index = 0; index < count; index++) {
      jdbc.update("INSERT INTO recommendation_items(batch_id,position,book_id,reason_type,reason_value,"
          + "reason_text,source_book_id,source_book_title,evidence_weight) VALUES(?,?,?,'SUBJECT','ficção',?,10,?,4)",
          batch, index + 1, 100L + index,
          "Compartilha o assunto ficção com Origem do perfil na biblioteca usada nessa seleção.",
          "Origem do perfil");
    }
  }

  private void seedActive(LocalDate date, boolean building) {
    jdbc.update("INSERT INTO recommendation_batches(user_id,reference_date,status,algorithm_version,"
        + "selected_count,attempt_count,automatic_attempt_count,lease_token,lease_until,failed_at,"
        + "created_at,updated_at,operational_pending_since,failure_code) "
        + "VALUES(1,?,?,'content-v1',0,3,3,?,"
        + (building ? "TIMESTAMPADD(MINUTE,5,UTC_TIMESTAMP(6)),NULL," : "NULL,TIMESTAMPADD(SECOND,-61,UTC_TIMESTAMP(6)),")
        + "UTC_TIMESTAMP(6),UTC_TIMESTAMP(6),UTC_TIMESTAMP(6),?)",
        java.sql.Date.valueOf(date), building ? "BUILDING" : "FAILED",
        building ? "test-fixture-lease" : null, building ? null : "TEST_FAILURE");
  }

  private void awaitWorkers() throws InterruptedException {
    if (context == null) return;
    long deadline = System.nanoTime() + java.time.Duration.ofSeconds(15).toNanos();
    while (System.nanoTime() < deadline) {
      var resources = context.getBeanProvider(RecommendationExecutionResources.class).getIfAvailable();
      boolean busy = resources != null && (resources.active() > 0 || resources.queued() > 0);
      busy |= context.getBeansOfType(ThreadPoolExecutor.class).values().stream()
          .filter(executor -> !(executor instanceof ScheduledThreadPoolExecutor))
          .anyMatch(executor -> executor.getActiveCount() > 0 || !executor.getQueue().isEmpty());
      busy |= context.getBeansOfType(ThreadPoolTaskExecutor.class).values().stream()
          .anyMatch(executor -> executor.getActiveCount() > 0 || executor.getQueueSize() > 0);
      if (!busy) return;
      Thread.sleep(50);
    }
    throw new IllegalStateException("Test workers did not become idle");
  }

  private void write(String filename, Object value) throws Exception {
    Path temporary = directory.resolve(filename + ".tmp");
    json.writeValue(temporary.toFile(), value);
    Files.move(temporary, directory.resolve(filename), StandardCopyOption.REPLACE_EXISTING,
        StandardCopyOption.ATOMIC_MOVE);
  }

  static final class MutableClock extends Clock {
    private final AtomicReference<Instant> time;
    private final ZoneId zone;
    MutableClock(Instant initial, ZoneId zone) { this(new AtomicReference<>(initial), zone); }
    private MutableClock(AtomicReference<Instant> time, ZoneId zone) { this.time = time; this.zone = zone; }
    void set(Instant instant) { time.set(instant); }
    @Override public ZoneId getZone() { return zone; }
    @Override public Clock withZone(ZoneId zone) { return new MutableClock(time, zone); }
    @Override public Instant instant() { return time.get(); }
  }
}
