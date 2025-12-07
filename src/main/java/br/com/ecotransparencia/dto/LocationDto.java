package br.com.ecotransparencia.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Localizacao geografica do embargo")
public class LocationDto {

    @Schema(description = "Sigla da Unidade Federativa", example = "RJ")
    private String uf;

    @Schema(description = "Nome do municipio", example = "Teresopolis")
    private String municipio;

    @Schema(description = "Nome do imovel embargado", example = "Prefeitura Municipal de Teresopolis")
    private String imovel;

    public LocationDto() {
    }

    public LocationDto(String uf, String municipio, String imovel) {
        this.uf = uf;
        this.municipio = municipio;
        this.imovel = imovel;
    }

    public String getUf() {
        return uf;
    }

    public void setUf(String uf) {
        this.uf = uf;
    }

    public String getMunicipio() {
        return municipio;
    }

    public void setMunicipio(String municipio) {
        this.municipio = municipio;
    }

    public String getImovel() {
        return imovel;
    }

    public void setImovel(String imovel) {
        this.imovel = imovel;
    }
}
