package tanalista.adaptadores.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.ValidationException;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import jakarta.ws.rs.ext.*;
import org.jboss.logging.Logger;
import tanalista.dominio.ErroDominio;
import tanalista.dominio.ErroConsultaCodigoBarras;

@Provider
public class ProblemMapper implements ExceptionMapper<Exception> {
    private static final Logger LOG = Logger.getLogger(ProblemMapper.class);
    @Context UriInfo uri;

    @Override
    public Response toResponse(Exception erro) {
        int status;
        String detalhe;
        if (erro instanceof ErroConsultaCodigoBarras consulta) {
            status = switch (consulta.tipo()) {
                case INVALIDO -> 422;
                case AUSENTE -> 404;
                case INDISPONIVEL -> 503;
                case PRAZO_EXCEDIDO -> 504;
                case INTERNO -> 500;
            };
            detalhe = switch (consulta.tipo()) {
                case INVALIDO -> "Código de barras inválido.";
                case AUSENTE -> "Produto não encontrado nas fontes externas.";
                case INDISPONIVEL -> "Consulta indisponível. Você pode adicionar o item manualmente.";
                case PRAZO_EXCEDIDO -> "Prazo da consulta excedido. Você pode adicionar o item manualmente.";
                case INTERNO -> "Não foi possível consultar o produto.";
            };
        } else if (erro instanceof ErroDominio dominio) {
            status = switch (dominio.tipo()) {
                case ENTRADA_INVALIDA -> 422;
                case NAO_ENCONTRADO -> 404;
                case CONFLITO -> 409;
            };
            detalhe = dominio.getMessage();
        } else if (erro instanceof ValidationException) {
            status = 422;
            detalhe = "Entrada inválida. Verifique os campos e os limites documentados.";
        } else if (erro instanceof JsonProcessingException || erro instanceof IllegalArgumentException) {
            status = 400;
            detalhe = "JSON ou parâmetro inválido.";
        } else if (erro instanceof WebApplicationException http) {
            status = http.getResponse().getStatus();
            // Conversão de parâmetro de caminho pode ser exposta como NotFoundException pelo JAX-RS.
            if (erro instanceof NotFoundException && erro.getCause() instanceof IllegalArgumentException) status = 400;
            detalhe = switch (status) {
                case 400 -> "JSON ou parâmetro inválido.";
                case 401 -> "Credencial Bearer ausente ou inválida.";
                case 404 -> "Recurso não encontrado.";
                case 405 -> "Método HTTP não permitido.";
                case 406 -> "Formato de resposta não aceito.";
                case 415 -> "Utilize Content-Type application/json.";
                case 503 -> "Identidade de desenvolvimento desabilitada. Serviço indisponível neste modo.";
                default -> "Não foi possível processar a requisição.";
            };
        } else {
            status = 500;
            detalhe = "Erro interno. Tente novamente mais tarde.";
        }
        if (status >= 500 && status != 503 && status != 504) LOG.error("Falha ao processar requisição HTTP", erro);
        var resposta = ProblemResponses.criar(status, detalhe, uri.getAbsolutePath().getPath());
        if (erro instanceof WebApplicationException http) {
            // Preserva cabeçalhos de protocolo, nunca entidades de erro de terceiros.
            var builder = Response.fromResponse(resposta);
            for (String nome : new String[]{"Allow", "Retry-After", "WWW-Authenticate"}) {
                var valor = http.getResponse().getHeaderString(nome);
                if (valor != null) builder.header(nome, valor);
            }
            return builder.build();
        }
        return resposta;
    }
}
