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
import java.util.Set;
import org.eclipse.microprofile.openapi.annotations.*;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.responses.*;
import org.eclipse.microprofile.openapi.annotations.media.*;
import org.eclipse.microprofile.openapi.annotations.headers.Header;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;

@Path("/api/v1/listas/{listaId}/itens")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Itens", description = "Itens planejados pertencentes a uma lista visível ao dono.")
@APIResponses({
    @APIResponse(responseCode = "400", description = "JSON, tipo ou parâmetro malformado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = Problem.class))),
    @APIResponse(responseCode = "404", description = "Lista/item inexistente ou item de outra lista", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = Problem.class))),
    @APIResponse(responseCode = "405", description = "Método não permitido", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = Problem.class))),
    @APIResponse(responseCode = "406", description = "Formato não aceito", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = Problem.class))),
    @APIResponse(responseCode = "409", description = "Nome normalizado já utilizado nesta lista", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = Problem.class))),
    @APIResponse(responseCode = "415", description = "Content-Type não suportado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = Problem.class))),
    @APIResponse(responseCode = "422", description = "Entrada fora dos limites do contrato", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = Problem.class))),
    @APIResponse(responseCode = "500", description = "Erro interno sem detalhes sensíveis", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = Problem.class))),
    @APIResponse(responseCode = "503", description = "Identidade de desenvolvimento desabilitada", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = Problem.class)))
})
public class ItemResource {
    private final Itens itens;
    private final IdentidadeDesenvolvimento identidade;

    public ItemResource(Itens itens, IdentidadeDesenvolvimento identidade) {
        this.itens = itens;
        this.identidade = identidade;
    }

    @POST
    @Transactional
    @Operation(summary = "Adicionar item à lista")
    @APIResponse(responseCode = "201", description = "Item criado", headers = @Header(name = "Location", description = "URL do item criado", schema = @Schema(type = SchemaType.STRING, format = "uri")), content = @Content(schema = @Schema(implementation = Item.class)))
    public Response criar(@PathParam("listaId") UUID listaId, @Valid ItemRequest request, @Context UriInfo uri) {
        if (request == null) throw ErroDominio.invalido("Corpo JSON obrigatório.");
        var item = itens.criar(identidade.usuarioId(), listaId, request.dados());
        return Response.created(uri.getAbsolutePathBuilder().path(item.id().toString()).build()).entity(item).build();
    }

    @PATCH
    @Path("/{itemId}")
    @Transactional
    @Operation(summary = "Editar item parcialmente", description = "Campo ausente mantém valor. null explícito limpa opcionais; nome não pode ser nulo. Corpo vazio ou campo desconhecido é inválido.")
    @APIResponse(responseCode = "200", description = "Item atualizado", content = @Content(schema = @Schema(implementation = Item.class)))
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
    @Operation(summary = "Remover item planejado")
    @APIResponse(responseCode = "204", description = "Item removido, sem corpo")
    public Response remover(@PathParam("listaId") UUID listaId, @PathParam("itemId") UUID itemId) {
        itens.remover(identidade.usuarioId(), listaId, itemId);
        return Response.noContent().build();
    }

    @GET
    @Operation(summary = "Listar itens da lista", description = "Paginação no SQL; ordem nomeNormalizado ASC, id ASC.")
    @APIResponse(responseCode = "200", description = "Página de itens", content = @Content(schema = @Schema(implementation = OpenApiSchemas.PaginaItens.class)))
    public Pagina<Item> listar(@PathParam("listaId") UUID listaId,
                              @Parameter(description = "Página a partir de zero; offset até 2147483647", schema = @Schema(minimum = "0")) @QueryParam("page") @DefaultValue("0") int page,
                              @Parameter(description = "Tamanho de 1 a 100", schema = @Schema(minimum = "1", maximum = "100")) @QueryParam("size") @DefaultValue("20") int size,
                              @Parameter(description = "Substring normalizada, até 120 caracteres") @QueryParam("nome") String nome,
                              @Parameter(description = "Categoria exata, sensível a caixa, até 60 caracteres") @QueryParam("categoria") String categoria,
                              @Context UriInfo uri) {
        QueryParameters.validar(uri, Set.of("page", "size", "nome", "categoria"));
        return itens.listar(identidade.usuarioId(), listaId, page, size, nome, categoria);
    }

    @GET
    @Path("/{itemId}")
    @Operation(summary = "Consultar item por UUID dentro da lista")
    @APIResponse(responseCode = "200", description = "Item encontrado", content = @Content(schema = @Schema(implementation = Item.class)))
    public Item buscar(@PathParam("listaId") UUID listaId, @PathParam("itemId") UUID itemId) {
        return itens.buscar(identidade.usuarioId(), listaId, itemId);
    }
}
