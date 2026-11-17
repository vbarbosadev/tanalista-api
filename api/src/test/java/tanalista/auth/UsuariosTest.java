package tanalista.auth;

import org.junit.jupiter.api.Test;
import tanalista.aplicacao.Usuarios;
import tanalista.adaptadores.seguranca.SenhasArgon2;
import tanalista.dominio.*;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class UsuariosTest {
    @Test void hashArgon2TemSaltENuncaNormalizaSenha() {
        var senhas = new SenhasArgon2();
        var a = senhas.gerarHash(" Senha longa 123 ");
        var b = senhas.gerarHash(" Senha longa 123 ");
        assertNotEquals(a, b);
        assertTrue(senhas.confere(" Senha longa 123 ", a));
        assertFalse(senhas.confere("Senha longa 123", a));
        assertFalse(senhas.confere("Senha errada", a));
        assertFalse(senhas.confere("Senha", "$argon2id$malformado"));
    }
    @Test void cadastroLoginNormalizacaoERejeicaoGenerica() {
        var dados = new HashMap<UUID, Usuario>();
        var repositorio = new UsuarioRepository() {
            public Usuario criar(Usuario u) {
                if (buscarPorEmail(u.email()).isPresent()) throw ErroDominio.duplicado();
                dados.put(u.id(), u);
                return u;
            }
            public Optional<Usuario> buscarPorEmail(String email) {
                return dados.values().stream().filter(u -> u.email().equals(email)).findFirst();
            }
            public Optional<Usuario> buscar(UUID id) { return Optional.ofNullable(dados.get(id)); }
        };
        var agora = Instant.parse("2026-11-17T12:00:00Z");
        var usuarios = new Usuarios(repositorio, new SenhasArgon2(), Clock.fixed(agora, ZoneOffset.UTC));
        var u = usuarios.cadastrar(" User@Example.Test ", "Senha-segura-123");
        assertEquals("user@example.test", u.email());
        assertEquals(agora, u.criadoEm());
        assertEquals(u.id(), usuarios.autenticar("USER@example.test", "Senha-segura-123").id());
        assertEquals(u, usuarios.buscar(u.id()));
        assertThrows(ErroDominio.class, () -> usuarios.cadastrar("user@example.test", "Senha-segura-123"));
        for (var email : new String[]{"user@example.test", "absent@example.test", "invalido", null}) {
            assertEquals("E-mail ou senha inválidos.", assertThrows(CredenciaisInvalidas.class,
                    () -> usuarios.autenticar(email, "Senha-errada-123")).getMessage());
        }
        assertFalse(u.toString().contains(u.senhaHash()));
        assertFalse(u.toString().contains(u.email()));
    }
    @Test void entradasTemLimitesExplicitos() {
        for (String email : new String[]{"x", "x@localhost", "x@@example.test", "á@example.test",
                ".x@example.test", "x..y@example.test", "x@" + "a".repeat(64) + ".test",
                "x".repeat(65) + "@example.test"}) {
            assertThrows(ErroDominio.class, () -> Usuario.normalizarEmail(email));
        }
        for (String senha : new String[]{"curta", "x".repeat(129)}) {
            assertThrows(ErroDominio.class, () -> Usuario.validarSenha(senha));
        }
        assertDoesNotThrow(() -> Usuario.validarSenha("x".repeat(128)));
    }
}
