package tanalista.auth;

import io.quarkus.test.junit.QuarkusTestProfile;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AuthProfile implements QuarkusTestProfile {
    @Override
    public Map<String, String> getConfigOverrides() {
        return Map.of(
                "tanalista.auth.enabled", "true",
                "tanalista.dev-identity.enabled", "false",
                "mp.jwt.verify.issuer", "https://issuer.example/",
                "mp.jwt.verify.audiences", "tanalista-api",
                "quarkus.flyway.migrate-at-start", "false");
    }

    @Override
    public boolean disableGlobalTestResources() { return true; }

    @Override
    public List<TestResourceEntry> testResources() {
        return List.of(new TestResourceEntry(ChavesJwtResource.class));
    }

    @Override
    public Set<Class<?>> getEnabledAlternatives() {
        return Set.of(RepositoriosAuthTeste.class);
    }
}
