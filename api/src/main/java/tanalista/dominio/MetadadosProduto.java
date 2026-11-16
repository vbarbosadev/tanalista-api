package tanalista.dominio;

/** Optional suggestions; null labels mean unknown, never defaults for a new Item. */
public record MetadadosProduto(
        String codigo, String nome, String marca, String categoria, String unidade, String fonte) {
}
