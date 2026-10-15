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
import org.eclipse.microprofile.openapi.annotations.*;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.responses.*;
import org.eclipse.microprofile.openapi.annotations.media.*;
import org.eclipse.microprofile.openapi.annotations.headers.Header;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;

@Path("/api/v1/listas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Listas", description = "CRUD com identidade de desenvolvimento do servidor; não implementa autenticação.")
@APIResponses({
    @APIResponse(responseCode = "400", description = "JSON, tipo ou parâmetro malformado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = Problem.class))),
    @APIResponse(responseCode = "404", description = "Recurso inexistente ou não visível", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = Problem.class))),
    @APIResponse(responseCode = "405", description = "Método não permitido", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = Problem.class))),
    @APIResponse(responseCode = "406", description = "Formato não aceito", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = Problem.class))),
    @APIResponse(responseCode = "409", description = "Nome normalizado já utilizado pelo dono", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = Problem.class))),
    @APIResponse(responseCode = "415", description = "Content-Type não suportado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = Problem.class))),
    @APIResponse(responseCode = "422", description = "Entrada fora dos limites do contrato", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = Problem.class))),
    @APIResponse(responseCode = "500", description = "Erro interno sem detalhes sensíveis", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = Problem.class))),
    @APIResponse(responseCode = "503", description = "Identidade de desenvolvimento desabilitada", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = Problem.class)))
})
public class ListaResource {
    private final Listas listas;
    private final IdentidadeDesenvolvimento identidade;

    public ListaResource(Listas listas, IdentidadeDesenvolvimento identidade) {
        this.listas = listas;
        this.identidade = identidade;
    }

    @POST
    @Transactional
    @Operation(summary = "Criar lista")
    @APIResponse(responseCode = "201", description = "Lista criada", headers = @Header(name = "Location", description = "URL da lista criada", schema = @Schema(type = SchemaType.STRING, format = "uri")), content = @Content(schema = @Schema(implementation = Lista.class)))
    public Response criar(@Valid NomeRequest request, @Context UriInfo uri) {
        if (request == null) throw ErroDominio.invalido("Corpo JSON obrigatório.");
        var lista = listas.criar(identidade.usuarioId(), request.nome());
        return Response.created(uri.getAbsolutePathBuilder().path(lista.id().toString()).build())
                .entity(lista).build();
    }

    @GET
    @Operation(summary = "Listar listas do dono", description = "Paginação no SQL; ordem atualizadaEm DESC, id ASC. Não inclui progresso de compras futuras.")
    @APIResponse(responseCode = "200", description = "Página de listas", content = @Content(schema = @Schema(implementation = OpenApiSchemas.PaginaListas.class)))
    public Pagina<Lista> listar(@Parameter(description = "Página a partir de zero; offset até 2147483647", schema = @Schema(minimum = "0")) @QueryParam("page") @DefaultValue("0") int page,
                              @Parameter(description = "Tamanho de 1 a 100", schema = @Schema(minimum = "1", maximum = "100")) @QueryParam("size") @DefaultValue("20") int size,
                              @Parameter(description = "Substring normalizada, até 120 caracteres; % e _ são literais") @QueryParam("nome") String nome) {
        return listas.listar(identidade.usuarioId(), page, size, nome);
    }

    @GET
    @Path("/{listaId}")
    @Operation(summary = "Consultar lista por UUID")
    @APIResponse(responseCode = "200", description = "Lista encontrada", content = @Content(schema = @Schema(implementation = Lista.class)))
    public Lista buscar(@PathParam("listaId") UUID listaId) {
        return listas.buscar(identidade.usuarioId(), listaId);
    }

    @PATCH
    @Path("/{listaId}")
    @Transactional
    @Operation(summary = "Renomear lista")
    @APIResponse(responseCode = "200", description = "Lista atualizada", content = @Content(schema = @Schema(implementation = Lista.class)))
    public Lista renomear(@PathParam("listaId") UUID listaId, @Valid NomeRequest request) {
        if (request == null) throw ErroDominio.invalido("Corpo JSON obrigatório.");
        return listas.renomear(identidade.usuarioId(), listaId, request.nome());
    }

    @DELETE
    @Path("/{listaId}")
    @Transactional
    @Operation(summary = "Excluir lista e seus itens planejados", description = "Exclusão transacional. Compras e histórico ainda não implementados.")
    @APIResponse(responseCode = "204", description = "Lista removida, sem corpo")
    public Response remover(@PathParam("listaId") UUID listaId) {
        listas.remover(identidade.usuarioId(), listaId);
        return Response.noContent().build();
    }
}
