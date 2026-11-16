package tanalista.aplicacao;

import org.junit.jupiter.api.Test;
import tanalista.adaptadores.memoria.ItensEmMemoria;
import tanalista.adaptadores.memoria.ListasEmMemoria;
import tanalista.dominio.*;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/** Plain JUnit: no Quarkus bootstrap, database, gRPC or external network. */
class CasosDeUsoTest {
    private static final Instant INICIO = Instant.parse("2026-11-16T12:00:00Z");
    private final UUID dono = UUID.randomUUID(), outro = UUID.randomUUID();
    private final ListasEmMemoria listasRepo = new ListasEmMemoria();
    private final ItensEmMemoria itensRepo = new ItensEmMemoria();
    private final Listas listas = new Listas(listasRepo, itensRepo, Clock.fixed(INICIO, ZoneOffset.UTC));
    private final Itens itens = new Itens(listas, listasRepo, itensRepo,
            Clock.fixed(INICIO.plusSeconds(60), ZoneOffset.UTC));

    private static Itens.Dados dados(String nome) {
        return new Itens.Dados(nome, null, null, null, null);
    }

    private static void erro(ErroDominio.Tipo tipo, Runnable acao) {
        assertEquals(tipo, assertThrows(ErroDominio.class, acao::run).tipo());
    }

    @Test
    void criaConsultaRenomeiaEPaginaListasComEscopoDoDono() {
        var a = listas.criar(dono, " Café ");
        var b = listas.criar(dono, "Mercado");
        listas.criar(outro, "Café");
        assertEquals(INICIO, a.criadaEm());
        assertEquals(dono, a.donoId());
        assertEquals(a, listas.buscar(dono, a.id()));
        erro(ErroDominio.Tipo.CONFLITO, () -> listas.criar(dono, "  CAFE "));
        assertEquals(List.of(a), listas.listar(dono, 0, 20, "cafe").items());
        assertEquals(2, listas.listar(dono, 0, 1, null).total());
        assertEquals(1, listas.listar(dono, 1, 1, null).items().size());
        assertTrue(listas.listar(dono, 10, 1, null).items().isEmpty());
        var depois = new Listas(listasRepo, itensRepo, Clock.fixed(INICIO.plusSeconds(120), ZoneOffset.UTC));
        var renomeada = depois.renomear(dono, a.id(), "Reserva");
        assertEquals(a.id(), renomeada.id());
        assertEquals(INICIO, renomeada.criadaEm());
        assertEquals(INICIO.plusSeconds(120), renomeada.atualizadaEm());
        erro(ErroDominio.Tipo.CONFLITO, () -> listas.renomear(dono, a.id(), b.nome()));
        assertEquals(renomeada, listas.buscar(dono, a.id()));
        erro(ErroDominio.Tipo.ENTRADA_INVALIDA, () -> listas.listar(dono, 0, 20, "a".repeat(121)));
        erro(ErroDominio.Tipo.ENTRADA_INVALIDA, () -> listas.listar(dono, -1, 20, null));
    }

    @Test
    void outroDonoNaoLeAlteraOuExcluiListaNemSeusItens() {
        var l = listas.criar(dono, "Lista");
        var item = itens.criar(dono, l.id(), dados("Arroz"));
        for (Runnable acao : List.<Runnable>of(
                () -> listas.buscar(outro, l.id()),
                () -> listas.renomear(outro, l.id(), "Invadida"),
                () -> listas.remover(outro, l.id()),
                () -> itens.criar(outro, l.id(), dados("Feijão")),
                () -> itens.buscar(outro, l.id(), item.id()),
                () -> itens.atualizar(outro, l.id(), item.id(), dados("Outro")),
                () -> itens.remover(outro, l.id(), item.id()),
                () -> itens.listar(outro, l.id(), 0, 20, null, null))) {
            erro(ErroDominio.Tipo.NAO_ENCONTRADO, acao);
        }
        assertEquals(0, listas.listar(outro, 0, 20, null).total());
        assertEquals(item, itens.buscar(dono, l.id(), item.id()));
        assertEquals("Lista", listas.buscar(dono, l.id()).nome());
    }

