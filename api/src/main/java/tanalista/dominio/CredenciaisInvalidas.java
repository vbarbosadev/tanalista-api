package tanalista.dominio;

public class CredenciaisInvalidas extends RuntimeException {
    public CredenciaisInvalidas() { super("E-mail ou senha inválidos."); }
}
