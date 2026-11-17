package tanalista.dominio;

public interface Senhas {
    String gerarHash(String senha);
    boolean confere(String senha, String hash);
}
