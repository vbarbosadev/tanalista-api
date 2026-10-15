package tanalista.adaptadores.http;

import io.quarkus.hibernate.validator.runtime.jaxrs.ResteasyReactiveViolationException;
import jakarta.annotation.Priority;
import jakarta.ws.rs.core.*;
import jakarta.ws.rs.ext.*;

@Provider
@Priority(1)
public class ValidationProblemMapper implements ExceptionMapper<ResteasyReactiveViolationException> {
    @Context UriInfo uri;

    @Override
    public Response toResponse(ResteasyReactiveViolationException erro) {
        return ProblemResponses.criar(422, "Entrada inválida. Verifique os campos e os limites documentados.",
                uri.getAbsolutePath().getPath());
    }
}
