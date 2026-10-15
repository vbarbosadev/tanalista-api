package tanalista.aplicacao;

import tanalista.dominio.*;
import java.time.Clock;
import java.util.UUID;

public class Listas {
    private final ListaRepository repositorio;
    private final ItemRepository itens;
    private final Clock relogio;

    public Listas(ListaRepository repositorio, ItemRepository itens, Clock relogio) {
        this.repositorio = repositorio;
        this.itens = itens;
        this.relogio = relogio;
    }

    public Lista criar(UUID donoId, String nome) {
        var agora = relogio.instant();
        return repositorio.salvar(new Lista(UUID.randomUUID(), donoId, nome, agora, agora));
    }

    public Lista buscar(UUID donoId, UUID id) {
        return repositorio.buscar(id, donoId).orElseThrow(ErroDominio::ausente);
    }

    public Pagina<Lista> listar(UUID donoId, int page, int size, String nome) {
        Pagina.validar(page, size);
        if (nome != null && nome.length() > 120) throw ErroDominio.invalido("Filtro nome deve ter até 120 caracteres.");
        return repositorio.listar(donoId, page, size, nome);
    }
}
