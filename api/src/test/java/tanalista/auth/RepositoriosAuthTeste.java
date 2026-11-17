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
    ListaRepository listas() {
        return new ListasEmMemoria();
    }

    @Produces
    @ApplicationScoped
    ItemRepository itens() {
        return new ItensEmMemoria();
    }
}
