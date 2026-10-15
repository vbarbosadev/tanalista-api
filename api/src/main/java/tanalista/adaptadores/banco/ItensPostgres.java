package tanalista.adaptadores.banco;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import tanalista.dominio.*;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class ItensPostgres implements ItemRepository, PanacheRepositoryBase<ItemJpa, UUID> {
    @Override
    public Item salvar(Item item) {
        if (count("listaId = ?1 and nomeNormalizado = ?2 and id <> ?3",
                item.listaId(), item.nomeNormalizado(), item.id()) > 0) throw ErroDominio.duplicado();
        try {
            var entidade = findById(item.id());
            if (entidade == null) {
                entidade = new ItemJpa();
                entidade.preencher(item);
                persist(entidade);
            } else {
                entidade.preencher(item);
            }
            flush();
            return entidade.dominio();
        } catch (RuntimeException erro) {
            throw ConflitosPostgres.traduzir(erro);
        }
    }

    @Override
    public Optional<Item> buscar(UUID id, UUID listaId) {
        return find("id = ?1 and listaId = ?2", id, listaId).firstResultOptional().map(ItemJpa::dominio);
    }

    @Override
    public Pagina<Item> listar(UUID listaId, int page, int size, String nome, String categoria) {
        Pagina.validar(page, size);
        String hql = "listaId = ?1 and locate(?2, nomeNormalizado) > 0";
        var consulta = categoria == null
                ? find(hql + " order by nomeNormalizado, id", listaId, Nomes.normalizar(nome))
                : find(hql + " and categoria = ?3 order by nomeNormalizado, id", listaId, Nomes.normalizar(nome), categoria);
        long total = consulta.count();
        var items = consulta.page(page, size).list().stream().map(ItemJpa::dominio).toList();
        return new Pagina<>(items, page, size, total);
    }

    @Override public void remover(UUID id) { deleteById(id); }
    @Override public void removerDaLista(UUID listaId) { delete("listaId", listaId); }
}
