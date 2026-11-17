package tanalista.adaptadores.banco;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import org.hibernate.exception.ConstraintViolationException;
import tanalista.dominio.*;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class UsuariosPostgres implements UsuarioRepository, PanacheRepositoryBase<UsuarioJpa, UUID> {
    @Override
    public Usuario criar(Usuario usuario) {
        var entidade = new UsuarioJpa();
        entidade.id = usuario.id();
        entidade.email = usuario.email();
        entidade.senhaHash = usuario.senhaHash();
        entidade.criadoEm = usuario.criadoEm();
        try {
            persistAndFlush(entidade);
            return entidade.dominio();
        } catch (RuntimeException falha) {
            for (Throwable causa = falha; causa != null; causa = causa.getCause()) {
                if (causa instanceof ConstraintViolationException violacao
                        && "23505".equals(violacao.getSQLState())) {
                    throw new ErroDominio(ErroDominio.Tipo.CONFLITO, "E-mail já cadastrado.");
                }
            }
            throw falha;
        }
    }

    @Override public Optional<Usuario> buscarPorEmail(String email) {
        return find("email", email).firstResultOptional().map(UsuarioJpa::dominio);
    }

    @Override public Optional<Usuario> buscar(UUID id) {
        return findByIdOptional(id).map(UsuarioJpa::dominio);
    }
}
