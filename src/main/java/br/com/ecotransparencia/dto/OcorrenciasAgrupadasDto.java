package br.com.ecotransparencia.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

/**
 * DTO que agrupa as ocorrencias por fonte de dados.
 */
@Schema(description = "Ocorrencias agrupadas por fonte de dados")
public class OcorrenciasAgrupadasDto {

    @Schema(description = "Lista de embargos ambientais (areas embargadas)")
    private List<OccurrenceDto> embargos;

    @Schema(description = "Lista de autos de infracao")
    private List<AutoInfracaoDto> autosInfracao;

    public OcorrenciasAgrupadasDto() {
    }

    public List<OccurrenceDto> getEmbargos() {
        return embargos;
    }

    public void setEmbargos(List<OccurrenceDto> embargos) {
        this.embargos = embargos;
    }

    public List<AutoInfracaoDto> getAutosInfracao() {
        return autosInfracao;
    }

    public void setAutosInfracao(List<AutoInfracaoDto> autosInfracao) {
        this.autosInfracao = autosInfracao;
    }
}
