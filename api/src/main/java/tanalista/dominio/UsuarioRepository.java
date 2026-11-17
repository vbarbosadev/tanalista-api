package tanalista.dominio;

import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepository {
    Usuario criar(Usuario usuario);
    Optional<Usuario> buscarPorEmail(String emailNormalizado);
    Optional<Usuario> buscar(UUID id);
}
