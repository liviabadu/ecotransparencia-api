package br.com.ecotransparencia.repository;

import br.com.ecotransparencia.entity.AutoInfracao;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

/**
 * Repositorio para acesso aos dados de Autos de Infracao do IBAMA.
 */
@ApplicationScoped
public class AutoInfracaoRepository implements PanacheRepositoryBase<AutoInfracao, Long> {

    /**
     * Busca autos de infracao por CPF ou CNPJ do infrator.
     */
    public List<AutoInfracao> findByDocument(String document) {
        return list("cpfCnpjInfrator", document);
    }

    /**
     * Busca autos de infracao por nome do infrator (busca parcial, case-insensitive).
     */
    public List<AutoInfracao> findByNameContaining(String name) {
        return list("lower(nomeInfrator) like lower(?1)", "%" + name + "%");
    }

    /**
     * Busca autos de infracao nao cancelados por documento.
     */
    public List<AutoInfracao> findActiveByDocument(String document) {
        return list("cpfCnpjInfrator = ?1 and situacaoCancelado != 'S'", document);
    }
}
