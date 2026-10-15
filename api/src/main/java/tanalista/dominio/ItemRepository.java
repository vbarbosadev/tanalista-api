package tanalista.dominio;

import java.util.Optional;
import java.util.UUID;

public interface ItemRepository {
    Item salvar(Item item);
    Optional<Item> buscar(UUID id, UUID listaId);
    Pagina<Item> listar(UUID listaId, int page, int size, String nome, String categoria);
    void remover(UUID id);
    void removerDaLista(UUID listaId);
}
