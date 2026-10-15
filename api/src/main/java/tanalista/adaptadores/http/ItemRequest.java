package tanalista.adaptadores.http;

import java.math.BigDecimal;
import tanalista.aplicacao.Itens;
import jakarta.validation.constraints.*;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

public record ItemRequest(@NotBlank @Size(max = 120) String nome,
                          @Schema(nullable = true, minimum = "0", exclusiveMinimum = true, maximum = "999999999.999", multipleOf = 0.001)
                          @Positive @Digits(integer = 9, fraction = 3) BigDecimal quantidade,
                          @Schema(nullable = true) @Size(max = 30) String unidade,
                          @Schema(nullable = true) @Size(max = 60) String categoria,
                          @Schema(nullable = true, minimum = "0", maximum = "999999999.99", multipleOf = 0.01)
                          @PositiveOrZero @Digits(integer = 9, fraction = 2) BigDecimal precoEstimado) {
    public Itens.Dados dados() { return new Itens.Dados(nome, quantidade, unidade, categoria, precoEstimado); }
}
