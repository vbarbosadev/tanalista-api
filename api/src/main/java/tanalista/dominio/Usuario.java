package tanalista.dominio;

import java.time.Instant;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

/** Internal credential record. Never return this object from HTTP. */
public record Usuario(UUID id, String email, String senhaHash, Instant criadoEm) {
    private static final Pattern EMAIL = Pattern.compile(
            "[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*"
                    + "@[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?"
                    + "(?:\\.[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?)+");

    public Usuario {
        Objects.requireNonNull(id);
        Objects.requireNonNull(criadoEm);
        email = normalizarEmail(email);
        if (senhaHash == null || senhaHash.isBlank()) throw new IllegalArgumentException("Hash obrigatório.");
    }

    public static String normalizarEmail(String email) {
        if (email == null || email.length() > 254) throw ErroDominio.invalido("E-mail inválido.");
        var normalizado = email.strip().toLowerCase(Locale.ROOT);
        if (!EMAIL.matcher(normalizado).matches() || normalizado.indexOf('@') > 64) {
            throw ErroDominio.invalido("E-mail inválido.");
        }
        return normalizado;
    }

    public static void validarSenha(String senha) {
        if (senha == null || senha.length() < 12 || senha.length() > 128) {
            throw ErroDominio.invalido("Senha deve conter de 12 a 128 caracteres.");
        }
    }

    @Override public String toString() { return "Usuario[credenciais omitidas]"; }
}
