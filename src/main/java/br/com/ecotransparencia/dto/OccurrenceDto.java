package br.com.ecotransparencia.dto;

import java.math.BigDecimal;

public class OccurrenceDto {

    private String id;
    private String category;
    private String date;
    private String description;
    private String source;
    private String sourceUrl;
    private String status;

    // US-006: Campos adicionais
    private String autoInfracao;
    private Boolean desmatamento;
    private BigDecimal areaEmbargada;
    private String biome;
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
