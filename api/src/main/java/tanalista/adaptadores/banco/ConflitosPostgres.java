package tanalista.adaptadores.banco;

import org.hibernate.exception.ConstraintViolationException;
import tanalista.dominio.ErroDominio;

final class ConflitosPostgres {
    private ConflitosPostgres() { }

    static RuntimeException traduzir(RuntimeException erro) {
        for (Throwable causa = erro; causa != null; causa = causa.getCause()) {
            if (causa instanceof ConstraintViolationException violacao
                    && "23505".equals(violacao.getSQLState())
                    && ("uq_item_lista_nome".equals(violacao.getConstraintName())
                    || "uq_lista_dono_nome".equals(violacao.getConstraintName()))) {
                return ErroDominio.duplicado();
            }
        }
        return erro;
    }
}
