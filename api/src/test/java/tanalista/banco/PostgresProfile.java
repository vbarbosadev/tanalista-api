package tanalista.banco;

import io.quarkus.test.junit.QuarkusTestProfile;
import java.util.List;

/** Separate Quarkus lifecycle: H2 tests cannot lend a datasource to this suite. */
public class PostgresProfile implements QuarkusTestProfile {
    @Override
    public boolean disableGlobalTestResources() {
        return true;
    }

    @Override
    public List<TestResourceEntry> testResources() {
        return List.of(new TestResourceEntry(PostgresResource.class));
    }
}
