package tanalista.dominio;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record Lista(UUID id, UUID donoId, String nome, Instant criadaEm, Instant atualizadaEm) {
    public Lista {
        Objects.requireNonNull(id);
        Objects.requireNonNull(donoId);
        Objects.requireNonNull(criadaEm);
        Objects.requireNonNull(atualizadaEm);
        nome = Nomes.validar(nome);
    }

    public String nomeNormalizado() { return Nomes.normalizar(nome); }
}
