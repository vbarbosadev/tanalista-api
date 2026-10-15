package tanalista.adaptadores.http;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import tanalista.aplicacao.Listas;
import tanalista.config.IdentidadeDesenvolvimento;
import tanalista.dominio.ErroDominio;

@Path("/api/v1/listas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ListaResource {
    private final Listas listas;
    private final IdentidadeDesenvolvimento identidade;

    public ListaResource(Listas listas, IdentidadeDesenvolvimento identidade) {
        this.listas = listas;
        this.identidade = identidade;
    }

    @POST
    @Transactional
    public Response criar(NomeRequest request, @Context UriInfo uri) {
        if (request == null) throw ErroDominio.invalido("Corpo JSON obrigatório.");
        var lista = listas.criar(identidade.usuarioId(), request.nome());
        return Response.created(uri.getAbsolutePathBuilder().path(lista.id().toString()).build())
                .entity(lista).build();
    }
}
