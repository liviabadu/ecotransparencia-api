package br.com.ecotransparencia.dto;

import java.util.List;

public class EntityDto {

    private String id;
    private String name;
    private String document;
    private String documentType;
    private String riskLevel;
    private int score;
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
}
