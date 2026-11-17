package tanalista.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConfiguracaoAutenticacaoTest {
    @Test
    void modoPrivadoNaoExigeProvedorMasAuthNaoAceitaFallback() {
        assertDoesNotThrow(() -> ConfiguracaoAutenticacao.validar(false, true, "", "", "", false));
        assertThrows(IllegalStateException.class, () ->
                ConfiguracaoAutenticacao.validar(true, true, "https://issuer.example", "api",
                        "https://issuer.example/jwks", false));
    }

    @Test
    void autenticacaoExigeEmissorAudienciaEChavesHttps() {
        assertDoesNotThrow(() -> ConfiguracaoAutenticacao.validar(true, false,
                "https://issuer.example/", "tanalista-api", "https://issuer.example/jwks", false));
        for (var configuracao : new String[][] {
                {"", "api", "https://issuer.example/jwks"},
                {"https://issuer.example", "", "https://issuer.example/jwks"},
                {"https://issuer.example", "api", ""},
                {"https://issuer.example", "unconfigured", "https://issuer.example/jwks"},
                {"http://issuer.example", "api", "https://issuer.example/jwks"},
                {"https://issuer.example", "api", "file:///tmp/public.pem"},
                {"https://issuer.example", "api", "http://issuer.example/jwks"},
                {"https://issuer.example", "api", "https://user:secret@issuer.example/jwks"},
                {"https://issuer.example", "api", "not a URL"}}) {
            assertThrows(IllegalStateException.class, () -> ConfiguracaoAutenticacao.validar(
                    true, false, configuracao[0], configuracao[1], configuracao[2], false));
        }
    }

    @Test
    void arquivoDeChavesEhPermitidoSomenteEmTestes() {
        assertDoesNotThrow(() -> ConfiguracaoAutenticacao.validar(true, false,
                "https://issuer.example", "api", "file:///fixture/public.pem", true));
    }
}
