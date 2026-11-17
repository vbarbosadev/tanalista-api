package tanalista.adaptadores.http;

import org.junit.jupiter.api.Test;
import jakarta.ws.rs.WebApplicationException;
import java.time.*;
import static org.junit.jupiter.api.Assertions.*;

class LimiteAutenticacaoTest {
    static class Relogio extends Clock {
        Instant agora = Instant.parse("2026-11-17T12:00:00Z");
        @Override public ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(ZoneId zone) { return this; }
        @Override public Instant instant() { return agora; }
    }
    @Test void limitaContaMesmoComIpsDistintosEResetaJanela() {
        var relogio = new Relogio();
        var limite = new LimiteAutenticacao(relogio);
        for (int i = 0; i < 10; i++) limite.conferir("ip" + i, "USER@example.test", false);
        var erro = assertThrows(WebApplicationException.class,
                () -> limite.conferir("outro", " user@example.test ", false));
        assertEquals(429, erro.getResponse().getStatus());
        assertEquals("60", erro.getResponse().getHeaderString("Retry-After"));
        relogio.agora = relogio.agora.plusSeconds(61);
        assertDoesNotThrow(() -> limite.conferir("outro", "user@example.test", false));
    }
    @Test void limitaIpECustoConcorrente() {
        var limite = new LimiteAutenticacao(new Relogio());
        for (int i = 0; i < 10; i++) limite.conferir("mesmo-ip", "user" + i, true);
        assertThrows(WebApplicationException.class, () -> limite.conferir("mesmo-ip", "outra", true));
        limite.adquirir();
        limite.adquirir();
        assertThrows(WebApplicationException.class, limite::adquirir);
        limite.liberar();
        assertDoesNotThrow(limite::adquirir);
        limite.liberar();
        limite.liberar();
    }
}
