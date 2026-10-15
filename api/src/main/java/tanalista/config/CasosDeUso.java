package tanalista.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import tanalista.aplicacao.Listas;
import tanalista.dominio.ItemRepository;
import tanalista.dominio.ListaRepository;
import java.time.Clock;

@ApplicationScoped
public class CasosDeUso {
    @Produces
    Listas listas(ListaRepository listas, ItemRepository itens) {
        return new Listas(listas, itens, Clock.systemUTC());
    }
}
