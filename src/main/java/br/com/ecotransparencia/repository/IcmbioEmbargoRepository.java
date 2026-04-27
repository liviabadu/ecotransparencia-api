package br.com.ecotransparencia.repository;

import br.com.ecotransparencia.entity.IcmbioEmbargo;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

/**
 * Repositorio para embargos do ICMBio.
 */
@ApplicationScoped
public class IcmbioEmbargoRepository implements PanacheRepositoryBase<IcmbioEmbargo, Integer> {

    /**
     * Busca embargos pelo CPF/CNPJ (digitos apenas).
     */
    public List<IcmbioEmbargo> findByCpfCnpj(String cpfCnpj) {
        return list("cpfCnpj", cpfCnpj);
    }
}
