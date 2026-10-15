package tanalista.dominio;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface ListaRepository {
    Lista salvar(Lista lista);
    Optional<Lista> buscar(UUID id, UUID donoId);
    Pagina<Lista> listar(UUID donoId, int page, int size, String nome);
    void remover(UUID id);
    void tocar(UUID id, Instant instante);
}
