package tanalista.auth;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import tanalista.banco.PostgresResource;
import java.util.HashMap;
import java.util.Map;

public class LocalAuthTestResource implements QuarkusTestResourceLifecycleManager {
    private final PostgresResource banco = new PostgresResource();
    private final ChavesJwtResource chaves = new ChavesJwtResource();

    @Override public Map<String, String> start() {
        try {
            var configuracao = new HashMap<>(banco.start());
            configuracao.putAll(chaves.start());
            configuracao.put("tanalista.dev-identity.enabled", "false");
            return configuracao;
        } catch (RuntimeException falha) { stop(); throw falha; }
    }
    @Override public void stop() { try { banco.stop(); } finally { chaves.stop(); } }
}
