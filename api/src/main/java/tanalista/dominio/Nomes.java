package tanalista.dominio;

import java.text.Normalizer;
import java.util.Locale;

public final class Nomes {
    private Nomes() { }

    public static String normalizar(String valor) {
        if (valor == null) return "";
        return Normalizer.normalize(valor, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .replaceAll("(?U)\\s+", " ").strip().toLowerCase(Locale.ROOT);
    }

    public static String validar(String nome) {
        if (nome == null || normalizar(nome).isBlank() || nome.length() > 120) {
            throw ErroDominio.invalido("Nome deve conter de 1 a 120 caracteres não vazios.");
        }
        return nome.strip();
    }
}
