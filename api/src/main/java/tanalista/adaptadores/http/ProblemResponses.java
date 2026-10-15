package tanalista.adaptadores.http;

import jakarta.ws.rs.core.Response;

final class ProblemResponses {
    private ProblemResponses() { }

    static Response criar(int status, String detalhe, String caminho) {
        var padrao = Response.Status.fromStatusCode(status);
        String titulo = status == 422 ? "Unprocessable Content" : padrao == null ? "Erro HTTP" : padrao.getReasonPhrase();
        var problem = new Problem("about:blank", titulo, status, detalhe, caminho);
        return Response.status(status).type("application/problem+json").entity(problem).build();
    }
}
