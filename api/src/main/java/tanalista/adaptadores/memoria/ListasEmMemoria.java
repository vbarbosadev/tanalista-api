package tanalista.adaptadores.memoria;

import tanalista.dominio.*;
import java.time.Instant;
import java.util.*;

/** Adaptador sem CDI, disponível para testes e composição explícita. */
public class ListasEmMemoria implements ListaRepository {
    private final Map<UUID, Lista> dados = new HashMap<>();

    @Override
    public synchronized Lista salvar(Lista lista) {
        if (dados.values().stream().anyMatch(l -> !l.id().equals(lista.id())
                && l.donoId().equals(lista.donoId()) && l.nomeNormalizado().equals(lista.nomeNormalizado()))) {
            throw ErroDominio.duplicado();
        }
        dados.put(lista.id(), lista);
        return lista;
    }

    @Override
    public synchronized Optional<Lista> buscar(UUID id, UUID donoId) {
        return Optional.ofNullable(dados.get(id)).filter(l -> l.donoId().equals(donoId));
    }

    @Override
    public synchronized Pagina<Lista> listar(UUID donoId, int page, int size, String nome) {
        Pagina.validar(page, size);
        var filtradas = dados.values().stream().filter(l -> l.donoId().equals(donoId))
                .filter(l -> l.nomeNormalizado().contains(Nomes.normalizar(nome)))
                .sorted(Comparator.comparing(Lista::atualizadaEm).reversed().thenComparing(Lista::id)).toList();
        return new Pagina<>(filtradas.stream().skip((long) page * size).limit(size).toList(), page, size, filtradas.size());
    }

    @Override public synchronized void remover(UUID id) { dados.remove(id); }

    @Override
    public synchronized void tocar(UUID id, Instant instante) {
        var l = dados.get(id);
        if (l != null) dados.put(id, new Lista(l.id(), l.donoId(), l.nome(), l.criadaEm(), instante));
    }
}
