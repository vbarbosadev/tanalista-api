package tanalista.banco;

import io.quarkus.test.junit.QuarkusTestProfile;

/** Separate Quarkus lifecycle: H2 tests cannot lend a datasource to this suite. */
public class PostgresProfile implements QuarkusTestProfile {
    @Override
    public boolean disableGlobalTestResources() {
        return true;
    }
}
