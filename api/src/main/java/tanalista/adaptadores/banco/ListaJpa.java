package tanalista.adaptadores.banco;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import tanalista.dominio.Lista;

@Entity(name = "ListaJpa")
@Table(name = "lista")
public class ListaJpa {
    @Id public UUID id;
    @Column(name = "dono_id", nullable = false) public UUID donoId;
    @Column(nullable = false, length = 120) public String nome;
    @Column(name = "nome_normalizado", nullable = false, length = 240) public String nomeNormalizado;
    @Column(name = "criada_em", nullable = false) public Instant criadaEm;
    @Column(name = "atualizada_em", nullable = false) public Instant atualizadaEm;

    public Lista dominio() { return new Lista(id, donoId, nome, criadaEm, atualizadaEm); }

    public void preencher(Lista lista) {
        id = lista.id();
        donoId = lista.donoId();
        nome = lista.nome();
        nomeNormalizado = lista.nomeNormalizado();
        criadaEm = lista.criadaEm();
        atualizadaEm = lista.atualizadaEm();
    }
}
