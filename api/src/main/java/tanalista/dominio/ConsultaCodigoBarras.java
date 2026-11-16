package tanalista.dominio;

/** Read-only port. Transport, provider formats and persistence stay outside. */
public interface ConsultaCodigoBarras {
    MetadadosProduto consultar(String codigo);
}
