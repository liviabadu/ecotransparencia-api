package br.com.ecotransparencia.repository;

import br.com.ecotransparencia.entity.SancaoAdmPublica;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

/**
 * Repositorio para sancoes administrativas (CEIS + CNEP).
 */
@ApplicationScoped
public class SancaoAdmPublicaRepository implements PanacheRepositoryBase<SancaoAdmPublica, Long> {

    /**
     * Busca sancoes pelo CPF/CNPJ (digitos apenas).
     */
    public List<SancaoAdmPublica> findByCpfCnpj(String cpfCnpj) {
        return list("cpfCnpj", cpfCnpj);
    }
}
