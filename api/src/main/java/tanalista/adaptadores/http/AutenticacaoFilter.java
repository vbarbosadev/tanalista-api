package tanalista.adaptadores.http;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;
import tanalista.config.IdentidadeAtual;

/** Post-match filter: resource classes use worker threads for JDBC/JWKS work. */
@Provider
@Priority(Priorities.AUTHENTICATION)
public class AutenticacaoFilter implements ContainerRequestFilter {
    @Inject IdentidadeAtual identidade;

    @Override
    public void filter(ContainerRequestContext request) {
        if (request.getUriInfo().getPath().startsWith("api/v1/")) {
            identidade.usuarioId();
        }
    }
}
