package tanalista.aplicacao;

import tanalista.dominio.*;
import java.math.BigDecimal;
import java.time.Clock;
import java.util.UUID;

public class Itens {
    public record Dados(String nome, BigDecimal quantidade, String unidade, String categoria, BigDecimal precoEstimado) {
        public Item item(UUID id, UUID listaId) {
            return new Item(id, listaId, nome, quantidade, unidade, categoria, precoEstimado);
        }
    }

    private final Listas listas;
    private final ListaRepository listasRepo;
    private final ItemRepository repositorio;
    private final Clock relogio;

    public Itens(Listas listas, ListaRepository listasRepo, ItemRepository repositorio, Clock relogio) {
        this.listas = listas;
        this.listasRepo = listasRepo;
        this.repositorio = repositorio;
        this.relogio = relogio;
    }

    public Item criar(UUID donoId, UUID listaId, Dados dados) {
        listas.buscar(donoId, listaId);
        var item = repositorio.salvar(dados.item(UUID.randomUUID(), listaId));
        listasRepo.tocar(listaId, relogio.instant());
        return item;
    }

    public Item buscar(UUID donoId, UUID listaId, UUID id) {
        listas.buscar(donoId, listaId);
        return repositorio.buscar(id, listaId).orElseThrow(ErroDominio::ausente);
    }
}
