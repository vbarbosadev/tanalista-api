package tanalista.controller;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import tanalista.service.ProdutoService;
import tanalista.dto.ProdutoSimpleDTO;

import java.util.List;

@Path("/produtos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProdutoController {

    private final ProdutoService produtoService;

    public ProdutoController(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }
    
   @GET
    public List<ProdutoSimpleDTO> getProduto() {
          return produtoService.getAllProdutos();
     } 

}
