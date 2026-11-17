package tanalista.aplicacao;

import tanalista.dominio.*;
import java.time.Clock;
import java.util.UUID;

public class Usuarios {
    private final UsuarioRepository usuarios;
    private final Senhas senhas;
    private final Clock relogio;
    private final String hashAusente;

    public Usuarios(UsuarioRepository usuarios, Senhas senhas, Clock relogio) {
        this.usuarios = usuarios;
        this.senhas = senhas;
        this.relogio = relogio;
        // Same password verification cost even when no account is found.
        this.hashAusente = senhas.gerarHash(UUID.randomUUID().toString());
    }

    public Usuario cadastrar(String email, String senha) {
        var normalizado = Usuario.normalizarEmail(email);
        Usuario.validarSenha(senha);
        return usuarios.criar(new Usuario(UUID.randomUUID(), normalizado,
                senhas.gerarHash(senha), relogio.instant()));
    }

    public Usuario autenticar(String email, String senha) {
        String normalizado;
        try {
            normalizado = Usuario.normalizarEmail(email);
            Usuario.validarSenha(senha);
        } catch (ErroDominio invalido) {
            senhas.confere("entrada-invalida", hashAusente);
            throw new CredenciaisInvalidas();
        }
        var usuario = usuarios.buscarPorEmail(normalizado);
        boolean confere = senhas.confere(senha, usuario.map(Usuario::senhaHash).orElse(hashAusente));
        if (!confere || usuario.isEmpty()) throw new CredenciaisInvalidas();
        return usuario.get();
    }

    public Usuario buscar(UUID id) {
        return usuarios.buscar(id).orElseThrow(CredenciaisInvalidas::new);
    }
}
