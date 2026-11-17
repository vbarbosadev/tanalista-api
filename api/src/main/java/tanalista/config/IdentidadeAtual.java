package tanalista.config;

import io.smallrye.jwt.auth.principal.JWTParser;
import io.smallrye.jwt.auth.principal.ParseException;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.ServiceUnavailableException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.UUID;

/** Cryptography and standard claims are verified by SmallRye JWT, never decoded by hand. */
@RequestScoped
public class IdentidadeAtual {
    @Inject ConfiguracaoAutenticacao configuracao;
    @Inject JWTParser parser;
    @Context HttpHeaders headers;
    @ConfigProperty(name = "tanalista.dev-identity.user-id")
    UUID usuarioDesenvolvimento;
    private UUID verificado;

    public UUID usuarioId() {
        if (verificado != null) return verificado;
        if (!configuracao.habilitada()) {
            if (!configuracao.desenvolvimento()) {
                throw new ServiceUnavailableException("Identidade não configurada.");
            }
            return usuarioDesenvolvimento;
        }
        var authorization = headers.getHeaderString(HttpHeaders.AUTHORIZATION);
        if (authorization == null || !authorization.regionMatches(true, 0, "Bearer ", 0, 7)) {
            throw new NotAuthorizedException("Bearer");
        }
        try {
            var jwt = parser.parse(authorization.substring(7).strip());
            verificado = IdentificadorExterno.de(jwt.getIssuer(), jwt.getSubject());
            return verificado;
        } catch (ParseException | IllegalArgumentException invalido) {
            // Do not log or expose the token, claims, key URL or verification failure.
            throw new NotAuthorizedException("Bearer");
        }
    }
}
