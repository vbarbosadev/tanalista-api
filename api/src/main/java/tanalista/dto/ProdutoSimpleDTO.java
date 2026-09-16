package tanalista.dto;

import java.math.BigDecimal;

public record ProdutoSimpleDTO(
    Long id, 
    String nome, 
    String descricao, 
    BigDecimal preco, 
    String status) {

        
}
