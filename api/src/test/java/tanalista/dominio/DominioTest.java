package tanalista.dominio;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DominioTest {
    private final UUID id = UUID.randomUUID();
    private final UUID lista = UUID.randomUUID();

    private Item item(String nome, String quantidade, String preco, String unidade, String categoria) {
        return new Item(id, lista, nome, quantidade == null ? null : new BigDecimal(quantidade),
                unidade, categoria, preco == null ? null : new BigDecimal(preco));
    }

    @Test
    void normalizaNomesSemConfundirProdutosDistintos() {
        assertEquals("cafe da manha", Nomes.normalizar("  CAFÉ\t da\n MANHÃ "));
        assertEquals("", Nomes.normalizar(null));
        assertEquals("Café", Nomes.validar(" Café "));
        assertNotEquals(Nomes.normalizar("Leite 200g"), Nomes.normalizar("Leite 400g"));
        for (String nome : new String[]{null, "", " \t\n", "\u0301", "a".repeat(121)}) {
            var erro = assertThrows(ErroDominio.class, () -> Nomes.validar(nome));
            assertEquals(ErroDominio.Tipo.ENTRADA_INVALIDA, erro.tipo());
        }
        assertEquals(120, Nomes.validar("a".repeat(120)).length());
    }

    @Test
    void listaPreservaIdentidadeETempos() {
        var agora = Instant.parse("2026-11-16T12:00:00Z");
        var l = new Lista(id, lista, "  CAFÉ ", agora, agora);
        assertEquals("CAFÉ", l.nome());
        assertEquals("cafe", l.nomeNormalizado());
        assertEquals(id, l.id());
        assertEquals(lista, l.donoId());
        assertEquals(agora, l.criadaEm());
        assertThrows(NullPointerException.class, () -> new Lista(null, lista, "Nome", agora, agora));
        assertThrows(NullPointerException.class, () -> new Lista(id, null, "Nome", agora, agora));
        assertThrows(NullPointerException.class, () -> new Lista(id, lista, "Nome", null, agora));
        assertThrows(NullPointerException.class, () -> new Lista(id, lista, "Nome", agora, null));
    }

    @Test
    void itemAceitaLimitesEOpcionaisSemInventarValores() {
        var completo = item(" Café ", "999999999.999", "999999999.99", " UN ", " Alimentos ");
        assertEquals("cafe", completo.nomeNormalizado());
        assertEquals("UN", completo.unidade());
        assertEquals("Alimentos", completo.categoria());
        assertEquals(new BigDecimal("999999999.999"), completo.quantidade());
        assertEquals(new BigDecimal("999999999.99"), completo.precoEstimado());
        var minimo = item("Produto", null, "0.00", null, null);
        assertNull(minimo.quantidade());
        assertNull(minimo.unidade());
        assertNull(minimo.categoria());
        assertNull(item("Produto", "0.001", null, "x".repeat(30), "x".repeat(60)).precoEstimado());
        assertThrows(NullPointerException.class, () -> new Item(null, lista, "Nome", null, null, null, null));
        assertThrows(NullPointerException.class, () -> new Item(id, null, "Nome", null, null, null, null));
    }

    @Test
    void itemRejeitaNumerosEFaixasInvalidas() {
        for (var quantidade : List.of("0", "-1", "0.0001", "1000000000", "1E+10")) {
            assertThrows(ErroDominio.class, () -> item("Produto", quantidade, null, null, null), quantidade);
        }
        for (var preco : List.of("-0.01", "0.001", "1000000000", "1E+10")) {
            assertThrows(ErroDominio.class, () -> item("Produto", null, preco, null, null), preco);
        }
        for (var unidade : List.of("", " ", "x".repeat(31))) {
            assertThrows(ErroDominio.class, () -> item("Produto", null, null, unidade, null));
        }
        for (var categoria : List.of("", "\t", "x".repeat(61))) {
            assertThrows(ErroDominio.class, () -> item("Produto", null, null, null, categoria));
        }
        assertThrows(ErroDominio.class, () -> item("", null, null, null, null));
    }

    @Test
    void paginacaoLimitaOffsetECopiaConteudo() {
        for (var params : new int[][]{{-1, 20}, {0, 0}, {0, 101}, {Integer.MAX_VALUE, 2}}) {
            assertThrows(ErroDominio.class, () -> Pagina.validar(params[0], params[1]));
        }
        Pagina.validar(Integer.MAX_VALUE, 1);
        Pagina.validar(0, 100);
        var origem = new ArrayList<>(List.of("a"));
        var pagina = new Pagina<>(origem, 0, 20, 1);
        origem.clear();
        assertEquals(List.of("a"), pagina.items());
        assertThrows(UnsupportedOperationException.class, () -> pagina.items().add("b"));
    }
}
