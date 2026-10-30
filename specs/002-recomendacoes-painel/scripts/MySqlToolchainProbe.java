import java.sql.DriverManager;
import java.time.Duration;
import org.flywaydb.core.Flyway;
import org.testcontainers.containers.MySQLContainer;

/** Probe da ferramenta existente; não testa a funcionalidade de recomendações. */
public class MySqlToolchainProbe {
  public static void main(String[] args) throws Exception {
    String image = args.length == 0 ? "mysql:8.0.32" : args[0];
    try (var database = new MySQLContainer<>(image)
        .withDatabaseName("alexandria_002_preflight")
        .withUsername("preflight")
        .withPassword("local-test-only")
        .withStartupTimeout(Duration.ofMinutes(2))) {
      database.start();
      var flyway = Flyway.configure()
          .dataSource(database.getJdbcUrl(), database.getUsername(), database.getPassword())
          .locations("filesystem:src/main/resources/db/migration")
          .baselineOnMigrate(false)
          .load();
      var migrations = flyway.migrate();
      flyway.validate();
      try (var connection = DriverManager.getConnection(
               database.getJdbcUrl(), database.getUsername(), database.getPassword());
           var statement = connection.createStatement();
           var result = statement.executeQuery(
               "SELECT COUNT(*) FROM information_schema.tables "
                   + "WHERE table_schema='alexandria_002_preflight'")) {
        result.next();
        System.out.println("PREFLIGHT_OK migrations=" + migrations.migrationsExecuted
            + " tables=" + result.getInt(1));
      }
    }
  }
}
