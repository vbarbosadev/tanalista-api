package tanalista.adaptadores.http;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import tanalista.aplicacao.Itens;
import tanalista.dominio.ErroDominio;
import tanalista.dominio.Item;

/** Campo ausente mantém o valor; null explícito limpa somente os opcionais. */
public class ItemPatchRequest {
    public String nome;
    public BigDecimal quantidade;
    public String unidade;
    public String categoria;
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
