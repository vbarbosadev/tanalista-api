package tanalista.dominio;

import java.util.List;

public record Pagina<T>(List<T> items, int page, int size, long total) {
    public Pagina { items = List.copyOf(items); }

    public static void validar(int page, int size) {
        if (page < 0 || size < 1 || size > 100 || (long) page * size > Integer.MAX_VALUE) {
            throw ErroDominio.invalido("page deve ser não negativo e size entre 1 e 100; offset máximo: 2147483647.");
        }
    }
}
