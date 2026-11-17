package tanalista.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IdentificadorExternoTest {
    @Test
    void identidadeEhEstavelSemExigirSubjectUuid() {
        var id = IdentificadorExterno.de("https://issuer.example/", "provider|alice");
        assertEquals(id, IdentificadorExterno.de("https://issuer.example/", "provider|alice"));
        assertEquals(8, id.version());
        assertEquals(2, id.variant());
        assertNotEquals(id, IdentificadorExterno.de("https://issuer.example/", "provider|bob"));
        assertNotEquals(id, IdentificadorExterno.de("https://other.example/", "provider|alice"));
        assertNotEquals(IdentificadorExterno.de("ab", "c"), IdentificadorExterno.de("a", "bc"));
    }

    @Test
    void rejeitaIdentidadeIncompleta() {
        assertThrows(IllegalArgumentException.class, () -> IdentificadorExterno.de(null, "a"));
        assertThrows(IllegalArgumentException.class, () -> IdentificadorExterno.de("issuer", " "));
    }
}
