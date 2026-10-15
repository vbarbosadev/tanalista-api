package tanalista.adaptadores.banco;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import tanalista.dominio.*;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class ListasPostgres implements ListaRepository, PanacheRepositoryBase<ListaJpa, UUID> {
    @Override
    public Lista salvar(Lista lista) {
        if (count("donoId = ?1 and nomeNormalizado = ?2 and id <> ?3",
                lista.donoId(), lista.nomeNormalizado(), lista.id()) > 0) throw ErroDominio.duplicado();
        try {
            var entidade = findById(lista.id());
            if (entidade == null) {
                entidade = new ListaJpa();
                entidade.preencher(lista);
                persist(entidade);
            } else {
                entidade.preencher(lista);
            }
            flush();
            return entidade.dominio();
        } catch (RuntimeException erro) {
            throw ConflitosPostgres.traduzir(erro);
        }
    }

    @Override
    public Optional<Lista> buscar(UUID id, UUID donoId) {
        return find("id = ?1 and donoId = ?2", id, donoId).firstResultOptional().map(ListaJpa::dominio);
    }

    @Override
    public Pagina<Lista> listar(UUID donoId, int page, int size, String nome) {
        Pagina.validar(page, size);
        var consulta = find("donoId = ?1 and locate(?2, nomeNormalizado) > 0 order by atualizadaEm desc, id",
                donoId, Nomes.normalizar(nome));
        long total = consulta.count();
        var items = consulta.page(page, size).list().stream().map(ListaJpa::dominio).toList();
        return new Pagina<>(items, page, size, total);
    }

    @Override public void remover(UUID id) { deleteById(id); }
    @Override public void tocar(UUID id, Instant instante) {
        update("atualizadaEm = ?1 where id = ?2", instante, id);
    }
}
