package tanalista.auth;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.enterprise.inject.Produces;
import tanalista.adaptadores.memoria.ItensEmMemoria;
import tanalista.adaptadores.memoria.ListasEmMemoria;
import tanalista.dominio.ItemRepository;
import tanalista.dominio.ListaRepository;

/** Selected only by AuthProfile; PostgreSQL has its own separate integration suite. */
@Alternative
@ApplicationScoped
public class RepositoriosAuthTeste {
    @Produces
    @ApplicationScoped
    tanalista.dominio.UsuarioRepository usuarios() {
        return new tanalista.dominio.UsuarioRepository() {
            private final java.util.Map<java.util.UUID, tanalista.dominio.Usuario> dados = new java.util.HashMap<>();
            public synchronized tanalista.dominio.Usuario criar(tanalista.dominio.Usuario usuario) {
                if (buscarPorEmail(usuario.email()).isPresent()) {
                    throw new tanalista.dominio.ErroDominio(
                            tanalista.dominio.ErroDominio.Tipo.CONFLITO, "E-mail já cadastrado.");
                }
                dados.put(usuario.id(), usuario);
                return usuario;
            }
            public synchronized java.util.Optional<tanalista.dominio.Usuario> buscarPorEmail(String email) {
                return dados.values().stream().filter(u -> u.email().equals(email)).findFirst();
            }
            public synchronized java.util.Optional<tanalista.dominio.Usuario> buscar(java.util.UUID id) {
                return java.util.Optional.ofNullable(dados.get(id));
            }
        };
    }

    @Produces
    @ApplicationScoped
    ListaRepository listas() {
        return new ListasEmMemoria();
    }

    @Produces
    @ApplicationScoped
    ItemRepository itens() {
        return new ItensEmMemoria();
    }
}
