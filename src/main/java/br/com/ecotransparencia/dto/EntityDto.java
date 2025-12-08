package br.com.ecotransparencia.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

@Schema(description = "Dados da entidade (pessoa fisica ou juridica) com ocorrencias ambientais")
public class EntityDto {

    @Schema(description = "Identificador unico da entidade", example = "1872430")
    private String id;

    @Schema(description = "Nome da entidade", example = "PREF MUN DE TERESOPOLIS")
    private String name;

    @Schema(description = "CPF ou CNPJ da entidade", example = "29138369000147")
    private String document;

    @Schema(description = "Tipo do documento: 'cpf' ou 'cnpj'", example = "cnpj")
    private String documentType;

    @Schema(description = "Score ASG calculado a partir de multiplas fontes")
    private AsgScoreDto asgScore;

    @Schema(description = "Nivel de risco: Baixo, Medio, Alto ou Critico (derivado do ASG)", example = "Baixo")
    private String riskLevel;

    @Schema(description = "Score numerico de risco (0-100) (derivado do ASG)", example = "15")
    private int score;

    @Schema(description = "Ocorrencias agrupadas por fonte de dados")
    private OcorrenciasAgrupadasDto ocorrencias;

    @Schema(description = "Lista de embargos associados a entidade (retrocompatibilidade)", deprecated = true)
    private List<OccurrenceDto> occurrences;

    public EntityDto() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDocument() {
        return document;
    }

    public void setDocument(String document) {
        this.document = document;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public List<OccurrenceDto> getOccurrences() {
        return occurrences;
    }

    public void setOccurrences(List<OccurrenceDto> occurrences) {
        this.occurrences = occurrences;
    }

    public AsgScoreDto getAsgScore() {
        return asgScore;
    }

    public void setAsgScore(AsgScoreDto asgScore) {
        this.asgScore = asgScore;
    }

    public OcorrenciasAgrupadasDto getOcorrencias() {
        return ocorrencias;
    }

    public void setOcorrencias(OcorrenciasAgrupadasDto ocorrencias) {
        this.ocorrencias = ocorrencias;
    }
}
