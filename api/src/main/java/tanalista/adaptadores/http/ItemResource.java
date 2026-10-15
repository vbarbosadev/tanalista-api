package tanalista.adaptadores.http;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import tanalista.aplicacao.Itens;
import tanalista.config.IdentidadeDesenvolvimento;
import tanalista.dominio.ErroDominio;
import tanalista.dominio.Item;
import tanalista.dominio.Pagina;
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
    public Response criar(@PathParam("listaId") UUID listaId, @Valid ItemRequest request, @Context UriInfo uri) {
        if (request == null) throw ErroDominio.invalido("Corpo JSON obrigatório.");
        var item = itens.criar(identidade.usuarioId(), listaId, request.dados());
        return Response.created(uri.getAbsolutePathBuilder().path(item.id().toString()).build()).entity(item).build();
    }

    @PATCH
    @Path("/{itemId}")
    @Transactional
    public Item atualizar(@PathParam("listaId") UUID listaId, @PathParam("itemId") UUID itemId,
                          ItemPatchRequest request) {
        if (request == null) throw ErroDominio.invalido("Corpo JSON obrigatório.");
        var usuario = identidade.usuarioId();
        var atual = itens.buscar(usuario, listaId, itemId);
        return itens.atualizar(usuario, listaId, itemId, request.mesclar(atual));
    }

    @DELETE
    @Path("/{itemId}")
    @Transactional
    public Response remover(@PathParam("listaId") UUID listaId, @PathParam("itemId") UUID itemId) {
        itens.remover(identidade.usuarioId(), listaId, itemId);
        return Response.noContent().build();
    }

    @GET
    public Pagina<Item> listar(@PathParam("listaId") UUID listaId,
                              @QueryParam("page") @DefaultValue("0") int page,
                              @QueryParam("size") @DefaultValue("20") int size,
                              @QueryParam("nome") String nome, @QueryParam("categoria") String categoria) {
        return itens.listar(identidade.usuarioId(), listaId, page, size, nome, categoria);
    }

    @GET
    @Path("/{itemId}")
    public Item buscar(@PathParam("listaId") UUID listaId, @PathParam("itemId") UUID itemId) {
        return itens.buscar(identidade.usuarioId(), listaId, itemId);
    }
}
