package tanalista.adaptadores.http;

import java.math.BigDecimal;
import tanalista.aplicacao.Itens;

public record ItemRequest(String nome, BigDecimal quantidade, String unidade, String categoria, BigDecimal precoEstimado) {
    public Itens.Dados dados() { return new Itens.Dados(nome, quantidade, unidade, categoria, precoEstimado); }
}
