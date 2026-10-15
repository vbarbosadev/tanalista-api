package tanalista.adaptadores.http;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import tanalista.aplicacao.Itens;
import tanalista.config.IdentidadeDesenvolvimento;
import tanalista.dominio.ErroDominio;
import java.util.UUID;

@Path("/api/v1/listas/{listaId}/itens")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ItemResource {
    private final Itens itens;
    private final IdentidadeDesenvolvimento identidade;

    public ItemResource(Itens itens, IdentidadeDesenvolvimento identidade) {
        this.itens = itens;
        this.identidade = identidade;
    }

    @POST
    @Transactional
    public Response criar(@PathParam("listaId") UUID listaId, ItemRequest request, @Context UriInfo uri) {
        if (request == null) throw ErroDominio.invalido("Corpo JSON obrigatório.");
        var item = itens.criar(identidade.usuarioId(), listaId, request.dados());
        return Response.created(uri.getAbsolutePathBuilder().path(item.id().toString()).build()).entity(item).build();
    }
}
