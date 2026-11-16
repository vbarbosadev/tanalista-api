package tanalista.banco;

import io.quarkus.test.junit.QuarkusTestProfile;
import java.util.List;
import java.util.Map;

/** Separate Quarkus lifecycle: H2 tests cannot lend a datasource to this suite. */
public class PostgresProfile implements QuarkusTestProfile {
    @Override
    public Map<String, String> getConfigOverrides() {
        // db-kind is fixed during augmentation, before resource runtime settings.
        return Map.of(
                "quarkus.datasource.db-kind", "postgresql",
                "quarkus.datasource.devservices.enabled", "false",
                "quarkus.flyway.migrate-at-start", "true",
                "tanalista.dev-identity.enabled", "true");
    }

    @Override
    public boolean disableGlobalTestResources() {
        return true;
    }

    @Override
    public List<TestResourceEntry> testResources() {
        return List.of(new TestResourceEntry(PostgresResource.class));
    }
}
