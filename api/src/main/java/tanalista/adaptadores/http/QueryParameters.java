package tanalista.adaptadores.http;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.core.UriInfo;
import tanalista.dominio.ErroDominio;
import java.util.Set;

final class QueryParameters {
    private QueryParameters() { }

    static void validar(UriInfo uri, Set<String> permitidos) {
        uri.getQueryParameters().forEach((nome, valores) -> {
            if (!permitidos.contains(nome) || valores.size() != 1) throw new BadRequestException();
            var valor = valores.getFirst();
            if ((nome.equals("page") || nome.equals("size")) && (valor == null || valor.isBlank())) {
                throw new BadRequestException();
            }
            if (nome.equals("categoria") && (valor == null || valor.isBlank())) {
                throw ErroDominio.invalido("Filtro categoria não pode ser vazio.");
            }
        });
    }
}
