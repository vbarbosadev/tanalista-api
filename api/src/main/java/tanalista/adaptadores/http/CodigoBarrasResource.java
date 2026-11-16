package tanalista.adaptadores.http;

import io.smallrye.common.annotation.Blocking;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.*;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.*;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import tanalista.config.IdentidadeDesenvolvimento;
import tanalista.dominio.ConsultaCodigoBarras;
import tanalista.dominio.MetadadosProduto;

@Path("/api/v1/codigos-barras")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Código de barras", description = "Consulta opcional; não persiste catálogo nem cria Item.")
@APIResponses({
    @APIResponse(responseCode = "404", description = "Produto não encontrado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = Problem.class))),
    @APIResponse(responseCode = "422", description = "EAN/UPC inválido", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = Problem.class))),
    @APIResponse(responseCode = "500", description = "Falha interna", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = Problem.class))),
    @APIResponse(responseCode = "503", description = "Consulta indisponível ou identidade de desenvolvimento desabilitada", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = Problem.class))),
    @APIResponse(responseCode = "504", description = "Prazo da consulta excedido", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = Problem.class)))
})
public class CodigoBarrasResource {
    private final ConsultaCodigoBarras consulta;
    private final IdentidadeDesenvolvimento identidade;

    public CodigoBarrasResource(ConsultaCodigoBarras consulta, IdentidadeDesenvolvimento identidade) {
        this.consulta = consulta;
        this.identidade = identidade;
    }

    @GET
    @Path("/{codigo}")
    @Blocking
    @Operation(summary = "Consultar sugestões por EAN-8, UPC-A ou EAN-13",
            description = "Somente leitura, com prazo. Categoria é rótulo externo; campos opcionais desconhecidos são nulos. Criação do Item é separada.")
    @APIResponse(responseCode = "200", description = "Metadados encontrados", content = @Content(schema = @Schema(implementation = MetadadosProduto.class)))
    public MetadadosProduto consultar(
            @Parameter(description = "Código como string; preserva zeros à esquerda", required = true)
            @PathParam("codigo") String codigo) {
        identidade.usuarioId();
        return consulta.consultar(codigo);
    }
}
