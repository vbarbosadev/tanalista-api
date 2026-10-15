package tanalista.adaptadores.memoria;

import tanalista.dominio.*;
import java.util.*;

public class ItensEmMemoria implements ItemRepository {
    private final Map<UUID, Item> dados = new HashMap<>();

    @Override
    public synchronized Item salvar(Item item) {
        if (dados.values().stream().anyMatch(i -> !i.id().equals(item.id())
                && i.listaId().equals(item.listaId()) && i.nomeNormalizado().equals(item.nomeNormalizado()))) {
            throw ErroDominio.duplicado();
        }
        dados.put(item.id(), item);
        return item;
    }

    @Override
    public synchronized Optional<Item> buscar(UUID id, UUID listaId) {
        return Optional.ofNullable(dados.get(id)).filter(i -> i.listaId().equals(listaId));
    }

    @Override
    public synchronized Pagina<Item> listar(UUID listaId, int page, int size, String nome, String categoria) {
        Pagina.validar(page, size);
        var filtrados = dados.values().stream().filter(i -> i.listaId().equals(listaId))
                .filter(i -> i.nomeNormalizado().contains(Nomes.normalizar(nome)))
                .filter(i -> categoria == null || categoria.equals(i.categoria()))
                .sorted(Comparator.comparing(Item::nomeNormalizado).thenComparing(Item::id)).toList();
        return new Pagina<>(filtrados.stream().skip((long) page * size).limit(size).toList(), page, size, filtrados.size());
    }

    @Override public synchronized void remover(UUID id) { dados.remove(id); }
    @Override public synchronized void removerDaLista(UUID listaId) {
        dados.values().removeIf(i -> i.listaId().equals(listaId));
    }
}
