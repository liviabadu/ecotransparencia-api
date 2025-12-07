package br.com.ecotransparencia.dto;

public class LocationDto {

    private String uf;
    private String municipio;
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
