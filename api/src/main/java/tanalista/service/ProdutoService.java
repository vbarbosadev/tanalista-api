package tanalista.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tanalista.dto.ProdutoSimpleDTO;
import tanalista.repository.ProdutoRepository;

import java.util.List;

@ApplicationScoped 
public class ProdutoService {
    
    private final ProdutoRepository produtoRepository;

    public ProdutoService(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    public List<ProdutoSimpleDTO> getAllProdutos() {
        var produtos = produtoRepository.findAllAtivos();
        return produtos.stream()
                .map(produto -> new ProdutoSimpleDTO(
                        produto.getId(),
                        produto.getNome(),
                        produto.getDescricao(),
                        produto.getPreco(),
                        produto.getStatus().name()))
                .toList();
    }


}
