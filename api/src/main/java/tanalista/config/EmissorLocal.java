package tanalista.config;

import io.quarkus.runtime.StartupEvent;
import io.smallrye.jwt.build.Jwt;
import io.smallrye.jwt.auth.principal.JWTParser;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

@ApplicationScoped
public class EmissorLocal {
    @Inject ConfiguracaoAutenticacao configuracao;
    @Inject JWTParser verificador;
    @ConfigProperty(name = "tanalista.auth.private-key-path") String caminho;
    @ConfigProperty(name = "mp.jwt.verify.issuer") String emissor;
    @ConfigProperty(name = "mp.jwt.verify.audiences") String audiencia;
    private RSAPrivateKey chave;

    void iniciar(@Observes StartupEvent evento) {
        if (!configuracao.local()) return;
        try {
            String pem = Files.readString(Path.of(caminho));
            var bytes = Base64.getMimeDecoder().decode(pem.replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", ""));
            try {
                chave = (RSAPrivateKey) KeyFactory.getInstance("RSA")
                        .generatePrivate(new PKCS8EncodedKeySpec(bytes));
            } finally { java.util.Arrays.fill(bytes, (byte) 0); }
            if (chave.getModulus().bitLength() < 2048) throw new IllegalArgumentException();
            // Refuse mismatched signing and verification keys before serving any login.
            verificador.parse(emitir(UUID.randomUUID()));
        } catch (Exception invalida) {
            throw new IllegalStateException("Configuração de chaves RSA local inválida.");
        }
    }

    public String emitir(UUID usuario) {
        if (!configuracao.local() || chave == null) {
            throw new IllegalStateException("Emissão local não configurada.");
        }
        long agora = Instant.now().getEpochSecond();
        return Jwt.issuer(emissor).subject(usuario.toString()).audience(audiencia)
                .issuedAt(agora).expiresAt(agora + 1800).claim("jti", UUID.randomUUID().toString())
                .sign(chave);
    }
}
