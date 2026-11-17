package tanalista.adaptadores.seguranca;

import jakarta.enterprise.context.ApplicationScoped;
import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;
import tanalista.dominio.Senhas;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

/** OWASP minimum Argon2id: 19 MiB, two passes, one lane, salt 128 bits. */
@ApplicationScoped
public class SenhasArgon2 implements Senhas {
    private static final String PREFIXO = "$argon2id$v=19$m=19456,t=2,p=1$";
    private final SecureRandom aleatorio = new SecureRandom();

    @Override
    public String gerarHash(String senha) {
        byte[] salt = new byte[16];
        aleatorio.nextBytes(salt);
        byte[] resultado = derivar(senha, salt);
        try {
            var base64 = Base64.getEncoder().withoutPadding();
            return PREFIXO + base64.encodeToString(salt) + "$" + base64.encodeToString(resultado);
        } finally { Arrays.fill(resultado, (byte) 0); }
    }

    @Override
    public boolean confere(String senha, String hash) {
        if (senha == null || hash == null || !hash.startsWith(PREFIXO) || hash.length() > 200) return false;
        byte[] resultado = null;
        try {
            var partes = hash.substring(PREFIXO.length()).split("\\$", -1);
            if (partes.length != 2) return false;
            var salt = Base64.getDecoder().decode(partes[0]);
            var esperado = Base64.getDecoder().decode(partes[1]);
            if (salt.length != 16 || esperado.length != 32) return false;
            resultado = derivar(senha, salt);
            return MessageDigest.isEqual(resultado, esperado);
        } catch (IllegalArgumentException invalido) {
            return false;
        } finally {
            if (resultado != null) Arrays.fill(resultado, (byte) 0);
        }
    }

    private byte[] derivar(String senha, byte[] salt) {
        char[] caracteres = senha.toCharArray();
        var parametros = new Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
                .withVersion(Argon2Parameters.ARGON2_VERSION_13).withMemoryAsKB(19456)
                .withIterations(2).withParallelism(1).withSalt(salt).build();
        var gerador = new Argon2BytesGenerator();
        byte[] resultado = new byte[32];
        try {
            gerador.init(parametros);
            gerador.generateBytes(caracteres, resultado);
            return resultado;
        } finally {
            Arrays.fill(caracteres, '\0');
            parametros.clear();
        }
    }
}
