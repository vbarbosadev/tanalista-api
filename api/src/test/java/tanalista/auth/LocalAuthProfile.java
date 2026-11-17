package tanalista.auth;

import io.quarkus.test.junit.QuarkusTestProfile;
import java.util.List;
import java.util.Map;

public class LocalAuthProfile implements QuarkusTestProfile {
    @Override public Map<String, String> getConfigOverrides() {
        return Map.of(
                "quarkus.datasource.db-kind", "postgresql",
                "quarkus.flyway.migrate-at-start", "true",
                "tanalista.auth.enabled", "true",
                "tanalista.auth.local-enabled", "true",
                "tanalista.dev-identity.enabled", "false",
                "mp.jwt.verify.issuer", "https://tanalista.test/",
                "mp.jwt.verify.audiences", "tanalista-api");
    }
    @Override public boolean disableGlobalTestResources() { return true; }
    @Override public List<TestResourceEntry> testResources() {
        return List.of(new TestResourceEntry(LocalAuthTestResource.class));
    }
}
