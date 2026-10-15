package tanalista.adaptadores.http;

import java.util.List;
import tanalista.dominio.Item;
import tanalista.dominio.Lista;

/** Formas concretas apenas para documentar as páginas genéricas sem anotar o domínio. */
public final class OpenApiSchemas {
    private OpenApiSchemas() { }
    public record PaginaListas(List<Lista> items, int page, int size, long total) { }
    public record PaginaItens(List<Item> items, int page, int size, long total) { }
}
