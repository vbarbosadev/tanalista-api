package tanalista.adaptadores.banco;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;
import tanalista.dominio.Item;

@Entity(name = "ItemJpa")
@Table(name = "item")
public class ItemJpa {
    @Id public UUID id;
    @Column(name = "lista_id", nullable = false) public UUID listaId;
    @Column(nullable = false, length = 120) public String nome;
    @Column(name = "nome_normalizado", nullable = false, length = 240) public String nomeNormalizado;
    @Column(precision = 12, scale = 3) public BigDecimal quantidade;
    @Column(length = 30) public String unidade;
    @Column(length = 60) public String categoria;
    @Column(name = "preco_estimado", precision = 11, scale = 2) public BigDecimal precoEstimado;

    public Item dominio() { return new Item(id, listaId, nome, quantidade, unidade, categoria, precoEstimado); }

    public void preencher(Item item) {
        id = item.id();
        listaId = item.listaId();
        nome = item.nome();
        nomeNormalizado = item.nomeNormalizado();
        quantidade = item.quantidade();
        unidade = item.unidade();
        categoria = item.categoria();
        precoEstimado = item.precoEstimado();
    }
}
