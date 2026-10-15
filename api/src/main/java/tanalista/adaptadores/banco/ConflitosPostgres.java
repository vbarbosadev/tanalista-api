package tanalista.adaptadores.banco;

import org.hibernate.exception.ConstraintViolationException;
import org.postgresql.util.PSQLException;
import tanalista.dominio.ErroDominio;

final class ConflitosPostgres {
    private ConflitosPostgres() { }

    static RuntimeException traduzir(RuntimeException erro) {
        for (Throwable causa = erro; causa != null; causa = causa.getCause()) {
            // Usa metadados do protocolo PostgreSQL, não o texto localizado da mensagem.
            if (causa instanceof PSQLException pg && "23505".equals(pg.getSQLState())
                    && pg.getServerErrorMessage() != null
                    && conhecida(pg.getServerErrorMessage().getConstraint())) {
                return ErroDominio.duplicado();
            }
            if (causa instanceof ConstraintViolationException violacao
                    && "23505".equals(violacao.getSQLState())
                    && conhecida(violacao.getConstraintName())) {
                return ErroDominio.duplicado();
            }
        }
        return erro;
    }

    private static boolean conhecida(String nome) {
        return "uq_item_lista_nome".equals(nome) || "uq_lista_dono_nome".equals(nome);
    }
}
