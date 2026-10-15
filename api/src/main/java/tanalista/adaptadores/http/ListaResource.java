package tanalista.adaptadores.http;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import tanalista.aplicacao.Listas;
import tanalista.config.IdentidadeDesenvolvimento;
import tanalista.dominio.ErroDominio;
import tanalista.dominio.Lista;
import tanalista.dominio.Pagina;
import java.util.UUID;

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
    public Response criar(@Valid NomeRequest request, @Context UriInfo uri) {
        if (request == null) throw ErroDominio.invalido("Corpo JSON obrigatório.");
        var lista = listas.criar(identidade.usuarioId(), request.nome());
        return Response.created(uri.getAbsolutePathBuilder().path(lista.id().toString()).build())
                .entity(lista).build();
    }

    @GET
    public Pagina<Lista> listar(@QueryParam("page") @DefaultValue("0") int page,
                              @QueryParam("size") @DefaultValue("20") int size,
                              @QueryParam("nome") String nome) {
        return listas.listar(identidade.usuarioId(), page, size, nome);
    }

    @GET
    @Path("/{listaId}")
    public Lista buscar(@PathParam("listaId") UUID listaId) {
        return listas.buscar(identidade.usuarioId(), listaId);
    }

    @PATCH
    @Path("/{listaId}")
    @Transactional
    public Lista renomear(@PathParam("listaId") UUID listaId, @Valid NomeRequest request) {
        if (request == null) throw ErroDominio.invalido("Corpo JSON obrigatório.");
        return listas.renomear(identidade.usuarioId(), listaId, request.nome());
    }

    @DELETE
    @Path("/{listaId}")
    @Transactional
    public Response remover(@PathParam("listaId") UUID listaId) {
        listas.remover(identidade.usuarioId(), listaId);
        return Response.noContent().build();
    }
}
