package br.com.ecotransparencia.repository;

import br.com.ecotransparencia.entity.Cepim;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

/**
 * Repositorio para CEPIM.
 */
@ApplicationScoped
public class CepimRepository implements PanacheRepositoryBase<Cepim, Long> {

    public List<Cepim> findByCpfCnpj(String cnpj) {
        return list("cnpjEntidade", cnpj);
    }
}
