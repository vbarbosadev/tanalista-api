package tanalista.auth;

import java.util.HashMap;
import java.util.Map;

public class LocalAuthMemoryProfile extends AuthProfile {
    @Override public Map<String, String> getConfigOverrides() {
        var configuracao = new HashMap<>(super.getConfigOverrides());
        configuracao.put("tanalista.auth.local-enabled", "true");
        return configuracao;
    }
}
