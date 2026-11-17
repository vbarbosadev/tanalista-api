package tanalista.adaptadores.http;

import io.smallrye.common.annotation.Blocking;
import io.vertx.core.http.HttpServerRequest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import tanalista.aplicacao.Usuarios;
import tanalista.config.*;
import tanalista.dominio.*;
import java.util.UUID;

@Path("/api/v1/auth")
@Blocking
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Autenticação", description = "Cadastro aberto com e-mail não verificado e senha; somente com LOCAL_AUTH_ENABLED=true.")
@APIResponses({
    @APIResponse(responseCode = "400", description = "JSON inválido",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = Problem.class))),
    @APIResponse(responseCode = "401", description = "Credenciais inválidas; WWW-Authenticate: Bearer",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = Problem.class))),
    @APIResponse(responseCode = "409", description = "E-mail já cadastrado",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = Problem.class))),
    @APIResponse(responseCode = "422", description = "Entrada inválida",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = Problem.class))),
    @APIResponse(responseCode = "429", description = "Limite excedido; Retry-After em segundos",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = Problem.class))),
    @APIResponse(responseCode = "503", description = "Autenticação local desabilitada",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = Problem.class)))
})
public class AuthResource {
    public record Credenciais(
            @Schema(required = true, maxLength = 254, format = "email") String email,
            @Schema(required = true, minLength = 12, maxLength = 128, writeOnly = true, format = "password") String senha) {
        @Override public String toString() { return "Credenciais[omitidas]"; }
    }
    public record Perfil(UUID id, String email) { }
    public record Token(String access_token, String token_type, long expires_in) {
        @Override public String toString() { return "Token[omitido]"; }
    }

    @Inject Usuarios usuarios;
    @Inject ConfiguracaoAutenticacao configuracao;
    @Inject IdentidadeAtual identidade;
    @Inject EmissorLocal emissor;
    @Inject LimiteAutenticacao limite;
    @Inject HttpServerRequest requisicao;

    private void conferir(Credenciais dados, boolean cadastro) {
        if (!configuracao.local()) throw new ServiceUnavailableException();
        if (dados == null) throw ErroDominio.invalido("Corpo JSON obrigatório.");
        // Bound input before normalization, hashing and repository work.
        if ((dados.email != null && dados.email.length() > 254)
                || (dados.senha != null && dados.senha.length() > 128)) {
            if (!cadastro) throw new CredenciaisInvalidas();
            throw ErroDominio.invalido("Credenciais fora dos limites.");
        }
        limite.conferir(requisicao.remoteAddress().host(), dados.email, cadastro);
    }

    @POST
    @Path("/cadastro")
    @Transactional
    @Operation(summary = "Cadastrar usuário com e-mail e senha")
    @APIResponse(responseCode = "201", description = "Usuário criado; e-mail não verificado",
            content = @Content(schema = @Schema(implementation = Perfil.class)))
    public Response cadastrar(Credenciais dados) {
        conferir(dados, true);
        limite.adquirir();
        try {
            var usuario = usuarios.cadastrar(dados.email, dados.senha);
            return Response.status(201).entity(new Perfil(usuario.id(), usuario.email()))
                    .header("Cache-Control", "no-store").build();
        } finally { limite.liberar(); }
    }

    @POST
    @Path("/login")
    @Operation(summary = "Obter access token; validade de 30 minutos")
    @APIResponse(responseCode = "200", description = "Bearer JWT emitido",
            content = @Content(schema = @Schema(implementation = Token.class)))
    public Response login(Credenciais dados) {
        conferir(dados, false);
        limite.adquirir();
        try {
            var usuario = usuarios.autenticar(dados.email, dados.senha);
            return Response.ok(new Token(emissor.emitir(usuario.id()), "Bearer", 1800))
                    .header("Cache-Control", "no-store").header("Pragma", "no-cache").build();
        } finally { limite.liberar(); }
    }

    @GET
    @Path("/me")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Consultar perfil do usuário autenticado local")
    @APIResponse(responseCode = "200", description = "ID e e-mail não verificado",
            content = @Content(schema = @Schema(implementation = Perfil.class)))
    public Response perfil() {
        if (!configuracao.local()) throw new ServiceUnavailableException();
        var usuario = usuarios.buscar(identidade.usuarioId());
        return Response.ok(new Perfil(usuario.id(), usuario.email())).header("Cache-Control", "no-store").build();
    }
}
