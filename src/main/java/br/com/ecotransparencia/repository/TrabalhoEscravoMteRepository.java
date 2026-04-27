package br.com.ecotransparencia.repository;

import br.com.ecotransparencia.entity.TrabalhoEscravoMte;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

/**
 * Repositorio para a Lista Suja do MTE.
 *
 * Busca por CPF/CNPJ digit-only (campo {@code cpfCnpj}); o valor formatado
 * fica em {@code cpfCnpjFormatado} apenas para referencia/exibicao.
 */
@ApplicationScoped
public class TrabalhoEscravoMteRepository implements PanacheRepositoryBase<TrabalhoEscravoMte, Long> {

    public List<TrabalhoEscravoMte> findByCpfCnpj(String cpfCnpj) {
        return list("cpfCnpj", cpfCnpj);
    }
}
