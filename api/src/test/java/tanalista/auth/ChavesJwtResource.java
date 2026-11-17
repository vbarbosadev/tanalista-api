package tanalista.auth;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPairGenerator;
import java.util.Base64;
import java.util.Map;

/** Ephemeral test keys only: no real provider, committed private key or external network. */
public class ChavesJwtResource implements QuarkusTestResourceLifecycleManager {
    private Path diretorio;
    private Path publica;
    private Path privada;

    @Override
    public Map<String, String> start() {
        try {
            var gerador = KeyPairGenerator.getInstance("RSA");
            gerador.initialize(2048);
            var par = gerador.generateKeyPair();
            diretorio = Files.createTempDirectory("tanalista-jwt-test-");
            publica = diretorio.resolve("public.pem");
            privada = diretorio.resolve("private.pem");
            Files.writeString(privada, "-----BEGIN PRIVATE KEY-----\n"
                    + Base64.getMimeEncoder(64, new byte[]{'\n'}).encodeToString(par.getPrivate().getEncoded())
                    + "\n-----END PRIVATE KEY-----\n");
            Files.writeString(publica, "-----BEGIN PUBLIC KEY-----\n"
                    + Base64.getMimeEncoder(64, new byte[]{'\n'}).encodeToString(par.getPublic().getEncoded())
                    + "\n-----END PUBLIC KEY-----\n");
            return Map.of(
                    "tanalista.auth.private-key-path", privada.toString(),
                    "mp.jwt.verify.publickey.location", publica.toUri().toString(),
                    "tanalista.test.jwt.private-key", Base64.getEncoder().encodeToString(par.getPrivate().getEncoded()));
        } catch (Exception falha) {
            stop();
            throw new IllegalStateException("Não foi possível criar chaves de teste.", falha);
        }
    }

    @Override
    public void stop() {
        try {
            if (publica != null) Files.deleteIfExists(publica);
            if (privada != null) Files.deleteIfExists(privada);
            if (diretorio != null) Files.deleteIfExists(diretorio);
        } catch (Exception falha) {
            throw new IllegalStateException("Não foi possível limpar chaves de teste.", falha);
        }
    }
}
