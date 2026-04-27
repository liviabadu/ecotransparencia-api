package br.com.ecotransparencia.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Marker de carga de dados.
 *
 * Usada pelos loaders de startup para garantir idempotencia: a presenca de
 * um registro com determinado {@code source} sinaliza que aquela fonte ja
 * foi carregada, evitando reprocessamento em boots subsequentes.
 *
 * Substitui a verificacao antiga {@code repository.count() > 0}, que era
 * fragil em cenarios de carga parcial.
 */
@Entity
@Table(name = "data_load_marker")
public class DataLoadMarker {

    @Id
    @Column(name = "source", length = 64)
    private String source;

    @Column(name = "version", length = 32)
    private String version;

    @Column(name = "loaded_at", nullable = false)
    private LocalDateTime loadedAt;

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public LocalDateTime getLoadedAt() {
        return loadedAt;
    }

    public void setLoadedAt(LocalDateTime loadedAt) {
        this.loadedAt = loadedAt;
    }
}
