package br.com.ecotransparencia.repository;

import br.com.ecotransparencia.entity.Embargo;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class EmbargoRepository implements PanacheRepositoryBase<Embargo, Long> {

    public List<Embargo> findByDocument(String document) {
        return list("cpfCnpjEmbargado", document);
    }

    public List<Embargo> findByNameContaining(String name) {
        return list("lower(nomePessoaEmbargada) like lower(?1)", "%" + name + "%");
    }
}
