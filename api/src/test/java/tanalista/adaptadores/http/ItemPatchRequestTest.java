package tanalista.adaptadores.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import tanalista.dominio.ErroDominio;
import tanalista.dominio.Item;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/** Adapter-level unit test for JSON presence, without starting HTTP or Quarkus. */
class ItemPatchRequestTest {
    private final ObjectMapper json = new ObjectMapper();
    private final Item atual = new Item(UUID.randomUUID(), UUID.randomUUID(), "Café",
            new BigDecimal("2"), "UN", "Alimentos", new BigDecimal("6.50"));

    private Item atualizar(String body) throws Exception {
        return json.readValue(body, ItemPatchRequest.class).mesclar(atual).item(atual.id(), atual.listaId());
    }

    @Test
    void ausenciaMantemENuloLimpaSomenteOpcionais() throws Exception {
        var atualizado = atualizar("{\"quantidade\":3,\"categoria\":null}");
        assertEquals(atual.id(), atualizado.id());
        assertEquals(atual.nome(), atualizado.nome());
        assertEquals(new BigDecimal("3"), atualizado.quantidade());
        assertNull(atualizado.categoria());
        assertEquals(atual.unidade(), atualizado.unidade());
        assertEquals(atual.precoEstimado(), atualizado.precoEstimado());
        var limpo = atualizar("{\"quantidade\":null,\"precoEstimado\":null,\"unidade\":null}");
        assertNull(limpo.quantidade());
        assertNull(limpo.precoEstimado());
        assertNull(limpo.unidade());
        assertEquals(atual.categoria(), limpo.categoria());
    }

    @Test
    void rejeitaPatchVazioNomeNuloEValoresInvalidos() {
        for (var body : new String[]{"{}", "{\"nome\":null}", "{\"quantidade\":0}", "{\"nome\":\"\"}"}) {
            assertThrows(ErroDominio.class, () -> atualizar(body), body);
        }
    }
}
