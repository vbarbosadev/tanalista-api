package tanalista.config;

import io.quarkus.runtime.LaunchMode;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.net.URI;
import java.util.Optional;

@ApplicationScoped
public class ConfiguracaoAutenticacao {
    @ConfigProperty(name = "tanalista.auth.enabled", defaultValue = "false")
    boolean habilitada;
    @ConfigProperty(name = "tanalista.dev-identity.enabled", defaultValue = "false")
    boolean desenvolvimento;
    @ConfigProperty(name = "mp.jwt.verify.issuer")
    Optional<String> emissor;
    @ConfigProperty(name = "mp.jwt.verify.audiences")
    Optional<String> audiencia;
    @ConfigProperty(name = "mp.jwt.verify.publickey.location")
    Optional<String> chaves;

    boolean habilitada() { return habilitada; }

    boolean desenvolvimento() { return desenvolvimento; }

    void iniciar(@Observes StartupEvent evento) {
        validar(habilitada, desenvolvimento, emissor.orElse(""), audiencia.orElse(""),
                chaves.orElse(""), LaunchMode.current() == LaunchMode.TEST);
    }

    static void validar(boolean habilitada, boolean desenvolvimento, String emissor,
                        String audiencia, String chaves, boolean teste) {
        if (!habilitada) return;
        if (desenvolvimento) {
            throw new IllegalStateException("AUTH_ENABLED exige DEV_IDENTITY_ENABLED=false.");
        }
        if (ausente(emissor) || ausente(audiencia) || ausente(chaves)) {
            throw new IllegalStateException("Configure emissor, audiência e JWKS para autenticação.");
        }
        if (!https(emissor) || (!teste && !https(chaves))) {
            throw new IllegalStateException("Emissor e JWKS devem usar HTTPS.");
        }
    }

    private static boolean ausente(String valor) {
        return valor.isBlank() || "unconfigured".equals(valor);
    }

    private static boolean https(String valor) {
        try {
            var uri = URI.create(valor);
            return "https".equals(uri.getScheme()) && uri.getHost() != null
                    && uri.getUserInfo() == null && uri.getFragment() == null;
        } catch (IllegalArgumentException invalida) {
            return false;
        }
    }
}
