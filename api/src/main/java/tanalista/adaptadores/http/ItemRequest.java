package tanalista.adaptadores.http;

import java.math.BigDecimal;
import tanalista.aplicacao.Itens;
import jakarta.validation.constraints.*;

public record ItemRequest(@NotBlank @Size(max = 120) String nome,
                          @Positive @Digits(integer = 9, fraction = 3) BigDecimal quantidade,
                          @Size(max = 30) String unidade, @Size(max = 60) String categoria,
                          @PositiveOrZero @Digits(integer = 9, fraction = 2) BigDecimal precoEstimado) {
    public Itens.Dados dados() { return new Itens.Dados(nome, quantidade, unidade, categoria, precoEstimado); }
}
