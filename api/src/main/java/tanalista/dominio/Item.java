package tanalista.dominio;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

public record Item(UUID id, UUID listaId, String nome, BigDecimal quantidade,
                   String unidade, String categoria, BigDecimal precoEstimado) {
    public Item {
        Objects.requireNonNull(id);
        Objects.requireNonNull(listaId);
        nome = Nomes.validar(nome);
        validarNumero(quantidade, 3, true, "Quantidade");
        validarNumero(precoEstimado, 2, false, "Preço estimado");
        unidade = opcional(unidade, 30, "Unidade");
        categoria = opcional(categoria, 60, "Categoria");
    }

    private static void validarNumero(BigDecimal valor, int casas, boolean positivo, String campo) {
        if (valor != null && (valor.signum() < 0 || (positivo && valor.signum() == 0)
                || valor.scale() > casas || valor.precision() - valor.scale() > 9)) {
            throw ErroDominio.invalido(campo + " fora dos limites permitidos.");
        }
    }

    private static String opcional(String valor, int maximo, String campo) {
        if (valor != null && (valor.isBlank() || valor.length() > maximo)) {
            throw ErroDominio.invalido(campo + " deve ser não vazio e ter até " + maximo + " caracteres.");
        }
        return valor == null ? null : valor.strip();
    }

    public String nomeNormalizado() { return Nomes.normalizar(nome); }
}
