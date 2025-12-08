package br.com.ecotransparencia.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.math.BigDecimal;

/**
 * DTO que representa um Auto de Infracao do IBAMA.
 */
@Schema(description = "Dados de um auto de infracao ambiental")
public class AutoInfracaoDto {

    @Schema(description = "Identificador unico do auto", example = "2093413")
    private String id;

    @Schema(description = "Numero do auto de infracao", example = "AMCU4YGL")
    private String numeroAuto;

    @Schema(description = "Data do auto em formato ISO 8601", example = "2024-01-03T00:00:00.000Z")
    private String data;

    @Schema(description = "Descricao do auto de infracao")
    private String descricao;

    @Schema(description = "Tipo de infracao: Flora, Fauna, Administracao Ambiental, etc.", example = "Flora")
    private String tipoInfracao;

    @Schema(description = "Valor da multa em reais", example = "15000.00")
    private BigDecimal valorMulta;

    @Schema(description = "Status do auto: Lavrado, Cancelado, etc.", example = "Lavrado")
    private String status;

    @Schema(description = "Gravidade: Leve, Grave, Gravissima", example = "Grave")
    private String gravidade;

    @Schema(description = "Motivacao da conduta: Intencional, Culposa", example = "Intencional")
    private String motivacaoConduta;

    @Schema(description = "Efeito no meio ambiente: Fraca, Media, Grave", example = "Fraca")
    private String efeitoMeioAmbiente;

    @Schema(description = "Biomas atingidos", example = "Cerrado")
    private String biomasAtingidos;

    @Schema(description = "Enquadramento legal", example = "Art. 47 - Decreto 6514/2008")
    private String enquadramentoLegal;

    @Schema(description = "Localizacao geografica")
    private LocationDto location;

    @Schema(description = "Fonte dos dados", example = "IBAMA")
    private String source;

    public AutoInfracaoDto() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNumeroAuto() {
        return numeroAuto;
    }

    public void setNumeroAuto(String numeroAuto) {
        this.numeroAuto = numeroAuto;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getTipoInfracao() {
        return tipoInfracao;
    }

    public void setTipoInfracao(String tipoInfracao) {
        this.tipoInfracao = tipoInfracao;
    }

    public BigDecimal getValorMulta() {
        return valorMulta;
    }

    public void setValorMulta(BigDecimal valorMulta) {
        this.valorMulta = valorMulta;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getGravidade() {
        return gravidade;
    }

    public void setGravidade(String gravidade) {
        this.gravidade = gravidade;
    }

    public String getMotivacaoConduta() {
        return motivacaoConduta;
    }

    public void setMotivacaoConduta(String motivacaoConduta) {
        this.motivacaoConduta = motivacaoConduta;
    }

    public String getEfeitoMeioAmbiente() {
        return efeitoMeioAmbiente;
    }

    public void setEfeitoMeioAmbiente(String efeitoMeioAmbiente) {
        this.efeitoMeioAmbiente = efeitoMeioAmbiente;
    }

    public String getBiomasAtingidos() {
        return biomasAtingidos;
    }

    public void setBiomasAtingidos(String biomasAtingidos) {
        this.biomasAtingidos = biomasAtingidos;
    }

    public String getEnquadramentoLegal() {
        return enquadramentoLegal;
    }

    public void setEnquadramentoLegal(String enquadramentoLegal) {
        this.enquadramentoLegal = enquadramentoLegal;
    }

    public LocationDto getLocation() {
        return location;
    }

    public void setLocation(LocationDto location) {
        this.location = location;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
