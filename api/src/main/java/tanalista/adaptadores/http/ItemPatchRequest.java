package tanalista.adaptadores.http;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import tanalista.aplicacao.Itens;
import tanalista.dominio.ErroDominio;
import tanalista.dominio.Item;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/** Campo ausente mantém o valor; null explícito limpa somente os opcionais. */
public class ItemPatchRequest {
    @Schema(maxLength = 120, description = "Nome não nulo; campo ausente mantém o nome atual.")
    public String nome;
    @Schema(nullable = true, minimum = "0", exclusiveMinimum = true, maximum = "999999999.999", multipleOf = 0.001)
    public BigDecimal quantidade;
    @Schema(nullable = true, maxLength = 30)
    public String unidade;
    @Schema(nullable = true, maxLength = 60)
    public String categoria;
    @Schema(nullable = true, minimum = "0", maximum = "999999999.99", multipleOf = 0.01)
    public BigDecimal precoEstimado;
    @JsonIgnore private final Set<String> presentes = new HashSet<>();

    public void setNome(String valor) { nome = valor; presentes.add("nome"); }
    public void setQuantidade(BigDecimal valor) { quantidade = valor; presentes.add("quantidade"); }
    public void setUnidade(String valor) { unidade = valor; presentes.add("unidade"); }
    public void setCategoria(String valor) { categoria = valor; presentes.add("categoria"); }
    public void setPrecoEstimado(BigDecimal valor) { precoEstimado = valor; presentes.add("precoEstimado"); }

    public Itens.Dados mesclar(Item atual) {
        if (presentes.isEmpty()) throw ErroDominio.invalido("Informe ao menos um campo para atualização.");
        return new Itens.Dados(
                presentes.contains("nome") ? nome : atual.nome(),
                presentes.contains("quantidade") ? quantidade : atual.quantidade(),
                presentes.contains("unidade") ? unidade : atual.unidade(),
                presentes.contains("categoria") ? categoria : atual.categoria(),
                presentes.contains("precoEstimado") ? precoEstimado : atual.precoEstimado());
    }
}
