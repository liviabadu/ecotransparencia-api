package br.com.ecotransparencia.repository;

import br.com.ecotransparencia.entity.DataLoadMarker;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Repositorio dos markers de carga inicial.
 *
 * Usado pelos loaders de startup para checar e marcar fontes ja carregadas.
 */
@ApplicationScoped
public class DataLoadMarkerRepository implements PanacheRepositoryBase<DataLoadMarker, String> {
}
