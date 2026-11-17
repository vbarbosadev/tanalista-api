package tanalista.config;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

/** UUID v8 from SHA-256 of an unambiguous issuer/subject pair, not email or token text. */
final class IdentificadorExterno {
    private IdentificadorExterno() { }

    static UUID de(String emissor, String sujeito) {
        if (emissor == null || emissor.isBlank() || sujeito == null || sujeito.isBlank()) {
            throw new IllegalArgumentException("Identidade externa incompleta.");
        }
        try {
            var bytes = (emissor.length() + ":" + emissor + sujeito).getBytes(StandardCharsets.UTF_8);
            var hash = MessageDigest.getInstance("SHA-256").digest(bytes);
            hash[6] = (byte) ((hash[6] & 0x0f) | 0x80);
            hash[8] = (byte) ((hash[8] & 0x3f) | 0x80);
            var buffer = ByteBuffer.wrap(hash);
            return new UUID(buffer.getLong(), buffer.getLong());
        } catch (NoSuchAlgorithmException impossivel) {
            throw new IllegalStateException("SHA-256 indisponível.", impossivel);
        }
    }
}
