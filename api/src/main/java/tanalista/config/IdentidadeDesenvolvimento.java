package tanalista.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.ServiceUnavailableException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import java.util.UUID;

@ApplicationScoped
public class IdentidadeDesenvolvimento {
    @ConfigProperty(name = "tanalista.dev-identity.enabled", defaultValue = "false")
    boolean habilitada;

    @ConfigProperty(name = "tanalista.dev-identity.user-id")
    UUID usuarioId;

    public UUID usuarioId() {
        if (!habilitada) throw new ServiceUnavailableException("Identidade de desenvolvimento desabilitada.");
        return usuarioId;
    }
}
