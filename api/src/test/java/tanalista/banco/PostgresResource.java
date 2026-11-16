package tanalista.banco;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.Map;
import java.util.UUID;

/** A disposable database. Never accepts a URL or credentials of an existing database. */
public class PostgresResource implements QuarkusTestResourceLifecycleManager {
    private PostgreSQLContainer postgres;

    @Override
    public Map<String, String> start() {
        postgres = new PostgreSQLContainer("postgres:16-alpine")
                .withDatabaseName("tanalista_test")
                .withUsername("test")
                .withPassword(UUID.randomUUID().toString());
        try {
            postgres.start();
            return Map.of(
                    "quarkus.datasource.db-kind", "postgresql",
                    "quarkus.datasource.jdbc.url", postgres.getJdbcUrl(),
                    "quarkus.datasource.username", postgres.getUsername(),
                    "quarkus.datasource.password", postgres.getPassword(),
                    "quarkus.datasource.devservices.enabled", "false",
                    "quarkus.flyway.migrate-at-start", "true",
                    "tanalista.dev-identity.enabled", "true",
                    "tanalista.dev-identity.user-id", "00000000-0000-0000-0000-000000000001");
        } catch (RuntimeException failure) {
            postgres.stop();
            throw failure;
        }
    }

    @Override
    public void stop() {
        if (postgres != null) postgres.stop();
    }
}
