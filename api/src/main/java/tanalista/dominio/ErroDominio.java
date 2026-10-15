package tanalista.dominio;

public class ErroDominio extends RuntimeException {
    public enum Tipo { ENTRADA_INVALIDA, NAO_ENCONTRADO, CONFLITO }

    private final Tipo tipo;

    public ErroDominio(Tipo tipo, String mensagem) {
        super(mensagem);
        this.tipo = tipo;
    }

    public Tipo tipo() { return tipo; }

    public static ErroDominio invalido(String mensagem) {
        return new ErroDominio(Tipo.ENTRADA_INVALIDA, mensagem);
    }

    public static ErroDominio ausente() {
        return new ErroDominio(Tipo.NAO_ENCONTRADO, "Recurso não encontrado.");
    }

    public static ErroDominio duplicado() {
        return new ErroDominio(Tipo.CONFLITO, "Nome já utilizado neste contexto.");
    }
}
