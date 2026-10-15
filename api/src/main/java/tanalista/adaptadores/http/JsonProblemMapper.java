package tanalista.adaptadores.http;

import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import jakarta.annotation.Priority;
import jakarta.ws.rs.core.*;
import jakarta.ws.rs.ext.*;

/** Precede o mapper de diagnóstico padrão do Jackson do Quarkus. */
@Provider
@Priority(1)
public class JsonProblemMapper implements ExceptionMapper<MismatchedInputException> {
    @Context UriInfo uri;

    @Override
    public Response toResponse(MismatchedInputException erro) {
        return ProblemResponses.criar(400, "JSON incompatível com o contrato da operação.", uri.getAbsolutePath().getPath());
    }
}
