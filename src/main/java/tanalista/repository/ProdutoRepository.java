package tanalista.repository;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import tanalista.entity.Produto;
import tanalista.entity.enums.Status;

import java.util.List;

@ApplicationScoped 
public class ProdutoRepository implements PanacheRepository<Produto> {

    public Produto findById(Long id) {
        return find("id", id).firstResult();
    }

    public List<Produto> findAllAtivos() {
        return list("status", Status.ATIVO);
    }

    public void deleteStefs(){
       delete("name", "Stef");
  }



}