    @Test
    void itensTemUnicidadePorListaFiltrosEDataDeAlteracao() {
        var a = listas.criar(dono, "A");
        var b = listas.criar(dono, "B");
        var cafe = itens.criar(dono, a.id(), new Itens.Dados(
                "Café", new BigDecimal("2.000"), "UN", "Alimentos", new BigDecimal("6.50")));
        var leite = itens.criar(dono, a.id(), dados("Leite 200g"));
        itens.criar(dono, b.id(), dados("Café"));
        erro(ErroDominio.Tipo.CONFLITO, () -> itens.criar(dono, a.id(), dados("  CAFE ")));
        assertEquals(INICIO.plusSeconds(60), listas.buscar(dono, a.id()).atualizadaEm());
        assertEquals(List.of(cafe), itens.listar(dono, a.id(), 0, 20, "cafe", " Alimentos ").items());
        assertEquals(0, itens.listar(dono, a.id(), 0, 20, null, "alimentos").total());
        assertEquals(2, itens.listar(dono, a.id(), 0, 1, null, null).total());
        assertEquals(List.of(leite), itens.listar(dono, a.id(), 1, 1, null, null).items());
        assertTrue(itens.listar(dono, a.id(), 2, 1, null, null).items().isEmpty());
        erro(ErroDominio.Tipo.NAO_ENCONTRADO, () -> itens.buscar(dono, b.id(), cafe.id()));
        erro(ErroDominio.Tipo.CONFLITO, () -> itens.atualizar(dono, a.id(), leite.id(), dados("café")));
        var alterado = itens.atualizar(dono, a.id(), cafe.id(),
                new Itens.Dados(cafe.nome(), BigDecimal.ONE, cafe.unidade(), null, cafe.precoEstimado()));
        assertEquals(cafe.id(), alterado.id());
        assertEquals(cafe.precoEstimado(), alterado.precoEstimado());
        assertNull(alterado.categoria());
        assertEquals(BigDecimal.ONE, alterado.quantidade());
        itens.remover(dono, a.id(), cafe.id());
        erro(ErroDominio.Tipo.NAO_ENCONTRADO, () -> itens.buscar(dono, a.id(), cafe.id()));
        erro(ErroDominio.Tipo.NAO_ENCONTRADO, () -> itens.remover(dono, a.id(), cafe.id()));
        assertEquals(1, itens.listar(dono, b.id(), 0, 20, null, null).total());
    }

    @Test
    void excluirListaRemoveSomenteItensDaLista() {
        var a = listas.criar(dono, "A");
        var b = listas.criar(dono, "B");
        var removido = itens.criar(dono, a.id(), dados("Arroz"));
        var preservado = itens.criar(dono, b.id(), dados("Arroz"));
        listas.remover(dono, a.id());
        assertTrue(itensRepo.buscar(removido.id(), a.id()).isEmpty());
        assertEquals(preservado, itens.buscar(dono, b.id(), preservado.id()));
        erro(ErroDominio.Tipo.NAO_ENCONTRADO, () -> listas.buscar(dono, a.id()));
        erro(ErroDominio.Tipo.NAO_ENCONTRADO, () -> listas.remover(dono, a.id()));
        erro(ErroDominio.Tipo.NAO_ENCONTRADO, () -> itens.criar(dono, UUID.randomUUID(), dados("Ausente")));
    }

    @Test
    void rejeitaFiltrosELimitesAntesDeConsultarPagina() {
        var l = listas.criar(dono, "Lista");
        for (var categoria : List.of("", " ", "x".repeat(61))) {
            erro(ErroDominio.Tipo.ENTRADA_INVALIDA,
                    () -> itens.listar(dono, l.id(), 0, 20, null, categoria));
        }
        erro(ErroDominio.Tipo.ENTRADA_INVALIDA,
                () -> itens.listar(dono, l.id(), 0, 20, "x".repeat(121), null));
        erro(ErroDominio.Tipo.ENTRADA_INVALIDA,
                () -> itens.listar(dono, l.id(), Integer.MAX_VALUE, 100, null, null));
        erro(ErroDominio.Tipo.ENTRADA_INVALIDA, () -> itens.criar(dono, l.id(), dados("")));
        assertEquals(0, itens.listar(dono, l.id(), 0, 20, null, null).total());
        assertEquals(INICIO, listas.buscar(dono, l.id()).atualizadaEm());
    }
}
