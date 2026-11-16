package tanalista.dominio;

public class ErroConsultaCodigoBarras extends RuntimeException {
    public enum Tipo { INVALIDO, AUSENTE, INDISPONIVEL, PRAZO_EXCEDIDO, INTERNO }

    private final Tipo tipo;

    public ErroConsultaCodigoBarras(Tipo tipo) {
        super(tipo.name());
        this.tipo = tipo;
    }

    public Tipo tipo() {
        return tipo;
    }
}
