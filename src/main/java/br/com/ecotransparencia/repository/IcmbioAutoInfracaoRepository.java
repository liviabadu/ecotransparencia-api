package br.com.ecotransparencia.repository;

import br.com.ecotransparencia.entity.IcmbioAutoInfracao;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

/**
 * Repositorio para autos de infracao do ICMBio.
 */
@ApplicationScoped
public class IcmbioAutoInfracaoRepository implements PanacheRepositoryBase<IcmbioAutoInfracao, Integer> {

    /**
     * Busca autos pelo CPF/CNPJ (digitos apenas).
     */
    public List<IcmbioAutoInfracao> findByCpfCnpj(String cpfCnpj) {
        return list("cpfCnpj", cpfCnpj);
    }
}
