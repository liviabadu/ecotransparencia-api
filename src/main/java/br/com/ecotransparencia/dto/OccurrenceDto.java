package br.com.ecotransparencia.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Dados de uma ocorrencia de embargo ambiental")
public class OccurrenceDto {

    @Schema(description = "Identificador unico do termo de embargo", example = "1872430")
    private String id;

    @Schema(description = "Categoria/tipo de area embargada", example = "Atividade")
    private String category;

    @Schema(description = "Data do embargo em formato ISO 8601", example = "1990-03-27T00:00:00.000Z")
    private String date;

    @Schema(description = "Descricao/justificativa do embargo", example = "Exercer atividades potencialmente degradadoras do meio ambiente.")
    private String description;

    @Schema(description = "Fonte dos dados", example = "IBAMA")
    private String source;

    @Schema(description = "URL para consulta na fonte oficial", example = "https://servicos.ibama.gov.br/ctf/publico/areasembargadas/ConsultaPublicaAreasEmbargadas.php")
    private String sourceUrl;

    @Schema(description = "Status do embargo: Ativo ou Baixado", example = "Ativo")
    private String status;

    @Schema(description = "Numero do auto de infracao vinculado", example = "598101-A")
    private String autoInfracao;

    @Schema(description = "Indica se o embargo e relacionado a desmatamento", example = "true")
    private Boolean desmatamento;

    @Schema(description = "Area embargada em hectares", example = "10.0")
    private BigDecimal areaEmbargada;

    @Schema(description = "Bioma afetado", example = "Amazonia")
    private String biome;

    @Schema(description = "Localizacao geografica do embargo")
    private LocationDto location;

    public OccurrenceDto() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAutoInfracao() {
        return autoInfracao;
    }

    public void setAutoInfracao(String autoInfracao) {
        this.autoInfracao = autoInfracao;
    }

    public Boolean getDesmatamento() {
        return desmatamento;
    }

    public void setDesmatamento(Boolean desmatamento) {
        this.desmatamento = desmatamento;
    }

    public BigDecimal getAreaEmbargada() {
        return areaEmbargada;
    }

    public void setAreaEmbargada(BigDecimal areaEmbargada) {
        this.areaEmbargada = areaEmbargada;
    }

    public String getBiome() {
        return biome;
    }

    public void setBiome(String biome) {
        this.biome = biome;
    }

    public LocationDto getLocation() {
        return location;
    }

    public void setLocation(LocationDto location) {
        this.location = location;
    }
}
